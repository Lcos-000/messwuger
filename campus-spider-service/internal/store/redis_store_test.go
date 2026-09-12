package store

import (
	"context"
	"testing"
	"time"

	"campus-spider-service/internal/model"
	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
)

func TestRenewPendingRefreshesIdleTime(t *testing.T) {
	miniRedis, err := miniredis.Run()
	if err != nil {
		t.Fatalf("start miniredis: %v", err)
	}
	defer miniRedis.Close()

	client := redis.NewClient(&redis.Options{Addr: miniRedis.Addr()})
	defer client.Close()

	ctx := context.Background()
	streamStore := NewRedisStore(client, "campus:tasks", "workers")
	baseTime := time.Unix(1_700_000_000, 0)
	miniRedis.SetTime(baseTime)
	if err := streamStore.EnsureGroups(ctx); err != nil {
		t.Fatalf("ensure consumer groups: %v", err)
	}

	if err := streamStore.Enqueue(ctx, model.PriorityHigh, model.Task{TaskID: "task-1"}); err != nil {
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

	miniRedis.SetTime(baseTime.Add(10 * time.Second))
	before, err := streamStore.PendingDetail(ctx, model.PriorityHigh, "-", "+", 10)
	if err != nil {
		t.Fatalf("inspect pending message before renewal: %v", err)
	}
	if len(before) != 1 || before[0].Idle < 10*time.Second {
		t.Fatalf("expected message idle time to advance, got %#v", before)
	}

	if err := streamStore.RenewPending(ctx, model.PriorityHigh, "worker-1", messageID); err != nil {
		t.Fatalf("renew pending message: %v", err)
	}

	after, err := streamStore.PendingDetail(ctx, model.PriorityHigh, "-", "+", 10)
	if err != nil {
		t.Fatalf("inspect pending message after renewal: %v", err)
	}
	if len(after) != 1 {
		t.Fatalf("expected one pending message after renewal, got %#v", after)
	}
	if after[0].Consumer != "worker-1" {
		t.Fatalf("renewal changed consumer to %q", after[0].Consumer)
	}
	if after[0].Idle >= before[0].Idle {
		t.Fatalf("expected renewal to reduce idle time: before=%s after=%s", before[0].Idle, after[0].Idle)
	}
}
