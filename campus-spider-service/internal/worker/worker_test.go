package worker

import (
	"context"
	"testing"
	"time"

	"campus-spider-service/internal/model"
	"campus-spider-service/internal/store"
	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
)

func TestFailTaskKeepsMessagePendingWhenDLQEnqueueFails(t *testing.T) {
	miniRedis, err := miniredis.Run()
	if err != nil {
		t.Fatalf("start miniredis: %v", err)
	}
	defer miniRedis.Close()

	client := redis.NewClient(&redis.Options{Addr: miniRedis.Addr()})
	defer client.Close()

	ctx := context.Background()
	streamStore := store.NewRedisStore(client, "campus:tasks", "workers")
	if err := streamStore.EnsureGroups(ctx); err != nil {
		t.Fatalf("ensure consumer groups: %v", err)
	}

	task := model.Task{
		TaskID:    "task-1",
		Type:      "FULL_CRAWL",
		StudentID: "2025001",
		Password:  "encrypted-password",
		Priority:  model.PriorityHigh,
	}
	if err := streamStore.Enqueue(ctx, model.PriorityHigh, task); err != nil {
		t.Fatalf("enqueue task: %v", err)
	}
	streams, err := streamStore.ReadOne(ctx, model.PriorityHigh, "worker-1", -1)
	if err != nil {
		t.Fatalf("read task: %v", err)
	}
	if len(streams) != 1 || len(streams[0].Messages) != 1 {
		t.Fatalf("expected one pending message, got %#v", streams)
	}
	messageID := streams[0].Messages[0].ID

	// DLQ 使用不可达 Redis，模拟死信写入失败；原消息所在 Redis 仍然可用。
	failedDLQClient := redis.NewClient(&redis.Options{Addr: "127.0.0.1:1"})
	defer failedDLQClient.Close()
	dlq := store.NewDLQ(failedDLQClient, "campus:tasks:dlq", "workers", 3, time.Second, time.Minute)
	p := &Pool{store: streamStore, dlq: dlq}

	p.failTask(ctx, model.PriorityHigh, messageID, task, "crawl failed")

	pending, err := streamStore.PendingDetail(ctx, model.PriorityHigh, "-", "+", 10)
	if err != nil {
		t.Fatalf("inspect pending messages: %v", err)
	}
	if len(pending) != 1 || pending[0].ID != messageID {
		t.Fatalf("expected original message to remain pending, got %#v", pending)
	}
}
