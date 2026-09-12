package worker

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"sync"
	"time"

	"campus-spider-service/internal/client"
	"campus-spider-service/internal/config"
	"campus-spider-service/internal/crypto"
	"campus-spider-service/internal/model"
	"campus-spider-service/internal/spider"
	"campus-spider-service/internal/store"

	"github.com/redis/go-redis/v9"
)

// Pool 是 Worker 消费池，负责多优先级队列调度、限流、死信队列与任务执行。
type Pool struct {
	concurrency int
	runner      *spider.Runner
	javaClient  *client.JavaClient
	store       *store.RedisStore
	scheduler   *PriorityScheduler
	rateLimiter *store.RateLimiter
	dlq         *store.DLQ
	cfg         config.Config

	stopOnce sync.Once
	stopCh   chan struct{}
	wg       sync.WaitGroup
}

// NewPool 创建 Worker 池
func NewPool(
	concurrency int,
	redisStore *store.RedisStore,
	scheduler *PriorityScheduler,
	rateLimiter *store.RateLimiter,
	dlq *store.DLQ,
	runner *spider.Runner,
	javaClient *client.JavaClient,
	cfg config.Config,
) *Pool {
	return &Pool{
		concurrency: concurrency,
		store:       redisStore,
		scheduler:   scheduler,
		rateLimiter: rateLimiter,
		dlq:         dlq,
		runner:      runner,
		javaClient:  javaClient,
		cfg:         cfg,
		stopCh:      make(chan struct{}),
	}
}

// Start 启动 Worker 消费池，阻塞直到 ctx 取消。
func (p *Pool) Start(ctx context.Context) error {
	consumerBase := hostname()
	if consumerBase == "" {
		consumerBase = "consumer"
	}

	for i := 0; i < p.concurrency; i++ {
		p.wg.Add(1)
		consumerName := fmt.Sprintf("%s-%d", consumerBase, i)
		go func(cn string) {
			defer p.wg.Done()
			p.consume(ctx, cn)
		}(consumerName)
	}

	<-ctx.Done()
	p.stopOnce.Do(func() { close(p.stopCh) })
	p.wg.Wait()
	return nil
}

// Stop 等待所有 Worker 优雅退出。
func (p *Pool) Stop(ctx context.Context) error {
	p.stopOnce.Do(func() { close(p.stopCh) })
	done := make(chan struct{})
	go func() {
		p.wg.Wait()
		close(done)
	}()
	select {
	case <-done:
		return nil
	case <-ctx.Done():
		return ctx.Err()
	}
}

// consume 单个 Worker 消费循环
func (p *Pool) consume(ctx context.Context, consumer string) {
	for {
		select {
		case <-ctx.Done():
			return
		case <-p.stopCh:
			return
		default:
		}

		// 按优先级调度消费一条消息
		consumed, _ := p.scheduler.ConsumeOne(ctx, consumer, func(priority string, msg redis.XMessage) {
			p.handleMessage(ctx, priority, consumer, msg)
		})

		if !consumed {
			// 所有队列都为空，短暂休眠避免空转
			select {
			case <-time.After(100 * time.Millisecond):
			case <-ctx.Done():
				return
			}
		}
	}
}

// handleMessage 处理单条任务消息
func (p *Pool) handleMessage(ctx context.Context, priority, consumer string, msg redis.XMessage) {
	defer func() {
		if r := recover(); r != nil {
			log.Printf("[Worker] 处理消息 panic consumer=%s err=%v", consumer, r)
		}
	}()

	task := store.MessageToTask(msg.Values)
	log.Printf("[Worker] 收到任务 taskId=%s type=%s priority=%s studentId=%s", task.TaskID, task.Type, priority, task.StudentID)

	// 字段校验
	if task.TaskID == "" || task.StudentID == "" || task.Password == "" {
		log.Printf("[Worker] 任务字段缺失，跳过 taskId=%s", task.TaskID)
		_ = p.store.Ack(ctx, priority, msg.ID)
		return
	}

	// 解密密码
	plainPassword, err := crypto.AesDecrypt(task.Password, p.cfg.AesSecretKey)
	if err != nil {
		log.Printf("[Worker] 密码解密失败 taskId=%s err=%v", task.TaskID, err)
		p.failTask(ctx, priority, msg.ID, task, "密码解密失败: "+err.Error())
		return
	}

	// 复制任务副本用于执行，避免修改原任务中的加密密码，确保死信队列重试时能正常解密
	execTask := task
	execTask.Password = plainPassword

	// 全局限流：固定窗口，每分钟 10 次，仅在实际执行学校请求前生效
	if err := p.acquireRateLimit(ctx); err != nil {
		log.Printf("[Worker] 限流获取失败 taskId=%s err=%v", task.TaskID, err)
		return
	}

	// 按任务类型执行
	var execErr error
	switch execTask.Type {
	case "FULL_CRAWL":
		execErr = p.handleSpiderTask(ctx, execTask)
	case "PUNCH_CARD":
		execErr = p.handlePunchCardTask(ctx, execTask)
	case "EMPTY_CLASSROOM":
		execErr = p.handleEmptyClassroomTask(ctx, execTask)
	case "GRADES":
		execErr = p.handleGradesTask(ctx, execTask)
	default:
		log.Printf("[Worker] 未知任务类型 taskId=%s type=%s", execTask.TaskID, execTask.Type)
		_ = p.store.Ack(ctx, priority, msg.ID)
		return
	}

	if execErr != nil {
		p.failTask(ctx, priority, msg.ID, task, execErr.Error())
		return
	}

	// 成功，确认消息
	log.Printf("[Worker] 任务处理成功 taskId=%s", task.TaskID)
	_ = p.store.Ack(ctx, priority, msg.ID)
}

