package main

import (
	"context"
	"crypto/subtle"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"campus-spider-service/internal/client"
	"campus-spider-service/internal/config"
	"campus-spider-service/internal/crypto"
	"campus-spider-service/internal/model"
	"campus-spider-service/internal/spider"
	"campus-spider-service/internal/store"
	"campus-spider-service/internal/worker"

	"github.com/redis/go-redis/v9"
)

// App 是一个应用实例
type App struct {
	cfg             config.Config
	store           *store.RedisStore
	idempotency     *store.IdempotencyStore
	scheduler       *worker.PriorityScheduler
	rateLimiter     *store.RateLimiter
	dlq             *store.DLQ
	zombieRecoverer *store.ZombieRecoverer
	worker          *worker.Pool
	spider          *spider.Runner
	java            *client.JavaClient
	httpSrv         *http.Server
}

func main() {
	cfg := config.Load()
	if cfg.APIToken == "" {
		log.Fatal("SPIDER_API_TOKEN 未配置，拒绝启动未鉴权的任务 API")
	}
	if cfg.YMToken == "" {
		log.Fatal("YM_TOKEN 未配置，拒绝启动缺少验证码服务凭据的爬虫")
	}

	rdb := redis.NewClient(&redis.Options{
		Addr:     cfg.RedisAddr,
		Password: cfg.RedisPassword,
		DB:       cfg.RedisDB,
	})

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := rdb.Ping(ctx).Err(); err != nil {
		log.Fatalf("redis 连接失败: %v", err)
	}

	// 多优先级 Stream 存储
	st := store.NewRedisStore(rdb, cfg.TaskStream, cfg.TaskGroup)
	if err := st.EnsureGroups(context.Background()); err != nil {
		log.Fatalf("创建 Redis Stream Group 失败: %v", err)
	}

	// 幂等去重
	idempotency := store.NewIdempotencyStore(rdb, "campus:spider:idempotency")

	// 优先级调度器：默认 high=3 medium=2 low=1，30 秒防饥饿
	scheduler := worker.NewPriorityScheduler(st, cfg.PriorityWeights, cfg.QueueStarveTimeout)

	// 固定窗口限流器：每分钟 10 次
	rateLimiter := store.NewRateLimiter(rdb, cfg.RateLimitKeyPrefix, cfg.RateLimitPerMinute, time.Minute)

	// 死信队列
	dlq := store.NewDLQ(rdb, cfg.DeadLetterStream, cfg.TaskGroup, cfg.MaxRetryCount, cfg.RetryBaseDelay, cfg.RetryMaxDelay)
	if err := dlq.EnsureGroup(context.Background()); err != nil {
		log.Fatalf("创建死信队列 Group 失败: %v", err)
	}

	// 僵尸消息恢复
	zombieRecoverer := store.NewZombieRecoverer(st, cfg.ZombieIdleTimeout, cfg.ZombieScanInterval)

	proxyPool := spider.NewProxyPool(cfg.ProxyPool)
	spiderRunner := spider.NewRunner(cfg.PythonPath, cfg.SpiderScript, cfg.SessionDir, cfg.SpiderTimeout, proxyPool, cfg.YMToken, cfg.YMType)
	javaClient := client.NewJavaClient(cfg.JavaInternalToken)

	app := &App{
		cfg:             cfg,
		store:           st,
		idempotency:     idempotency,
		scheduler:       scheduler,
		rateLimiter:     rateLimiter,
		dlq:             dlq,
		zombieRecoverer: zombieRecoverer,
		worker: worker.NewPool(
			cfg.WorkerConcurrency,
			st,
			scheduler,
			rateLimiter,
			dlq,
			spiderRunner,
			javaClient,
			cfg,
		),
		spider: spiderRunner,
		java:   javaClient,
	}

	// 后台全局上下文，用于协程统一退出
	bgCtx, bgCancel := context.WithCancel(context.Background())
	defer bgCancel()

	// 启动 worker
	go func() {
		if err := app.worker.Start(bgCtx); err != nil {
			log.Fatalf("worker 启动失败: %v", err)
		}
	}()

	// 启动死信队列重处理协程
	go func() {
		dlq.StartReprocessor(bgCtx, st, cfg.DeadLetterScanInterval)
	}()

	// 启动僵尸消息恢复协程
	go func() {
		zombieRecoverer.Start(bgCtx)
	}()

	// 构建路由 mux
	mux := http.NewServeMux()
	mux.HandleFunc("/health", app.healthHandler)
	mux.Handle("/api/v1/task/submit", app.requireAPIToken(http.HandlerFunc(app.submitHandler)))
	mux.Handle("/api/v1/task/punch-card", app.requireAPIToken(http.HandlerFunc(app.punchCardHandler)))
	mux.Handle("/api/v1/task/empty-classroom", app.requireAPIToken(http.HandlerFunc(app.emptyClassroomHandler)))
	mux.Handle("/api/v1/task/grades", app.requireAPIToken(http.HandlerFunc(app.gradesHandler)))

	// 启动 HTTP 服务
	app.httpSrv = &http.Server{
		Addr:         cfg.HTTPAddr,
		Handler:      mux,
		ReadTimeout:  10 * time.Second,
		WriteTimeout: 60 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		log.Printf("HTTP 服务启动: %s", cfg.HTTPAddr)
		if err := app.httpSrv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("HTTP 服务异常退出: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("收到退出信号，准备关闭...")

	// 先停止后台协程
	bgCancel()

	shutdownCtx, shutdownCancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer shutdownCancel()

	_ = app.httpSrv.Shutdown(shutdownCtx)
	_ = app.worker.Stop(shutdownCtx)
	log.Println("服务已退出")
}

// requireAPIToken 保护仅供 user-service 调用的任务提交接口。
func (a *App) requireAPIToken(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		provided := r.Header.Get("X-Spider-Token")
		if subtle.ConstantTimeCompare([]byte(provided), []byte(a.cfg.APIToken)) != 1 {
			writeJSON(w, http.StatusUnauthorized, model.APIResponse{
				Code:    http.StatusUnauthorized,
				Message: "unauthorized",
			})
			return
		}
		next.ServeHTTP(w, r)
	})
}

