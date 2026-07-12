package store

import (
	"context"
	"log"
	"time"
)

// ZombieRecoverer 负责扫描三个优先级 Stream 的 Pending Entries List，
// 认领并恢复长时间未被确认（Worker 崩溃导致）的僵尸消息。
type ZombieRecoverer struct {
	store        *RedisStore
	idleTimeout  time.Duration
	scanInterval time.Duration
}

func NewZombieRecoverer(store *RedisStore, idleTimeout, scanInterval time.Duration) *ZombieRecoverer {
	return &ZombieRecoverer{
		store:        store,
		idleTimeout:  idleTimeout,
		scanInterval: scanInterval,
	}
}

// Start 启动僵尸消息恢复协程
func (z *ZombieRecoverer) Start(ctx context.Context) {
	consumer := "zombie-recoverer"
	ticker := time.NewTicker(z.scanInterval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			for _, p := range z.store.Priorities() {
				z.recoverQueue(ctx, p, consumer)
			}
		}
	}
}

func (z *ZombieRecoverer) recoverQueue(ctx context.Context, priority, consumer string) {
	pending, err := z.store.PendingDetail(ctx, priority, "-", "+", 100)
	if err != nil {
		log.Printf("[Zombie] 获取 %s Pending 列表失败: %v", priority, err)
		return
	}

	var reclaimIDs []string
	for _, p := range pending {
		if p.Idle >= z.idleTimeout {
			reclaimIDs = append(reclaimIDs, p.ID)
		}
	}

	if len(reclaimIDs) == 0 {
		return
	}

	log.Printf("[Zombie] %s 发现 %d 条僵尸消息，准备恢复", priority, len(reclaimIDs))

	// 认领僵尸消息到 recoverer 消费者
	msgs, err := z.store.Claim(ctx, priority, consumer, z.idleTimeout, reclaimIDs...)
	if err != nil {
		log.Printf("[Zombie] 认领 %s 僵尸消息失败: %v", priority, err)
		return
	}

	for _, msg := range msgs {
		task := MessageToTask(msg.Values)
		log.Printf("[Zombie] 恢复 taskId=%s 到 %s", task.TaskID, priority)

		// 重新入队原优先级，并确认旧消息，避免重复恢复
		if err := z.store.Enqueue(ctx, priority, task); err != nil {
			log.Printf("[Zombie] taskId=%s 重新入队失败: %v", task.TaskID, err)
			continue
		}
		_ = z.store.Ack(ctx, priority, msg.ID)
	}
}
