package store

import (
	"context"
	"fmt"
	"log"
	"time"

	"campus-spider-service/internal/model"

	"github.com/redis/go-redis/v9"
)

// DLQ 死信队列：任务失败时进入该 Stream，由独立协程扫描并按指数退避重新入队。
// 采用 XRANGE + XDEL 简化扫描，避免 Consumer Group 中 Pending 消息难以重复扫描的问题。
type DLQ struct {
	rdb       *redis.Client
	stream    string
	maxRetry  int
	baseDelay time.Duration
	maxDelay  time.Duration
}

var enqueueAndAckScript = redis.NewScript(`
local field_count = tonumber(ARGV[3])
local xadd_args = {'*'}
for i = 1, field_count * 2 do
    xadd_args[#xadd_args + 1] = ARGV[3 + i]
end

local pending = redis.pcall('XPENDING', KEYS[2], ARGV[1], ARGV[2], ARGV[2], 1)
if type(pending) == 'table' and pending.err then
    return {0, pending.err}
end
if type(pending) ~= 'table' or #pending == 0 then
    return {0, 'source message is no longer pending'}
end

local added = redis.pcall('XADD', KEYS[1], unpack(xadd_args))
if type(added) == 'table' and added.err then
    return {0, added.err}
end

local acked = redis.pcall('XACK', KEYS[2], ARGV[1], ARGV[2])
if type(acked) == 'table' and acked.err then
    redis.pcall('XDEL', KEYS[1], added)
    return {0, acked.err}
end
if acked ~= 1 then
    local deleted = redis.pcall('XDEL', KEYS[1], added)
    if type(deleted) == 'table' and deleted.err then
        return {0, deleted.err}
    end
    return {0, 'source message is no longer pending'}
end
return {1, added, acked}
`)

var requeueAndDeleteScript = redis.NewScript(`
local field_count = tonumber(ARGV[1])
local xadd_args = {'*'}
for i = 1, field_count * 2 do
    xadd_args[#xadd_args + 1] = ARGV[1 + i]
end

local added = redis.pcall('XADD', KEYS[1], unpack(xadd_args))
if type(added) == 'table' and added.err then
    return {0, added.err}
end

local deleted = redis.pcall('XDEL', KEYS[2], ARGV[2 + field_count * 2])
if type(deleted) == 'table' and deleted.err then
    return {0, deleted.err}
end
return {1, added, deleted}
`)

func NewDLQ(rdb *redis.Client, stream string, group string, maxRetry int, baseDelay, maxDelay time.Duration) *DLQ {
	// group 参数保留以兼容配置，当前实现不使用 Consumer Group
	_ = group
	return &DLQ{
		rdb:       rdb,
		stream:    stream,
		maxRetry:  maxRetry,
		baseDelay: baseDelay,
		maxDelay:  maxDelay,
	}
}

// EnsureGroup 当前实现为空操作（保留接口兼容性）
func (d *DLQ) EnsureGroup(ctx context.Context) error {
	return nil
}

// Enqueue 将失败任务写入死信队列，并递增重试次数、记录失败原因和失败时间。
func (d *DLQ) Enqueue(ctx context.Context, task model.Task, reason string) error {
	return d.enqueue(ctx, task, reason)
}

func (d *DLQ) enqueue(ctx context.Context, task model.Task, reason string) error {
	task.Status = "dead_letter"
	task.FailedReason = reason
	task.LastFailedAt = time.Now().Unix()
	task.RetryCount++
	_, err := d.rdb.XAdd(ctx, &redis.XAddArgs{
		Stream: d.stream,
		Values: task.ToMap(),
	}).Result()
	return err
}

// EnqueueAndAck atomically moves a failed task to the DLQ and acknowledges its
// source message. This prevents a successful DLQ write followed by an ACK
// failure from executing the same side effect twice.
func (d *DLQ) EnqueueAndAck(ctx context.Context, store *RedisStore, priority, messageID string, task model.Task, reason string) error {
	task.Status = "dead_letter"
	task.FailedReason = reason
	task.LastFailedAt = time.Now().Unix()
	task.RetryCount++
	values := task.ToMap()
	args := []interface{}{store.Group(), messageID, len(values) * 2}
	for key, value := range values {
		args = append(args, key, fmt.Sprint(value))
	}
	return runAtomicMoveScript(ctx, enqueueAndAckScript, d.rdb,
		[]string{d.stream, store.StreamFor(priority)}, args...)
}

// StartReprocessor 启动死信队列重处理协程，按 interval 周期扫描。
func (d *DLQ) StartReprocessor(ctx context.Context, store *RedisStore, interval time.Duration) {
	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			d.processBatch(ctx, store)
		}
	}
}

