package worker

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"strings"
	"sync"
	"time"

	"campus-spider-service/internal/client"
	"campus-spider-service/internal/config"
	"campus-spider-service/internal/crypto"
	"campus-spider-service/internal/model"
	"campus-spider-service/internal/spider"

	"github.com/redis/go-redis/v9"
)

// 一个pool就是一个消费团队，在创建的时候就已经指定了group的名字和目标stream的名字
type Pool struct {
	concurrency int
	runner      *spider.Runner
	javaClient  *client.JavaClient
	rdb         *redis.Client
	stream      string
	group       string
	cfg         config.Config
}

// NewPool 创建一个新的消费队列池
func NewPool(concurrency int, rdb *redis.Client, stream, group string, runner *spider.Runner, javaClient *client.JavaClient, cfg config.Config) *Pool {
	return &Pool{
		concurrency: concurrency,
		runner:      runner,
		javaClient:  javaClient,
		rdb:         rdb,
		stream:      stream,
		group:       group,
		cfg:         cfg,
	}
}

// Start 启动任务消费队列池
func (p *Pool) Start(ctx context.Context) error {
	var wg sync.WaitGroup
	// 拿主机名
	consumerBase := hostname()
	if consumerBase == "" {
		consumerBase = "consumer"
	}

	for i := 0; i < p.concurrency; i++ {
		wg.Add(1)
		// 每个消费者都有一个唯一的名称，用于在 Redis 中标识
		// 消费者名称格式为 "consumer-索引"
		// 例如："consumer-0"
		// 例如："consumer-1"
		// 例如："consumer-2"
		// 例如："consumer-3"
		consumerName := fmt.Sprintf("%s-%d", consumerBase, i)
		go func(cn string) {
			defer wg.Done()
			// 启动消费队列
			p.consume(ctx, cn)
		}(consumerName)
	}

	wg.Wait()
	return nil
}

// consume 消费任务队列
func (p *Pool) consume(ctx context.Context, consumer string) {
	for {
		select {
		case <-ctx.Done():
			return
		default:
		}
		// 按照group消费任务队列，为group读
		res, err := p.rdb.XReadGroup(ctx, &redis.XReadGroupArgs{
			Group: p.group,
			// 消费者的名字，用于在 Redis 中标识，与group无关
			Consumer: consumer,
			// 目标stream的名字，">>" 表示从stream未消费的消息开始读取
			// 这里就只独立消费一个stream
			Streams: []string{p.stream, ">"},
			// 一次只读一条
			Count: 1,
			// 阻塞时间，假如stream没有消息，就阻塞5秒
			Block: 5 * time.Second,
		}).Result()

		// 处理错误
		if err != nil {
			if err == redis.Nil || strings.Contains(err.Error(), "NOGROUP") {
				time.Sleep(2 * time.Second)
				continue
			}
			if ctx.Err() != nil {
				return
			}
			time.Sleep(1 * time.Second)
			continue
		}

		// 取出所有stream的消息，处理每个消息
		for _, stream := range res {
			// 从stream中取出所有消息，处理每个消息
			for _, msg := range stream.Messages {
				p.handleMessage(ctx, consumer, msg)
			}
		}
		// 注意在这里当前情况下就只处理一个stream，也只处理一个消息
	}
}

// handleMessage 处理任务队列消息
func (p *Pool) handleMessage(ctx context.Context, consumer string, msg redis.XMessage) {
	// 将消息转换为任务，放进model.Task
	task := messageToTask(msg.Values)
	log.Printf("[Worker] 收到任务 taskId=%s type=%s studentId=%s", task.TaskID, task.Type, task.StudentID)

	// 缺失必要字段，直接返回
	if task.TaskID == "" || task.StudentID == "" || task.Password == "" {
		log.Printf("[Worker] 任务字段缺失，跳过")
		_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
		return
	}

	switch task.Type {
	case "PUNCH_CARD":
		p.handlePunchCardTask(ctx, msg, task)
	default:
		p.handleSpiderTask(ctx, msg, task)
	}
}

