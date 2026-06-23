package config

import (
	"os"
	"strconv"
	"strings"
	"time"
)

type Config struct {
	HTTPAddr            string
	RedisAddr           string
	RedisPassword       string
	RedisDB             int
	TaskStream          string
	TaskGroup           string
	WorkerConcurrency   int
	JavaCallbackURL     string
	JavaInternalToken   string
	PythonPath          string
	SpiderScript        string
	SessionDir          string
	SpiderTimeout       time.Duration
	DefaultAcademicYear string
	DefaultSemester     string
	ProxyPool           []string
	CheckinScript       string
	CheckinTimeout      time.Duration
	PunchCallbackURL    string
}

func Load() Config {
	return Config{
		HTTPAddr:            env("HTTP_ADDR", "localhost:8082"),
		RedisAddr:           env("REDIS_ADDR", "127.0.0.1:6379"),
		RedisPassword:       env("REDIS_PASSWORD", ""),
		RedisDB:             envInt("REDIS_DB", 0),
		TaskStream:          env("TASK_STREAM", "campus:spider:tasks"),
		TaskGroup:           env("TASK_GROUP", "campus-spider-workers"),
		WorkerConcurrency:   envInt("WORKER_CONCURRENCY", 4),
		JavaCallbackURL:     env("JAVA_CALLBACK_URL", "http://localhost:8000/internal/api/v1/sync/student-data"),
		JavaInternalToken:   env("JAVA_INTERNAL_TOKEN", ""),
		PythonPath:          env("PYTHON_PATH", "python"),
		SpiderScript:        env("SPIDER_SCRIPT", "./scripts/spider_cli.py"),
		SessionDir:          env("SESSION_DIR", "./data/sessions"),
		SpiderTimeout:       envDurationMinutes("SPIDER_TIMEOUT_MINUTES", 20),
		DefaultAcademicYear: env("DEFAULT_ACADEMIC_YEAR", "2025"),
		DefaultSemester:     env("DEFAULT_SEMESTER", "12"),
		ProxyPool:           splitAndTrim(env("PROXY_POOL", "")),
		CheckinScript:       env("CHECKIN_SCRIPT", "./scripts/checkin_cli.py"),
		CheckinTimeout:      envDurationMinutes("CHECKIN_TIMEOUT_MINUTES", 5),
		PunchCallbackURL:    env("PUNCH_CALLBACK_URL", "http://localhost:8000/internal/api/v1/sync/punch-result"),
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