func (d *DLQ) processBatch(ctx context.Context, store *RedisStore) {
	// 每次扫描最多 100 条
	msgs, err := d.rdb.XRangeN(ctx, d.stream, "-", "+", 100).Result()
	if err != nil {
		log.Printf("[DLQ] 扫描死信队列失败: %v", err)
		return
	}
	if len(msgs) == 0 {
		return
	}

	for _, msg := range msgs {
		d.handleMessage(ctx, store, msg)
	}
}

func (d *DLQ) handleMessage(ctx context.Context, store *RedisStore, msg redis.XMessage) {
	task := MessageToTask(msg.Values)

	// 超过最大重试次数，丢弃
	if task.RetryCount > d.maxRetry {
		log.Printf("[DLQ] taskId=%s 重试次数已达上限 %d，丢弃", task.TaskID, d.maxRetry)
		_ = d.rdb.XDel(ctx, d.stream, msg.ID).Err()
		return
	}

	// 计算当前重试次数对应的退避时间
	// retryCount=1 对应 baseDelay * 2^0，retryCount=2 对应 baseDelay * 2^1，依此类推
	backoff := model.RetryBackoff(task.RetryCount-1, d.baseDelay, d.maxDelay)
	elapsed := time.Since(time.Unix(task.LastFailedAt, 0))
	if elapsed < backoff {
		// 退避时间未到，跳过，等下次扫描
		log.Printf("[DLQ] taskId=%s 退避时间未到 (还需 %v)，跳过", task.TaskID, backoff-elapsed)
		return
	}

	// 只有重新入队成功后才删除死信消息，避免 Redis 命令级错误导致任务丢失。
	priority := model.NormalizePriority(task.Priority)
	values := task.ToMap()
	args := []interface{}{len(values) * 2}
	for key, value := range values {
		args = append(args, key, fmt.Sprint(value))
	}
	args = append(args, msg.ID)
	if err := runAtomicMoveScript(ctx, requeueAndDeleteScript, d.rdb,
		[]string{store.StreamFor(priority), d.stream}, args...); err != nil {
		log.Printf("[DLQ] taskId=%s 重新入队失败: %v", task.TaskID, err)
		return
	}

	log.Printf("[DLQ] taskId=%s 已重新入队 %s (retryCount=%d)", task.TaskID, priority, task.RetryCount)
}

func runAtomicMoveScript(ctx context.Context, script *redis.Script, rdb redis.Scripter,
	keys []string, args ...interface{}) error {
	result, err := script.Run(ctx, rdb, keys, args...).Result()
	if err != nil {
		return err
	}
	values, ok := result.([]interface{})
	if !ok || len(values) == 0 || fmt.Sprint(values[0]) != "1" {
		if len(values) > 1 {
			return fmt.Errorf("redis atomic move failed: %v", values[1])
		}
		return fmt.Errorf("redis atomic move failed")
	}
	return nil
}

// MessageToTask 将 Redis Stream 消息字段转换为 Task。
func MessageToTask(values map[string]interface{}) model.Task {
	toString := func(v interface{}) string {
		if v == nil {
			return ""
		}
		return fmt.Sprint(v)
	}
	toInt := func(v interface{}) int {
		s := toString(v)
		if s == "" {
			return 0
		}
		var n int
		_, _ = fmt.Sscanf(s, "%d", &n)
		return n
	}
	toInt64 := func(v interface{}) int64 {
		s := toString(v)
		if s == "" {
			return 0
		}
		var n int64
		_, _ = fmt.Sscanf(s, "%d", &n)
		return n
	}

	return model.Task{
		TaskID:       toString(values["taskId"]),
		Type:         toString(values["type"]),
		StudentID:    toString(values["studentId"]),
		Password:     toString(values["password"]),
		AcademicYear: toString(values["academicYear"]),
		Semester:     toString(values["semester"]),
		CallbackURL:  toString(values["callbackUrl"]),
		Status:       toString(values["status"]),
		Error:        toString(values["error"]),
		ResultJSON:   toString(values["resultJson"]),
		Priority:     toString(values["priority"]),
		RetryCount:   toInt(values["retryCount"]),
		LastFailedAt: toInt64(values["lastFailedAt"]),
		FailedReason: toString(values["failedReason"]),
		DayOfWeek:    toString(values["dayOfWeek"]),
		PeriodsMask:  toString(values["periodsMask"]),
		WeeksMask:    toString(values["weeksMask"]),
		CampusID:     toString(values["campusId"]),
		Building:     toString(values["building"]),
		RoomType:     toString(values["roomType"]),
		CreatedAt:    toInt64(values["createdAt"]),
		UpdatedAt:    toInt64(values["updatedAt"]),
	}
}
