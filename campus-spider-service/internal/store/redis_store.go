package store

import (
	"context"
	"strings"
	"time"

	"campus-spider-service/internal/model"

	"github.com/redis/go-redis/v9"
)

type RedisStore struct {
	rdb    *redis.Client
	stream string
	group  string
}

func NewRedisStore(rdb *redis.Client, stream, group string) *RedisStore {
	return &RedisStore{
		rdb:    rdb,
		stream: stream,
		group:  group,
	}
}

func (s *RedisStore) EnsureGroup(ctx context.Context) error {
	err := s.rdb.XGroupCreateMkStream(ctx, s.stream, s.group, "0").Err()
	if err != nil && strings.Contains(err.Error(), "BUSYGROUP") {
		return nil
	}
	return err
}

func (s *RedisStore) RDB() *redis.Client {
	return s.rdb
}

func (s *RedisStore) Stream() string {
	return s.stream
}

func (s *RedisStore) Group() string {
	return s.group
}

func (s *RedisStore) Enqueue(ctx context.Context, task model.Task) error {
	task.UpdatedAt = time.Now().Unix()
	_, err := s.rdb.XAdd(ctx, &redis.XAddArgs{
		Stream: s.stream,
		Values: task.ToMap(),
	}).Result()
	return err
}
