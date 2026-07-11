package config

import (
	"os"
	"strconv"
	"strings"
	"time"
)

type Config struct {
	HTTPAddr                  string
	RedisAddr                 string
	RedisPassword             string
	RedisDB                   int
	TaskStream                string
	TaskGroup                 string
	WorkerConcurrency         int
	JavaCallbackURL           string
	JavaInternalToken         string
	AesSecretKey              string
	YMToken                   string
	YMType                    string
	PythonPath                string
	SpiderScript              string
	SessionDir                string
	SpiderTimeout             time.Duration
	DefaultAcademicYear       string
	DefaultSemester           string
	ProxyPool                 []string
	CheckinScript             string
	CheckinTimeout            time.Duration
	PunchCallbackURL          string
	EmptyClassroomCallbackURL string
	GradesCallbackURL         string

	// 优先级队列配置
	PriorityWeights map[string]int // high/medium/low 权重
	QueueStarveTimeout time.Duration // 防饥饿超时

	// 幂等去重
	IdempotencyTTL time.Duration // 任务幂等 key 过期时间

	// 死信队列
	DeadLetterStream      string
	DeadLetterScanInterval time.Duration
	MaxRetryCount         int
	RetryBaseDelay        time.Duration
	RetryMaxDelay         time.Duration

	// 僵尸消息恢复
	ZombieScanInterval time.Duration
	ZombieIdleTimeout  time.Duration // 消息 idle 多久视为僵尸

	// 限流
	RateLimitKeyPrefix string
	RateLimitPerMinute int
}

func Load() Config {
	return Config{
		HTTPAddr:                  env("HTTP_ADDR", "localhost:8082"),
		RedisAddr:                 env("REDIS_ADDR", "127.0.0.1:6379"),
		RedisPassword:             env("REDIS_PASSWORD", ""),
		RedisDB:                   envInt("REDIS_DB", 0),
		TaskStream:                env("TASK_STREAM", "campus:spider:tasks"),
		TaskGroup:                 env("TASK_GROUP", "campus-spider-workers"),
		WorkerConcurrency:         envInt("WORKER_CONCURRENCY", 4),
		JavaCallbackURL:           env("JAVA_CALLBACK_URL", "http://localhost:8000/internal/api/v1/sync/student-data"),
		JavaInternalToken:         env("JAVA_INTERNAL_TOKEN", ""),
		AesSecretKey:              env("AES_SECRET_KEY", "@aes-secret-key#"),
		YMToken:                   env("YM_TOKEN", ""),
		YMType:                    env("YM_TYPE", "10110"),
		PythonPath:                env("PYTHON_PATH", "python"),
		SpiderScript:              env("SPIDER_SCRIPT", "./scripts/spider_cli.py"),
		SessionDir:                env("SESSION_DIR", "./data/sessions"),
		SpiderTimeout:             envDurationMinutes("SPIDER_TIMEOUT_MINUTES", 20),
		DefaultAcademicYear:       env("DEFAULT_ACADEMIC_YEAR", "2025"),
		DefaultSemester:           env("DEFAULT_SEMESTER", "12"),
		ProxyPool:                 splitAndTrim(env("PROXY_POOL", "")),
		CheckinScript:             env("CHECKIN_SCRIPT", "./scripts/checkin_cli.py"),
		CheckinTimeout:            envDurationMinutes("CHECKIN_TIMEOUT_MINUTES", 5),
		PunchCallbackURL:          env("PUNCH_CALLBACK_URL", "http://localhost:8000/internal/api/v1/sync/punch-result"),
		EmptyClassroomCallbackURL: env("EMPTY_CLASSROOM_CALLBACK_URL", "http://localhost:8000/internal/api/v1/sync/empty-classroom"),
		GradesCallbackURL:         env("GRADES_CALLBACK_URL", "http://localhost:8000/internal/api/v1/sync/grades"),

		// 优先级队列：默认权重 high=3 medium=2 low=1
		PriorityWeights: parsePriorityWeights(env("PRIORITY_WEIGHTS", "high:3,medium:2,low:1")),
		QueueStarveTimeout: envDurationSeconds("QUEUE_STARVE_TIMEOUT_SECONDS", 30),

		// 幂等：默认 24 小时
		IdempotencyTTL: envDurationSeconds("IDEMPOTENCY_TTL_SECONDS", 86400),

		// 死信队列
		DeadLetterStream:       env("DEAD_LETTER_STREAM", "campus:spider:tasks:dlq"),
		DeadLetterScanInterval: envDurationSeconds("DEAD_LETTER_SCAN_INTERVAL_SECONDS", 30),
		MaxRetryCount:          envInt("MAX_RETRY_COUNT", 5),
		RetryBaseDelay:         envDurationSeconds("RETRY_BASE_DELAY_SECONDS", 10),
		RetryMaxDelay:          envDurationSeconds("RETRY_MAX_DELAY_SECONDS", 600),

		// 僵尸消息恢复
		ZombieScanInterval: envDurationSeconds("ZOMBIE_SCAN_INTERVAL_SECONDS", 60),
		ZombieIdleTimeout:  envDurationSeconds("ZOMBIE_IDLE_TIMEOUT_SECONDS", 300),

		// 限流：每分钟 10 次
		RateLimitKeyPrefix: env("RATE_LIMIT_KEY_PREFIX", "campus:spider:rate_limit"),
		RateLimitPerMinute: envInt("RATE_LIMIT_PER_MINUTE", 10),
	}
}

func env(key, def string) string {
	v := strings.TrimSpace(os.Getenv(key))
	if v == "" {
		return def
	}
	return v
}

func envInt(key string, def int) int {
	v := strings.TrimSpace(os.Getenv(key))
	if v == "" {
		return def
	}
	n, err := strconv.Atoi(v)
	if err != nil {
		return def
	}
	return n
}

func envDurationMinutes(key string, def int) time.Duration {
	return time.Duration(envInt(key, def)) * time.Minute
}

func envDurationSeconds(key string, def int) time.Duration {
	return time.Duration(envInt(key, def)) * time.Second
}

func parsePriorityWeights(s string) map[string]int {
	weights := map[string]int{
		"high":   3,
		"medium": 2,
		"low":    1,
	}
	if strings.TrimSpace(s) == "" {
		return weights
	}
	parts := strings.Split(s, ",")
	for _, p := range parts {
		kv := strings.SplitN(strings.TrimSpace(p), ":", 2)
		if len(kv) != 2 {
			continue
		}
		key := strings.TrimSpace(kv[0])
		val, err := strconv.Atoi(strings.TrimSpace(kv[1]))
		if err != nil || val <= 0 {
			continue
		}
		if _, ok := weights[key]; ok {
			weights[key] = val
		}
	}
	return weights
}

func splitAndTrim(s string) []string {
	if strings.TrimSpace(s) == "" {
		return nil
	}
	parts := strings.Split(s, ",")
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			out = append(out, p)
		}
	}
	return out
}
