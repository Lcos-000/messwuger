package store

import (
	"context"
	"fmt"
	"strings"
	"time"

	"campus-spider-service/internal/model"

	"github.com/redis/go-redis/v9"
)

// RedisStore 管理多优先级 Redis Stream 队列。
// 基础 stream key 为 cfg.TaskStream，实际队列分别为 {TaskStream}:high / :medium / :low。
type RedisStore struct {
	rdb        *redis.Client
	baseStream string
	group      string
	priorities []string
}

func NewRedisStore(rdb *redis.Client, baseStream, group string) *RedisStore {
	return &RedisStore{
		rdb:        rdb,
		baseStream: baseStream,
		group:      group,
		priorities: []string{model.PriorityHigh, model.PriorityMedium, model.PriorityLow},
	}
}

func (s *RedisStore) RDB() *redis.Client {
	return s.rdb
}

func (s *RedisStore) BaseStream() string {
	return s.baseStream
}

func (s *RedisStore) Group() string {
	return s.group
}

func (s *RedisStore) Priorities() []string {
	return s.priorities
}

// StreamFor 返回指定优先级对应的完整 Stream key
func (s *RedisStore) StreamFor(priority string) string {
	return fmt.Sprintf("%s:%s", s.baseStream, priority)
}

// EnsureGroups 为每个优先级 Stream 创建消费组；Stream 不存在时自动创建。
func (s *RedisStore) EnsureGroups(ctx context.Context) error {
	for _, p := range s.priorities {
		stream := s.StreamFor(p)
		err := s.rdb.XGroupCreateMkStream(ctx, stream, s.group, "0").Err()
		if err != nil && !strings.Contains(err.Error(), "BUSYGROUP") {
			return fmt.Errorf("create group for %s failed: %w", stream, err)
		}
	}
	return nil
}

// Enqueue 按优先级将任务写入对应 Stream
func (s *RedisStore) Enqueue(ctx context.Context, priority string, task model.Task) error {
	stream := s.StreamFor(priority)
	task.UpdatedAt = time.Now().Unix()
	_, err := s.rdb.XAdd(ctx, &redis.XAddArgs{
		Stream: stream,
		Values: task.ToMap(),
	}).Result()
	return err
}

// ReadOne 从指定优先级的 Stream 消费一条消息
func (s *RedisStore) ReadOne(ctx context.Context, priority, consumer string, block time.Duration) ([]redis.XStream, error) {
	return s.rdb.XReadGroup(ctx, &redis.XReadGroupArgs{
		Group:    s.group,
		Consumer: consumer,
		Streams:  []string{s.StreamFor(priority), ">"},
		Count:    1,
		Block:    block,
	}).Result()
}

// Ack 确认指定优先级 Stream 中的消息
func (s *RedisStore) Ack(ctx context.Context, priority, msgID string) error {
	return s.rdb.XAck(ctx, s.StreamFor(priority), s.group, msgID).Err()
}

// RenewPending 重置正在处理中的 Pending 消息 idle 时间，避免长任务被僵尸恢复器误认领。
func (s *RedisStore) RenewPending(ctx context.Context, priority, consumer, msgID string) error {
	_, err := s.rdb.XClaim(ctx, &redis.XClaimArgs{
		Stream:   s.StreamFor(priority),
		Group:    s.group,
		Consumer: consumer,
		MinIdle:  0,
		Messages: []string{msgID},
	}).Result()
	return err
}

// Pending 获取指定优先级 Stream 的 Pending 概览
func (s *RedisStore) Pending(ctx context.Context, priority string) (*redis.XPending, error) {
	return s.rdb.XPending(ctx, s.StreamFor(priority), s.group).Result()
}

// PendingDetail 获取 Pending 消息详情（用于僵尸恢复）
func (s *RedisStore) PendingDetail(ctx context.Context, priority, start, end string, count int64) ([]redis.XPendingExt, error) {
	return s.rdb.XPendingExt(ctx, &redis.XPendingExtArgs{
		Stream: s.StreamFor(priority),
		Group:  s.group,
		Start:  start,
		End:    end,
		Count:  count,
	}).Result()
}

// Claim 认领指定优先级的 Pending 消息
func (s *RedisStore) Claim(ctx context.Context, priority, consumer string, minIdle time.Duration, msgIDs ...string) ([]redis.XMessage, error) {
	return s.rdb.XClaim(ctx, &redis.XClaimArgs{
		Stream:   s.StreamFor(priority),
		Group:    s.group,
		Consumer: consumer,
		MinIdle:  minIdle,
		Messages: msgIDs,
	}).Result()
}