// failTask 统一失败处理：入队死信队列，然后确认原消息。
func (p *Pool) failTask(ctx context.Context, priority, msgID string, task model.Task, reason string) {
	log.Printf("[Worker] 任务失败，转入死信队列 taskId=%s reason=%s", task.TaskID, reason)
	if err := p.dlq.Enqueue(ctx, task, reason); err != nil {
		log.Printf("[Worker] 死信队列入队失败 taskId=%s err=%v", task.TaskID, err)
		// DLQ 写入失败时不能确认原消息，保留在 PEL 中交由僵尸恢复机制重试。
		return
	}
	if err := p.store.Ack(ctx, priority, msgID); err != nil {
		// 入队已成功但 ACK 失败时原消息仍会留在 PEL；记录错误，避免误以为已完成。
		log.Printf("[Worker] 原任务 ACK 失败 taskId=%s priority=%s msgID=%s err=%v", task.TaskID, priority, msgID, err)
	}
}

func (p *Pool) handleSpiderTask(ctx context.Context, task model.Task) error {
	log.Printf("[Worker] 开始爬取 taskId=%s", task.TaskID)
	out, err := p.runner.RunCrawl(ctx, task)
	if err != nil {
		return fmt.Errorf("爬取失败: %w", err)
	}

	var spiderData model.SpiderData
	if err := convertAnyToStruct(out.Data, &spiderData); err != nil {
		return fmt.Errorf("爬取结果解析失败: %w", err)
	}
	callbackPayload := spiderData.ToCallbackPayload()

	callbackURL := task.CallbackURL
	if callbackURL == "" {
		callbackURL = p.cfg.JavaCallbackURL
	}

	log.Printf("[Worker] 开始回调 taskId=%s url=%s", task.TaskID, callbackURL)
	if err := p.retryCallback(ctx, callbackURL, callbackPayload); err != nil {
		return fmt.Errorf("回调失败: %w", err)
	}
	log.Printf("[Worker] 回调成功 taskId=%s", task.TaskID)
	return nil
}

func (p *Pool) handlePunchCardTask(ctx context.Context, task model.Task) error {
	log.Printf("[Worker] 开始打卡 taskId=%s studentId=%s", task.TaskID, task.StudentID)
	out, err := p.runner.RunCheckin(ctx, task, p.cfg.CheckinScript, p.cfg.CheckinTimeout)
	if err != nil {
		_ = p.retryPunchCallback(ctx, task.StudentID, false)
		return fmt.Errorf("打卡失败: %w", err)
	}

	callbackURL := task.CallbackURL
	if callbackURL == "" {
		callbackURL = p.cfg.PunchCallbackURL
	}
	if err := p.retryPunchCallback(ctx, task.StudentID, true); err != nil {
		return fmt.Errorf("打卡回调失败: %w", err)
	}
	log.Printf("[Worker] 打卡成功 taskId=%s message=%s", task.TaskID, out.Message)
	return nil
}

func (p *Pool) handleEmptyClassroomTask(ctx context.Context, task model.Task) error {
	log.Printf("[Worker] 开始查询空教室 taskId=%s", task.TaskID)
	out, err := p.runner.RunEmptyClassroom(ctx, task)
	if err != nil {
		return fmt.Errorf("空教室查询失败: %w", err)
	}

	var payload model.EmptyClassroomPayload
	if err := convertAnyToStruct(out.Data, &payload); err != nil {
		return fmt.Errorf("空教室结果解析失败: %w", err)
	}

	callbackURL := task.CallbackURL
	if callbackURL == "" {
		callbackURL = p.cfg.EmptyClassroomCallbackURL
	}

	log.Printf("[Worker] 开始空教室回调 taskId=%s url=%s", task.TaskID, callbackURL)
	if err := p.retryEmptyClassroomCallback(ctx, callbackURL, payload); err != nil {
		return fmt.Errorf("空教室回调失败: %w", err)
	}
	log.Printf("[Worker] 空教室回调成功 taskId=%s", task.TaskID)
	return nil
}