func (p *Pool) handleSpiderTask(ctx context.Context, msg redis.XMessage, task model.Task) {
	// 解密密码（Java 使用 AES 加密）
	plainPassword, err := crypto.AesDecrypt(task.Password, "@aes-secret-key#")
	if err != nil {
		log.Printf("[Worker] 爬虫任务密码解密失败 taskId=%s err=%v", task.TaskID, err)
		_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
		return
	}
	task.Password = plainPassword

	// 运行爬虫
	log.Printf("[Worker] 开始爬取 taskId=%s", task.TaskID)
	out, err := p.runner.RunCrawl(ctx, task)
	if err != nil {
		log.Printf("[Worker] 爬取失败 taskId=%s err=%v", task.TaskID, err)
		_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
		return
	}
	log.Printf("[Worker] 爬取成功 taskId=%s", task.TaskID)

	// 将SpiderData转换为CallbackPayload结构体
	callbackPayload := out.Data.ToCallbackPayload()

	// 回调
	log.Printf("[Worker] 开始回调 taskId=%s url=%s", task.TaskID, task.CallbackURL)
	if err := p.retryCallback(ctx, task.CallbackURL, callbackPayload); err != nil {
		log.Printf("[Worker] 回调失败 taskId=%s err=%v", task.TaskID, err)
		_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
		return
	}
	log.Printf("[Worker] 回调成功 taskId=%s", task.TaskID)
	// 回调成功，确认消息
	_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
}

func (p *Pool) handlePunchCardTask(ctx context.Context, msg redis.XMessage, task model.Task) {
	// 解密密码（Java 使用 AES 加密）
	plainPassword, err := crypto.AesDecrypt(task.Password, "@aes-secret-key#")
	if err != nil {
		log.Printf("[Worker] 打卡任务密码解密失败 taskId=%s err=%v", task.TaskID, err)
		_ = p.retryPunchCallback(ctx, task.StudentID, false)
		_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
		return
	}

	// 临时替换为明文密码
	task.Password = plainPassword

	log.Printf("[Worker] 开始打卡 taskId=%s studentId=%s", task.TaskID, task.StudentID)
	out, err := p.runner.RunCheckin(ctx, task, p.cfg.CheckinScript, p.cfg.CheckinTimeout)
	if err != nil {
		log.Printf("[Worker] 打卡失败 taskId=%s err=%v", task.TaskID, err)
		_ = p.retryPunchCallback(ctx, task.StudentID, false)
		_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
		return
	}
	log.Printf("[Worker] 打卡成功 taskId=%s message=%s", task.TaskID, out.Message)

	callbackURL := task.CallbackURL
	if callbackURL == "" {
		callbackURL = p.cfg.PunchCallbackURL
	}
	if err := p.retryPunchCallback(ctx, task.StudentID, true); err != nil {
		log.Printf("[Worker] 打卡回调失败 taskId=%s err=%v", task.TaskID, err)
		_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
		return
	}
	log.Printf("[Worker] 打卡回调成功 taskId=%s", task.TaskID)
	_ = p.rdb.XAck(ctx, p.stream, p.group, msg.ID).Err()
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

// retryCallback 回调Java服务
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

// messageToTask 将消息转换为任务，放进model.Task
func messageToTask(values map[string]any) model.Task {
	toString := func(v any) string {
		switch x := v.(type) {
		case string:
			return x
		case []byte:
			return string(x)
		default:
			b, _ := json.Marshal(x)
			return strings.Trim(string(b), `"`)
		}
	}

	return model.Task{
		TaskID:       toString(values["taskId"]),
		Type:         toString(values["type"]),
		StudentID:    toString(values["studentId"]),
		Password:     toString(values["password"]),
		AcademicYear: toString(values["academicYear"]),
		Semester:     toString(values["semester"]),
		CallbackURL:  toString(values["callbackUrl"]),
		Status:       toString(values["status"]),
	}
}

// hostname 获取当前主机名
func hostname() string {
	h, _ := os.Hostname()
	return h
}
