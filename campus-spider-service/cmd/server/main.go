package main

import (
	"context"
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
	cfg     config.Config
	store   *store.RedisStore
	worker  *worker.Pool
	spider  *spider.Runner
	java    *client.JavaClient
	httpSrv *http.Server
}

// main 函数
func main() {
	cfg := config.Load()

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

	st := store.NewRedisStore(rdb, cfg.TaskStream, cfg.TaskGroup)
	if err := st.EnsureGroup(context.Background()); err != nil {
		log.Fatalf("创建 Redis Stream Group 失败: %v", err)
	}

	proxyPool := spider.NewProxyPool(cfg.ProxyPool)
	spiderRunner := spider.NewRunner(cfg.PythonPath, cfg.SpiderScript, cfg.SessionDir, cfg.SpiderTimeout, proxyPool)
	javaClient := client.NewJavaClient(cfg.JavaInternalToken)

	app := &App{
		cfg:    cfg,
		store:  st,
		worker: worker.NewPool(cfg.WorkerConcurrency, rdb, cfg.TaskStream, cfg.TaskGroup, spiderRunner, javaClient, cfg),
		spider: spiderRunner,
		java:   javaClient,
	}

	// 启动 worker
	go func() {
		if err := app.worker.Start(context.Background()); err != nil {
			log.Fatalf("worker 启动失败: %v", err)
		}
	}()
	// 构建路由 mux
	mux := http.NewServeMux()
	mux.HandleFunc("/health", app.healthHandler)
	mux.HandleFunc("/api/v1/task/submit", app.submitHandler)
	mux.HandleFunc("/api/v1/task/punch-card", app.punchCardHandler)

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
	// 监听 SIGINT 和 SIGTERM 信号
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	// 优雅关闭
	<-quit
	log.Println("收到退出信号，准备关闭...")

	shutdownCtx, shutdownCancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer shutdownCancel()

	_ = app.httpSrv.Shutdown(shutdownCtx)
	log.Println("服务已退出")
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

// startTaskHandler 处理提交任务请求
func (a *App) startTaskHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了提交任务接口")
	if r.Method != http.MethodPost {
		writeJSON(w, http.StatusMethodNotAllowed, model.APIResponse{
			Code:    405,
			Message: "method not allowed",
		})
		return
	}

	// 学生账号
	studentID := r.Header.Get("X-Student-Id")
	// 学生密码
	password := r.Header.Get("X-Password")
	// 校验
	if studentID == "" || password == "" {
		fmt.Println("缺少 X-Student-Id 或 X-Password")
		writeJSON(w, http.StatusBadRequest, model.APIResponse{
			Code:    400,
			Message: "缺少 X-Student-Id 或 X-Password",
		})
		return
	}

	// 解析请求体
	var req model.StartTaskRequest
	_ = decodeJSON(r, &req) // body 可空

	// 学年值
	if req.AcademicYear == "" {
		req.AcademicYear = a.cfg.DefaultAcademicYear
	}
	// 学期值
	if req.Semester == "" {
		req.Semester = a.cfg.DefaultSemester
	}
	// 回调 URL
	if req.CallbackURL == "" {
		req.CallbackURL = a.cfg.JavaCallbackURL
	}

	// 创建任务
	task := model.Task{
		TaskID: model.NewTaskID(),
		Type:   "FULL_CRAWL",
		// 学生账号密码
		StudentID: studentID,
		Password:  password,
		// 学年学期
		AcademicYear: req.AcademicYear,
		Semester:     req.Semester,
		// 回调URL
		CallbackURL: req.CallbackURL,
		// 任务状态
		Status: "queued",
		// 时间戳
		CreatedAt: time.Now().Unix(),
		UpdatedAt: time.Now().Unix(),
	}

	// 入队任务
	if err := a.store.Enqueue(r.Context(), task); err != nil {
		fmt.Println("任务入队失败:", err)
		writeJSON(w, http.StatusInternalServerError, model.APIResponse{
			Code:    500,
			Message: "任务入队失败: " + err.Error(),
		})
		return
	}
	fmt.Println("任务入队成功:", task.TaskID)
	// 返回任务 ID
	writeJSON(w, http.StatusOK, model.APIResponse{
		Code:    200,
		Message: "爬虫任务已提交",
		Data: map[string]string{
			"taskId": task.TaskID,
		},
	})
}

// validateCredentialsHandler 处理账号密码验证请求
func (a *App) validateCredentialsHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了账号密码验证接口")
	if r.Method != http.MethodPost {
		fmt.Println("method not allowed")
		writeJSON(w, http.StatusMethodNotAllowed, model.APIResponse{
			Code:    405,
			Message: "method not allowed",
		})
		return
	}

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

	// 解密密码（Java 端使用 AES 加密）
	plainPassword, err := crypto.AesDecrypt(password, "@aes-secret-key#")
	if err != nil {
		fmt.Println("密码解密失败:", err)
		writeJSON(w, http.StatusOK, model.APIResponse{
			Code:    401,
			Message: "密码解密失败: " + err.Error(),
			Data:    map[string]any{"valid": false},
		})
		return
	}

	// 验证账号密码
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

// punchCardHandler 处理打卡任务提交请求
func (a *App) punchCardHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("访问了打卡任务提交接口")
	if r.Method != http.MethodPost {
		writeJSON(w, http.StatusMethodNotAllowed, model.APIResponse{
			Code:    405,
			Message: "method not allowed",
		})
		return
	}

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

	// 创建打卡任务
	task := model.Task{
		TaskID:      model.NewTaskID(),
		Type:        "PUNCH_CARD",
		StudentID:   studentID,
		Password:    password,
		CallbackURL: a.cfg.PunchCallbackURL,
		Status:      "queued",
		CreatedAt:   time.Now().Unix(),
		UpdatedAt:   time.Now().Unix(),
	}

	if err := a.store.Enqueue(r.Context(), task); err != nil {
		fmt.Println("打卡任务入队失败:", err)
		writeJSON(w, http.StatusInternalServerError, model.APIResponse{
			Code:    500,
			Message: "打卡任务入队失败: " + err.Error(),
		})
		return
	}
	fmt.Println("打卡任务入队成功:", task.TaskID)
	writeJSON(w, http.StatusOK, model.APIResponse{
		Code:    200,
		Message: "打卡任务已提交",
		Data: map[string]string{
			"taskId": task.TaskID,
		},
	})
}

// writeJSON 写入 JSON 响应
func writeJSON(w http.ResponseWriter, status int, resp model.APIResponse) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	// 编码 JSON 响应
	_ = jsonEncode(w, resp)
}