func (p *Pool) handleGradesTask(ctx context.Context, task model.Task) error {
	log.Printf("[Worker] 开始查询成绩 taskId=%s", task.TaskID)
	out, err := p.runner.RunGrades(ctx, task)
	if err != nil {
		return fmt.Errorf("成绩查询失败: %w", err)
	}

	var payload model.GradesPayload
	if err := convertAnyToStruct(out.Data, &payload); err != nil {
		return fmt.Errorf("成绩结果解析失败: %w", err)
	}

	callbackURL := task.CallbackURL
	if callbackURL == "" {
		callbackURL = p.cfg.GradesCallbackURL
	}

	log.Printf("[Worker] 开始成绩回调 taskId=%s url=%s", task.TaskID, callbackURL)
	if err := p.retryGradesCallback(ctx, callbackURL, payload); err != nil {
		return fmt.Errorf("成绩回调失败: %w", err)
	}
	log.Printf("[Worker] 成绩回调成功 taskId=%s", task.TaskID)
	return nil
}

// convertAnyToStruct 将 any 类型（通常来自 JSON 反序列化后的 map[string]interface{}）转换为具体结构体
func convertAnyToStruct(src any, dst any) error {
	bytes, err := json.Marshal(src)
	if err != nil {
		return fmt.Errorf("序列化源数据失败: %w", err)
	}
	if err := json.Unmarshal(bytes, dst); err != nil {
		return fmt.Errorf("反序列化目标结构体失败: %w", err)
	}
	return nil
}

// retryPunchCallback 重试打卡回调
func (p *Pool) retryPunchCallback(ctx context.Context, studentID string, success bool) error {
	var lastErr error
	for i := 0; i < 3; i++ {
		if err := p.javaClient.PunchCallback(ctx, p.cfg.PunchCallbackURL, studentID, success); err != nil {
			lastErr = err
			time.Sleep(time.Duration(i+1) * 2 * time.Second)
			continue
		}
		return nil
	}
	return lastErr
}

// retryCallback 重试 Java 回调
func (p *Pool) retryCallback(ctx context.Context, url string, payload model.CallbackPayload) error {
	var lastErr error
	for i := 0; i < 3; i++ {
		if err := p.javaClient.Callback(ctx, url, payload); err != nil {
			lastErr = err
			time.Sleep(time.Duration(i+1) * 2 * time.Second)
			continue
		}
		return nil
	}
	return lastErr
}

// retryEmptyClassroomCallback 重试空教室回调
func (p *Pool) retryEmptyClassroomCallback(ctx context.Context, url string, payload model.EmptyClassroomPayload) error {
	var lastErr error
	for i := 0; i < 3; i++ {
		if err := p.javaClient.EmptyClassroomCallback(ctx, url, payload); err != nil {
			lastErr = err
			time.Sleep(time.Duration(i+1) * 2 * time.Second)
			continue
		}
		return nil
	}
	return lastErr
}

// retryGradesCallback 重试成绩回调
func (p *Pool) retryGradesCallback(ctx context.Context, url string, payload model.GradesPayload) error {
	var lastErr error
	for i := 0; i < 3; i++ {
		if err := p.javaClient.GradesCallback(ctx, url, payload); err != nil {
			lastErr = err
			time.Sleep(time.Duration(i+1) * 2 * time.Second)
			continue
		}
		return nil
	}
	return lastErr
}

// acquireRateLimit 阻塞获取一个限流配额；ctx 取消时直接返回，消息保持 pending 由僵尸恢复机制重试。
func (p *Pool) acquireRateLimit(ctx context.Context) error {
	for {
		allowed, err := p.rateLimiter.Allow(ctx)
		if err != nil {
			return err
		}
		if allowed {
			return nil
		}
		wait := p.rateLimiter.WaitUntilNextWindow()
		log.Printf("[Worker] 触发限流，等待下一窗口 %v", wait)
		select {
		case <-time.After(wait):
			continue
		case <-ctx.Done():
			return ctx.Err()
		}
	}
}

// hostname 获取当前主机名
func hostname() string {
	h, _ := os.Hostname()
	return h
}
