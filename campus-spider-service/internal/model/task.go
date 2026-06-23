/* Python 输出 JSON
→ Runner 解析成 SpiderOutput 结构体
→ 再转成 CallbackPayload 结构体
→ json.Marshal 变回 JSON
→ 发给 Java
*/

package model

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/google/uuid"
)

type APIResponse struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Data    any    `json:"data,omitempty"`
}

type StartTaskRequest struct {
	AcademicYear string `json:"academicYear"`
	Semester     string `json:"semester"`
	CallbackURL  string `json:"callbackUrl"`
}

type Task struct {
	TaskID       string `json:"taskId"`
	Type         string `json:"type"`
	StudentID    string `json:"studentId"`
	Password     string `json:"password"`
	AcademicYear string `json:"academicYear"`
	Semester     string `json:"semester"`
	CallbackURL  string `json:"callbackUrl"`
	Status       string `json:"status"`
	Error        string `json:"error,omitempty"`
	ResultJSON   string `json:"resultJson,omitempty"`
	CreatedAt    int64  `json:"createdAt"`
	UpdatedAt    int64  `json:"updatedAt"`
}

type PersonalInfo struct {
	StudentID string `json:"studentId"`
	Name      string `json:"name"`
	Major     string `json:"major"`
	ClassName string `json:"className"`
	College   string `json:"college"`
}

type ScheduleItem map[string]any

type SpiderData struct {
	StudentID    string         `json:"studentId"`
	AcademicYear string         `json:"academicYear"`
	Semester     string         `json:"semester"`
	PersonalInfo PersonalInfo   `json:"personalInfo"`
	ScheduleData []ScheduleItem `json:"scheduleData"`
}

type SpiderOutput struct {
	Success bool       `json:"success"`
	Message string     `json:"message"`
	Data    SpiderData `json:"data"`
}

type CallbackPayload struct {
	StudentID    string         `json:"studentId"`
	AcademicYear string         `json:"academicYear"`
	Semester     string         `json:"semester"`
	PersonalInfo PersonalInfo   `json:"personalInfo"`
	ScheduleData []ScheduleItem `json:"scheduleData"`
}

// 创建TEaskID
func NewTaskID() string {
	return "task-" + uuid.NewString()
}

// 创建时间戳
func NowUnix() int64 {
	return time.Now().Unix()
}

func (t Task) ToMap() map[string]any {
	return map[string]any{
		"taskId":       t.TaskID,
		"type":         t.Type,
		"studentId":    t.StudentID,
		"password":     t.Password,
		"academicYear": t.AcademicYear,
		"semester":     t.Semester,
		"callbackUrl":  t.CallbackURL,
		"status":       t.Status,
		"error":        t.Error,
		"resultJson":   t.ResultJSON,
		"createdAt":    fmt.Sprint(t.CreatedAt),
		"updatedAt":    fmt.Sprint(t.UpdatedAt),
	}
}

func (d SpiderData) ToCallbackPayload() CallbackPayload {
	return CallbackPayload{
		StudentID:    d.StudentID,
		AcademicYear: d.AcademicYear,
		Semester:     d.Semester,
		PersonalInfo: d.PersonalInfo,
		ScheduleData: d.ScheduleData,
	}
}

// 将结构体转换为 JSON 字符串
func MustJSON(v any) string {
	b, _ := json.Marshal(v)
	return string(b)
}