// healthHandler 处理健康检查请求
func (a *App) healthHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了健康检查接口")
	writeJSON(w, http.StatusOK, model.APIResponse{
		Code:    200,
		Message: "ok",
		Data:    map[string]string{"status": "ok"},
	})
}

// submitHandler 统一入口：根据 X-TYPE 分发到 VERIFY 或 FULL_CRAWL
func (a *App) submitHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeJSON(w, http.StatusMethodNotAllowed, model.APIResponse{
			Code:    405,
			Message: "method not allowed",
		})
		return
	}

	taskType := r.Header.Get("X-TYPE")
	switch taskType {
	case "VERIFY":
		a.validateCredentialsHandler(w, r)
	case "FULL_CRAWL":
		a.startTaskHandler(w, r)
	default:
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "无效的 X-TYPE，应为 VERIFY 或 FULL_CRAWL",
		})
	}
}

// validateCredentialsHandler 处理账号密码验证请求（同步，不走队列）
func (a *App) validateCredentialsHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了账号密码验证接口")

	studentID := r.Header.Get("X-Student-Id")
	password := r.Header.Get("X-Password")
	if studentID == "" || password == "" {
		fmt.Println("缺少 X-Student-Id 或 X-Password")
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "缺少 X-Student-Id 或 X-Password",
		})
		return
	}

	plainPassword, err := crypto.AesDecrypt(password, a.cfg.AesSecretKey)
	if err != nil {
		fmt.Println("密码解密失败:", err)
		writeJSON(w, http.StatusOK, model.APIResponse{
			Code:    401,
			Message: "密码解密失败: " + err.Error(),
			Data:    map[string]any{"valid": false},
		})
		return
	}

	ok, msg := a.spider.ValidateCredentials(r.Context(), studentID, plainPassword)
	if !ok {
		fmt.Println("账号密码验证失败:", msg)
		writeJSON(w, http.StatusOK, model.APIResponse{
			Code:    401,
			Message: msg,
			Data:    map[string]any{"valid": false},
		})
		return
	}
	fmt.Println("账号密码验证成功")
	writeJSON(w, http.StatusOK, model.APIResponse{
		Code:    200,
		Message: "账号密码验证成功",
		Data:    map[string]any{"valid": true},
	})
}

