package spider

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"time"

	"campus-spider-service/internal/model"
)

// Runner 爬虫运行器
type Runner struct {
	PythonPath string
	ScriptPath string
	SessionDir string
	Timeout    time.Duration
	ProxyPool  *ProxyPool
	YMToken    string
	YMType     string
}

// NewRunner 创建爬虫运行器
func NewRunner(pythonPath, scriptPath, sessionDir string, timeout time.Duration, proxyPool *ProxyPool, ymToken, ymType string) *Runner {
	_ = os.MkdirAll(sessionDir, 0755)
	return &Runner{
		PythonPath: pythonPath,
		ScriptPath: scriptPath,
		SessionDir: sessionDir,
		Timeout:    timeout,
		ProxyPool:  proxyPool,
		YMToken:    ymToken,
		YMType:     ymType,
	}
}

// buildEnv 构造 Python 子进程环境变量
func (r *Runner) buildEnv() []string {
	return append(os.Environ(),
		"PYTHONUNBUFFERED=1",
		"PYTHONIOENCODING=utf-8",
		"YM_TOKEN="+r.YMToken,
		"YM_TYPE="+r.YMType,
	)
}

// RunCrawl 运行爬虫
func (r *Runner) RunCrawl(parent context.Context, task model.Task) (model.SpiderOutput, error) {
	ctx, cancel := context.WithTimeout(parent, r.Timeout)
	defer cancel()

	args := []string{
		r.ScriptPath,
		"--mode", "crawl",
		"--student-id", task.StudentID,
		"--password", task.Password,
		"--xnm", task.AcademicYear,
		"--xqm", task.Semester,
		"--session-dir", r.SessionDir,
	}

	// 从代理池中获取代理
	if proxy := r.pickProxy(); proxy != "" {
		args = append(args, "--proxy", proxy)
	}

	cmd := exec.CommandContext(ctx, r.PythonPath, args...)
	cmd.Env = r.buildEnv()

	// 执行 python 脚本
	out, err := cmd.CombinedOutput()
	if err != nil {
		return model.SpiderOutput{}, fmt.Errorf("python 执行失败: %w, output=%s", err, string(out))
	}

	// 解析 python 输出
	var resp model.SpiderOutput
	if err := json.Unmarshal(out, &resp); err != nil {
		return model.SpiderOutput{}, fmt.Errorf("解析 python 输出失败: %w, raw=%s", err, string(out))
	}

	// 检查爬虫是否成功
	if !resp.Success {
		return model.SpiderOutput{}, fmt.Errorf("%s", resp.Message)
	}

	return resp, nil
}

// ValidateCredentials 验证登录凭证据
func (r *Runner) ValidateCredentials(parent context.Context, studentID, password string) (bool, string) {
	ctx, cancel := context.WithTimeout(parent, 3*time.Minute)
	defer cancel()

	args := []string{
		r.ScriptPath,
		"--mode", "validate",
		"--student-id", studentID,
		"--password", password,
		"--session-dir", r.SessionDir,
	}
	// 从代理池中获取代理
	if proxy := r.pickProxy(); proxy != "" {
		args = append(args, "--proxy", proxy)
	}

	// 执行 python 脚本
	cmd := exec.CommandContext(ctx, r.PythonPath, args...)
	cmd.Env = r.buildEnv()
	out, err := cmd.CombinedOutput()

	// 先解析 python 输出；validate 模式下账号错误会返回 exit code 1，但 stdout 仍是有效 JSON
	var resp model.SpiderOutput
	if jsonErr := json.Unmarshal(out, &resp); jsonErr == nil {
		return resp.Success, resp.Message
	}

	if err != nil {
		return false, fmt.Sprintf("验证失败: %v, output=%s", err, string(out))
	}

	return false, fmt.Sprintf("验证输出解析失败, raw=%s", string(out))
}

