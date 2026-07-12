package store

import (
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

// IdempotencyStore 基于 Redis 实现任务幂等去重。
// 用于防止 Java 侧因超时/重试导致同一 taskId 被重复提交。
type IdempotencyStore struct {
	rdb        *redis.Client
	keyPrefix  string
}

func NewIdempotencyStore(rdb *redis.Client, keyPrefix string) *IdempotencyStore {
	return &IdempotencyStore{
		rdb:       rdb,
		keyPrefix: keyPrefix,
	}
}

func (s *IdempotencyStore) key(taskID string) string {
	return fmt.Sprintf("%s:%s", s.keyPrefix, taskID)
}

// TryAcquire 尝试获取任务执行权。
// 返回 true 表示该 taskId 首次出现，允许执行；
// 返回 false 表示该 taskId 已存在，属于重复提交，应拒绝。
func (s *IdempotencyStore) TryAcquire(ctx context.Context, taskID string, ttl time.Duration) (bool, error) {
	if taskID == "" {
		// 没有 taskId 不做幂等控制，直接放行
		return true, nil
	}
	key := s.key(taskID)
	ok, err := s.rdb.SetNX(ctx, key, "1", ttl).Result()
	if err != nil {
		return false, err
	}
	return ok, nil
}

// Release 主动释放幂等锁（通常不需要，靠 TTL 自动过期即可）
func (s *IdempotencyStore) Release(ctx context.Context, taskID string) error {
	if taskID == "" {
		return nil
	}
	return s.rdb.Del(ctx, s.key(taskID)).Err()
}