// startTaskHandler 处理全量爬取任务提交
func (a *App) startTaskHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了提交任务接口")

	studentID := r.Header.Get("X-Student-Id")
	password := r.Header.Get("X-Password")
	if studentID == "" || password == "" {
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "缺少 X-Student-Id 或 X-Password",
		})
		return
	}

	var req model.StartTaskRequest
	_ = decodeJSON(r, &req)

	if req.AcademicYear == "" {
		req.AcademicYear = a.cfg.DefaultAcademicYear
	}
	if req.Semester == "" {
		req.Semester = a.cfg.DefaultSemester
	}
	if req.CallbackURL == "" {
		req.CallbackURL = a.cfg.JavaCallbackURL
	}

	task := model.Task{
		TaskID:       taskIDFromHeader(r),
		Type:         "FULL_CRAWL",
		StudentID:    studentID,
		Password:     password,
		AcademicYear: req.AcademicYear,
		Semester:     req.Semester,
		CallbackURL:  req.CallbackURL,
		Status:       "queued",
		CreatedAt:    time.Now().Unix(),
		UpdatedAt:    time.Now().Unix(),
	}

	a.enqueueTask(w, r, task)
}

// punchCardHandler 处理打卡任务提交
func (a *App) punchCardHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了打卡任务提交接口")

	studentID := r.Header.Get("X-Student-Id")
	password := r.Header.Get("X-Password")
	if studentID == "" || password == "" {
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "缺少 X-Student-Id 或 X-Password",
		})
		return
	}

	task := model.Task{
		TaskID:      taskIDFromHeader(r),
		Type:        "PUNCH_CARD",
		StudentID:   studentID,
		Password:    password,
		CallbackURL: a.cfg.PunchCallbackURL,
		Status:      "queued",
		CreatedAt:   time.Now().Unix(),
		UpdatedAt:   time.Now().Unix(),
	}

	a.enqueueTask(w, r, task)
}

// emptyClassroomHandler 处理空教室任务提交
func (a *App) emptyClassroomHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了空教室任务提交接口")

	studentID := r.Header.Get("X-Student-Id")
	password := r.Header.Get("X-Password")
	if studentID == "" || password == "" {
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "缺少 X-Student-Id 或 X-Password",
		})
		return
	}

	var req model.StartTaskRequest
	_ = decodeJSON(r, &req)

	if req.AcademicYear == "" {
		req.AcademicYear = a.cfg.DefaultAcademicYear
	}
	if req.Semester == "" {
		req.Semester = a.cfg.DefaultSemester
	}
	if req.CallbackURL == "" {
		req.CallbackURL = a.cfg.EmptyClassroomCallbackURL
	}

	if req.DayOfWeek == "" || req.PeriodsMask == "" || req.WeeksMask == "" {
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "缺少 dayOfWeek / periodsMask / weeksMask",
		})
		return
	}

	task := model.Task{
		TaskID:       taskIDFromHeader(r),
		Type:         "EMPTY_CLASSROOM",
		StudentID:    studentID,
		Password:     password,
		AcademicYear: req.AcademicYear,
		Semester:     req.Semester,
		CallbackURL:  req.CallbackURL,
		DayOfWeek:    req.DayOfWeek,
		PeriodsMask:  req.PeriodsMask,
		WeeksMask:    req.WeeksMask,
		CampusID:     req.CampusID,
		Building:     req.Building,
		RoomType:     req.RoomType,
		Status:       "queued",
		CreatedAt:    time.Now().Unix(),
		UpdatedAt:    time.Now().Unix(),
	}

	a.enqueueTask(w, r, task)
}

