package store

import (
	"context"
	"testing"
	"time"

	"campus-spider-service/internal/model"
	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
)

func TestEnqueueAndAckKeepsSourcePendingWhenDLQWriteFails(t *testing.T) {
	miniRedis, err := miniredis.Run()
	if err != nil {
		t.Fatalf("start miniredis: %v", err)
	}
	defer miniRedis.Close()

	client := redis.NewClient(&redis.Options{Addr: miniRedis.Addr()})
	defer client.Close()

	ctx := context.Background()
	streamStore := NewRedisStore(client, "campus:tasks", "workers")
	if err := streamStore.EnsureGroups(ctx); err != nil {
		t.Fatalf("ensure consumer groups: %v", err)
	}
	task := model.Task{TaskID: "task-1", Type: "FULL_CRAWL", StudentID: "2025001", Password: "encrypted", Priority: model.PriorityHigh}
	if err := streamStore.Enqueue(ctx, model.PriorityHigh, task); err != nil {
		t.Fatalf("enqueue task: %v", err)
	}
	streams, err := streamStore.ReadOne(ctx, model.PriorityHigh, "worker-1", -1)
	if err != nil || len(streams) != 1 || len(streams[0].Messages) != 1 {
		t.Fatalf("read pending task: streams=%#v err=%v", streams, err)
	}
	messageID := streams[0].Messages[0].ID

	if err := client.Set(ctx, "campus:tasks:dlq", "wrong-type", 0).Err(); err != nil {
		t.Fatalf("prepare broken DLQ stream: %v", err)
	}
	dlq := NewDLQ(client, "campus:tasks:dlq", "workers", 3, time.Second, time.Minute)
	if err := dlq.EnqueueAndAck(ctx, streamStore, model.PriorityHigh, messageID, task, "failed"); err == nil {
		t.Fatal("expected DLQ write failure")
	}

	pending, err := streamStore.PendingDetail(ctx, model.PriorityHigh, "-", "+", 10)
	if err != nil {
		t.Fatalf("inspect pending messages: %v", err)
	}
	if len(pending) != 1 || pending[0].ID != messageID {
		t.Fatalf("source message was not preserved: %#v", pending)
	}
}

func TestEnqueueAndAckSkipsSourceThatIsNoLongerPending(t *testing.T) {
	miniRedis, err := miniredis.Run()
	if err != nil {
		t.Fatalf("start miniredis: %v", err)
	}
	defer miniRedis.Close()

	client := redis.NewClient(&redis.Options{Addr: miniRedis.Addr()})
	defer client.Close()

	ctx := context.Background()
	streamStore := NewRedisStore(client, "campus:tasks", "workers")
	if err := streamStore.EnsureGroups(ctx); err != nil {
		t.Fatalf("ensure consumer groups: %v", err)
	}
	task := model.Task{TaskID: "task-acked", Type: "FULL_CRAWL", StudentID: "2025003", Password: "encrypted", Priority: model.PriorityHigh}
	if err := streamStore.Enqueue(ctx, model.PriorityHigh, task); err != nil {
		t.Fatalf("enqueue task: %v", err)
	}
	streams, err := streamStore.ReadOne(ctx, model.PriorityHigh, "worker-1", -1)
	if err != nil || len(streams) != 1 || len(streams[0].Messages) != 1 {
		t.Fatalf("read pending task: streams=%#v err=%v", streams, err)
	}
	messageID := streams[0].Messages[0].ID
	if err := streamStore.Ack(ctx, model.PriorityHigh, messageID); err != nil {
		t.Fatalf("ack source task: %v", err)
	}

	dlq := NewDLQ(client, "campus:tasks:dlq", "workers", 3, time.Second, time.Minute)
	if err := dlq.EnqueueAndAck(ctx, streamStore, model.PriorityHigh, messageID, task, "failed"); err == nil {
		t.Fatal("expected already-acked source to be rejected")
	}
	if messages, err := client.XRange(ctx, "campus:tasks:dlq", "-", "+").Result(); err != nil {
		t.Fatalf("inspect DLQ: %v", err)
	} else if len(messages) != 0 {
		t.Fatalf("already-acked source was copied to DLQ: %#v", messages)
	}
}

func TestReprocessKeepsDLQMessageWhenTargetWriteFails(t *testing.T) {
	miniRedis, err := miniredis.Run()
	if err != nil {
		t.Fatalf("start miniredis: %v", err)
	}
	defer miniRedis.Close()

	client := redis.NewClient(&redis.Options{Addr: miniRedis.Addr()})
	defer client.Close()

	ctx := context.Background()
	streamStore := NewRedisStore(client, "campus:tasks", "workers")
	dlq := NewDLQ(client, "campus:tasks:dlq", "workers", 3, time.Second, time.Minute)
	task := model.Task{
		TaskID:       "task-2",
		Type:         "FULL_CRAWL",
		StudentID:    "2025002",
		Password:     "encrypted",
		Priority:     model.PriorityHigh,
		RetryCount:   1,
		LastFailedAt: time.Now().Add(-time.Minute).Unix(),
	}
	messageID, err := client.XAdd(ctx, &redis.XAddArgs{Stream: "campus:tasks:dlq", Values: task.ToMap()}).Result()
	if err != nil {
		t.Fatalf("enqueue DLQ task: %v", err)
	}
	if err := client.Set(ctx, streamStore.StreamFor(model.PriorityHigh), "wrong-type", 0).Err(); err != nil {
		t.Fatalf("prepare broken target stream: %v", err)
	}

	dlq.handleMessage(ctx, streamStore, redis.XMessage{ID: messageID, Values: task.ToMap()})

	messages, err := client.XRange(ctx, "campus:tasks:dlq", messageID, messageID).Result()
	if err != nil {
		t.Fatalf("inspect DLQ message: %v", err)
	}
	if len(messages) != 1 || messages[0].ID != messageID {
		t.Fatalf("DLQ message was deleted after target write failure: %#v", messages)
	}
}
