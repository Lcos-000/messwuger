package store

import (
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

// fixedWindowLua 固定窗口限流 Lua 脚本：
// 当前窗口内未达上限则放行并计数，已达上限则拒绝。
const fixedWindowLua = `
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

local current = redis.call('get', key)
if current == false then
    redis.call('set', key, 1)
    redis.call('expire', key, window)
    return 1
end

current = tonumber(current)
if current >= limit then
    return 0
end

redis.call('incr', key)
return 1
`

// RateLimiter 基于 Redis 的固定窗口全局限流器，所有 Worker 共享配额。
type RateLimiter struct {
	rdb       *redis.Client
	keyPrefix string
	limit     int
	window    time.Duration
	script    *redis.Script
}

func NewRateLimiter(rdb *redis.Client, keyPrefix string, limit int, window time.Duration) *RateLimiter {
	return &RateLimiter{
		rdb:       rdb,
		keyPrefix: keyPrefix,
		limit:     limit,
		window:    window,
		script:    redis.NewScript(fixedWindowLua),
	}
}

func (r *RateLimiter) windowKey(now time.Time) string {
	// 按分钟对齐窗口
	windowStart := now.Unix() / int64(r.window.Seconds()) * int64(r.window.Seconds())
	return fmt.Sprintf("%s:%d", r.keyPrefix, windowStart)
}

// Allow 尝试获取一个配额。
// 返回 true 表示可以执行，false 表示当前窗口已用完。
func (r *RateLimiter) Allow(ctx context.Context) (bool, error) {
	key := r.windowKey(time.Now())
	ok, err := r.script.Run(ctx, r.rdb, []string{key}, r.limit, int(r.window.Seconds())).Bool()
	if err != nil {
		return false, err
	}
	return ok, nil
}

// WaitUntilNextWindow 返回距离下一个窗口开始还有多久
func (r *RateLimiter) WaitUntilNextWindow() time.Duration {
	now := time.Now()
	windowSeconds := int64(r.window.Seconds())
	elapsed := now.Unix() % windowSeconds
	return time.Duration(windowSeconds-elapsed) * time.Second
}