// gradesHandler 处理成绩任务提交
func (a *App) gradesHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了成绩任务提交接口")

	studentID := r.Header.Get("X-Student-Id")
	password := r.Header.Get("X-Password")
	if studentID == "" || password == "" {
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "缺少 X-Student-Id 或 X-Password",
		})
		return
	}

	var req model.StartTaskRequest
	_ = decodeJSON(r, &req)

	if req.AcademicYear == "" {
		req.AcademicYear = a.cfg.DefaultAcademicYear
	}
	if req.Semester == "" {
		req.Semester = a.cfg.DefaultSemester
	}
	if req.CallbackURL == "" {
		req.CallbackURL = a.cfg.GradesCallbackURL
	}

	task := model.Task{
		TaskID:       taskIDFromHeader(r),
		Type:         "GRADES",
		StudentID:    studentID,
		Password:     password,
		AcademicYear: req.AcademicYear,
		Semester:     req.Semester,
		CallbackURL:  req.CallbackURL,
		Status:       "queued",
		CreatedAt:    time.Now().Unix(),
		UpdatedAt:    time.Now().Unix(),
	}

	a.enqueueTask(w, r, task)
}

// enqueueTask 统一任务入队逻辑：幂等校验 + 优先级路由 + 入队
func (a *App) enqueueTask(w http.ResponseWriter, r *http.Request, task model.Task) {
	if task.TaskID == "" {
		task.TaskID = model.NewTaskID()
	}

	// 幂等去重
	acquired, err := a.idempotency.TryAcquire(r.Context(), task.TaskID, a.cfg.IdempotencyTTL)
	if err != nil {
		log.Printf("[HTTP] 幂等校验失败 taskId=%s err=%v", task.TaskID, err)
		writeJSON(w, http.StatusInternalServerError, model.APIResponse{
			Code:    500,
			Message: "幂等校验失败: " + err.Error(),
		})
		return
	}
	if !acquired {
		log.Printf("[HTTP] 重复任务提交 taskId=%s", task.TaskID)
		writeJSON(w, http.StatusOK, model.APIResponse{
			Code:    200,
			Message: "任务已提交（重复请求）",
			Data: map[string]string{
				"taskId": task.TaskID,
			},
		})
		return
	}

	// 优先级
	priority := model.NormalizePriority(r.Header.Get("X-Priority"))
	task.Priority = priority

	// 入队
	if err := a.store.Enqueue(r.Context(), priority, task); err != nil {
		// 入队失败时释放幂等锁，允许重试
		_ = a.idempotency.Release(r.Context(), task.TaskID)
		log.Printf("[HTTP] 任务入队失败 taskId=%s err=%v", task.TaskID, err)
		writeJSON(w, http.StatusInternalServerError, model.APIResponse{
			Code:    500,
			Message: "任务入队失败: " + err.Error(),
		})
		return
	}

	log.Printf("[HTTP] 任务入队成功 taskId=%s priority=%s", task.TaskID, priority)
	writeJSON(w, http.StatusOK, model.APIResponse{
		Code:    200,
		Message: "任务已提交",
		Data: map[string]string{
			"taskId":   task.TaskID,
			"priority": priority,
		},
	})
}

// taskIDFromHeader 从 Header 读取 X-Task-Id，为空则返回空字符串（由 enqueueTask 生成）
func taskIDFromHeader(r *http.Request) string {
	return r.Header.Get("X-Task-Id")
}

// writeJSON 写入 JSON 响应
func writeJSON(w http.ResponseWriter, status int, resp model.APIResponse) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	_ = jsonEncode(w, resp)
}