// RunCheckin 运行打卡脚本
func (r *Runner) RunCheckin(parent context.Context, task model.Task, checkinScript string, timeout time.Duration) (model.SpiderOutput, error) {
	ctx, cancel := context.WithTimeout(parent, timeout)
	defer cancel()

	args := []string{
		checkinScript,
		"--student-id", task.StudentID,
		"--password", task.Password,
	}

	cmd := exec.CommandContext(ctx, r.PythonPath, args...)
	cmd.Env = r.buildEnv()
	out, err := cmd.CombinedOutput()
	if err != nil {
		return model.SpiderOutput{}, fmt.Errorf("checkin python 执行失败: %w, output=%s", err, string(out))
	}

	var resp model.SpiderOutput
	if err := json.Unmarshal(out, &resp); err != nil {
		return model.SpiderOutput{}, fmt.Errorf("解析 checkin 输出失败: %w, raw=%s", err, string(out))
	}

	// 对于打卡，no_task 和 already_checked 也视为成功
	if !resp.Success {
		return model.SpiderOutput{}, fmt.Errorf("%s", resp.Message)
	}

	return resp, nil
}

// RunEmptyClassroom 运行空教室查询
func (r *Runner) RunEmptyClassroom(parent context.Context, task model.Task) (model.SpiderOutput, error) {
	ctx, cancel := context.WithTimeout(parent, r.Timeout)
	defer cancel()

	args := []string{
		r.ScriptPath,
		"--mode", "empty-classroom",
		"--student-id", task.StudentID,
		"--password", task.Password,
		"--xnm", task.AcademicYear,
		"--xqm", task.Semester,
		"--xqj", task.DayOfWeek,
		"--jcd", task.PeriodsMask,
		"--zcd", task.WeeksMask,
		"--xqh-id", task.CampusID,
		"--lh", task.Building,
		"--cdlb-id", task.RoomType,
		"--session-dir", r.SessionDir,
	}

	if proxy := r.pickProxy(); proxy != "" {
		args = append(args, "--proxy", proxy)
	}

	cmd := exec.CommandContext(ctx, r.PythonPath, args...)
	cmd.Env = r.buildEnv()

	out, err := cmd.CombinedOutput()
	if err != nil {
		return model.SpiderOutput{}, fmt.Errorf("empty-classroom python 执行失败: %w, output=%s", err, string(out))
	}

	var resp struct {
		Success bool                        `json:"success"`
		Message string                      `json:"message"`
		Data    model.EmptyClassroomPayload `json:"data"`
	}
	if err := json.Unmarshal(out, &resp); err != nil {
		return model.SpiderOutput{}, fmt.Errorf("解析 empty-classroom 输出失败: %w, raw=%s", err, string(out))
	}

	if !resp.Success {
		return model.SpiderOutput{}, fmt.Errorf("%s", resp.Message)
	}

	return model.SpiderOutput{Success: resp.Success, Message: resp.Message, Data: resp.Data}, nil
}

// RunGrades 运行成绩查询
func (r *Runner) RunGrades(parent context.Context, task model.Task) (model.SpiderOutput, error) {
	ctx, cancel := context.WithTimeout(parent, r.Timeout)
	defer cancel()

	args := []string{
		r.ScriptPath,
		"--mode", "grades",
		"--student-id", task.StudentID,
		"--password", task.Password,
		"--xnm", task.AcademicYear,
		"--xqm", task.Semester,
		"--session-dir", r.SessionDir,
	}

	if proxy := r.pickProxy(); proxy != "" {
		args = append(args, "--proxy", proxy)
	}

	cmd := exec.CommandContext(ctx, r.PythonPath, args...)
	cmd.Env = r.buildEnv()

	out, err := cmd.CombinedOutput()
	if err != nil {
		return model.SpiderOutput{}, fmt.Errorf("grades python 执行失败: %w, output=%s", err, string(out))
	}

	var resp struct {
		Success bool                `json:"success"`
		Message string              `json:"message"`
		Data    model.GradesPayload `json:"data"`
	}
	if err := json.Unmarshal(out, &resp); err != nil {
		return model.SpiderOutput{}, fmt.Errorf("解析 grades 输出失败: %w, raw=%s", err, string(out))
	}

	if !resp.Success {
		return model.SpiderOutput{}, fmt.Errorf("%s", resp.Message)
	}

	return model.SpiderOutput{Success: resp.Success, Message: resp.Message, Data: resp.Data}, nil
}

// pickProxy 从代理池中获取代理
func (r *Runner) pickProxy() string {
	if r.ProxyPool == nil {
		return ""
	}
	return r.ProxyPool.Pick()
}
