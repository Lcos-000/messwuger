package worker

import (
	"context"
	"sync"
	"time"

	"campus-spider-service/internal/model"
	"campus-spider-service/internal/store"

	"github.com/redis/go-redis/v9"
)

// PriorityScheduler 管理多优先级队列的加权轮询与防饥饿调度。
type PriorityScheduler struct {
	store         *store.RedisStore
	weights       map[string]int
	starveTimeout time.Duration

	mu           sync.Mutex
	lastConsume  map[string]time.Time
	roundRobin   []string // 按权重展开的队列序列，如 [high, high, high, medium, medium, low]
	pos          int
}

func NewPriorityScheduler(store *store.RedisStore, weights map[string]int, starveTimeout time.Duration) *PriorityScheduler {
	if weights == nil {
		weights = map[string]int{
			model.PriorityHigh:   3,
			model.PriorityMedium: 2,
			model.PriorityLow:    1,
		}
	}

	s := &PriorityScheduler{
		store:         store,
		weights:       weights,
		starveTimeout: starveTimeout,
		lastConsume:   make(map[string]time.Time),
	}
	s.rebuildRoundRobin()
	now := time.Now()
	for _, p := range store.Priorities() {
		s.lastConsume[p] = now
	}
	return s
}

// rebuildRoundRobin 根据权重生成轮询序列。
// 例如 high=3, medium=2, low=1 生成 [high, high, high, medium, medium, low]
func (s *PriorityScheduler) rebuildRoundRobin() {
	seq := make([]string, 0)
	for _, p := range s.store.Priorities() {
		w := s.weights[p]
		if w <= 0 {
			w = 1
		}
		for i := 0; i < w; i++ {
			seq = append(seq, p)
		}
	}
	s.roundRobin = seq
}

// ConsumeOne 尝试按调度策略消费一条消息，找到后调用 handle 处理。
// 返回 true 表示成功消费到一条消息。
func (s *PriorityScheduler) ConsumeOne(ctx context.Context, consumer string, handle func(priority string, msg redis.XMessage)) (bool, error) {
	// 1. 防饥饿：如果某个队列超过 starveTimeout 未消费，优先尝试消费它
	if p := s.starvingQueue(); p != "" {
		msg, err := s.readOne(ctx, p, consumer, 500*time.Millisecond)
		if err == nil && msg != nil {
			s.markConsumed(p)
			handle(p, *msg)
			return true, nil
		}
	}

	// 2. 加权轮询：按权重序列依次尝试每个队列
	for i := 0; i < len(s.roundRobin); i++ {
		p := s.nextInRoundRobin()
		msg, err := s.readOne(ctx, p, consumer, 300*time.Millisecond)
		if err != nil {
			continue
		}
		if msg == nil {
			continue
		}

		s.markConsumed(p)
		handle(p, *msg)
		return true, nil
	}

	return false, nil
}

// starvingQueue 返回超过 starveTimeout 未被消费的队列；没有则返回空字符串。
func (s *PriorityScheduler) starvingQueue() string {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now()
	for _, p := range s.store.Priorities() {
		last, ok := s.lastConsume[p]
		if !ok {
			return p
		}
		if now.Sub(last) >= s.starveTimeout {
			return p
		}
	}
	return ""
}

func (s *PriorityScheduler) nextInRoundRobin() string {
	s.mu.Lock()
	defer s.mu.Unlock()

	if len(s.roundRobin) == 0 {
		return model.PriorityHigh
	}
	p := s.roundRobin[s.pos]
	s.pos = (s.pos + 1) % len(s.roundRobin)
	return p
}

func (s *PriorityScheduler) markConsumed(priority string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.lastConsume[priority] = time.Now()
}

// readOne 从指定队列读取一条消息；没有消息时返回 nil。
func (s *PriorityScheduler) readOne(ctx context.Context, priority, consumer string, block time.Duration) (*redis.XMessage, error) {
	streams, err := s.store.ReadOne(ctx, priority, consumer, block)
	if err != nil {
		return nil, err
	}
	for _, stream := range streams {
		for _, msg := range stream.Messages {
			return &msg, nil
		}
	}
	return nil, nil
}
