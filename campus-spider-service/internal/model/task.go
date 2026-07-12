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

	// 空教室查询参数
	DayOfWeek   string `json:"dayOfWeek"`
	PeriodsMask string `json:"periodsMask"`
	WeeksMask   string `json:"weeksMask"`
	CampusID    string `json:"campusId"`
	Building    string `json:"building"`
	RoomType    string `json:"roomType"`
}

const (
	PriorityHigh   = "high"
	PriorityMedium = "medium"
	PriorityLow    = "low"
)

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

	// 优先级与重试
	Priority      string `json:"priority"`
	RetryCount    int    `json:"retryCount"`
	LastFailedAt  int64  `json:"lastFailedAt"`
	FailedReason  string `json:"failedReason,omitempty"`

	// 空教室查询参数
	DayOfWeek   string `json:"dayOfWeek"`
	PeriodsMask string `json:"periodsMask"`
	WeeksMask   string `json:"weeksMask"`
	CampusID    string `json:"campusId"`
	Building    string `json:"building"`
	RoomType    string `json:"roomType"`

	CreatedAt int64 `json:"createdAt"`
	UpdatedAt int64 `json:"updatedAt"`
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
	Success bool   `json:"success"`
	Message string `json:"message"`
	Data    any    `json:"data"`
}

type CallbackPayload struct {
	StudentID    string         `json:"studentId"`
	AcademicYear string         `json:"academicYear"`
	Semester     string         `json:"semester"`
	PersonalInfo PersonalInfo   `json:"personalInfo"`
	ScheduleData []ScheduleItem `json:"scheduleData"`
}

// 空教室相关模型

type ClassroomItem struct {
	Building     string `json:"building"`
	RoomCode     string `json:"roomCode"`
	RoomName     string `json:"roomName"`
	Campus       string `json:"campus"`
	Capacity     string `json:"capacity"`
	RealCapacity string `json:"realCapacity"`
	RoomType     string `json:"roomType"`
	Floor        string `json:"floor"`
	Remark       string `json:"remark"`
}

type EmptyClassroomPayload struct {
	StudentID    string          `json:"studentId"`
	AcademicYear string          `json:"academicYear"`
	Semester     string          `json:"semester"`
	DayOfWeek    string          `json:"dayOfWeek"`
	PeriodsMask  string          `json:"periodsMask"`
	WeeksMask    string          `json:"weeksMask"`
	CampusID     string          `json:"campusId"`
	Building     string          `json:"building"`
	RoomType     string          `json:"roomType"`
	Classrooms   []ClassroomItem `json:"classrooms"`
}

// 成绩相关模型

type GradeItem struct {
	CourseName   string `json:"courseName"`
	CourseCode   string `json:"courseCode"`
	CourseNature string `json:"courseNature"`
	Credit       string `json:"credit"`
	Score        string `json:"score"`
	Gpa          string `json:"gpa"`
	Teacher      string `json:"teacher"`
	ExamNature   string `json:"examNature"`
	CourseType   string `json:"courseType"`
	AcademicYear string `json:"academicYear"`
	Semester     string `json:"semester"`
}

type GradesPayload struct {
	StudentID    string      `json:"studentId"`
	AcademicYear string      `json:"academicYear"`
	Semester     string      `json:"semester"`
	Grades       []GradeItem `json:"grades"`
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
		"priority":     t.Priority,
		"retryCount":   fmt.Sprint(t.RetryCount),
		"lastFailedAt": fmt.Sprint(t.LastFailedAt),
		"failedReason": t.FailedReason,
		"dayOfWeek":    t.DayOfWeek,
		"periodsMask":  t.PeriodsMask,
		"weeksMask":    t.WeeksMask,
		"campusId":     t.CampusID,
		"building":     t.Building,
		"roomType":     t.RoomType,
		"createdAt":    fmt.Sprint(t.CreatedAt),
		"updatedAt":    fmt.Sprint(t.UpdatedAt),
	}
}

// NormalizePriority 校验并归一化优先级，非法值默认返回 medium
func NormalizePriority(p string) string {
	switch p {
	case PriorityHigh, PriorityMedium, PriorityLow:
		return p
	default:
		return PriorityMedium
	}
}

// RetryBackoff 指数退避：base * 2^retryCount，最大上限 max
func RetryBackoff(retryCount int, base, max time.Duration) time.Duration {
	if retryCount < 0 {
		retryCount = 0
	}
	d := base * time.Duration(1<<retryCount)
	if d > max || d <= 0 {
		return max
	}
	return d
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
