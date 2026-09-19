package client

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"time"

	"campus-spider-service/internal/model"
)

// JavaClient 是一个 Java 服务的客户端
type JavaClient struct {
	HTTPClient *http.Client
	Token      string
}

// NewJavaClient 创建一个新的 JavaClient 实例
func NewJavaClient(token string) *JavaClient {
	return &JavaClient{
		HTTPClient: &http.Client{Timeout: 30 * time.Second},
		Token:      token,
	}
}

// Callback 发送回调请求到 Java 服务
func (c *JavaClient) Callback(ctx context.Context, callbackURL string, payload model.CallbackPayload) error {
	// 把payload转换为JSON字符串
	b, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	log.Printf("[Callback] URL=%s", callbackURL)

	// 组装请求体
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, callbackURL, bytes.NewReader(b))
	if err != nil {
		return err
	}
	// 设置请求头
	req.Header.Set("Content-Type", "application/json")
	if c.Token != "" {
		req.Header.Set("Authorization", "Bearer "+c.Token)
	}

	// 发送请求
	// 这里发的就是只有body数据，没有其他header
	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return err
	}
	return validateCallbackResponse(resp, "callback")
}

// PunchCallback 发送打卡结果回调到 Java 服务
func (c *JavaClient) PunchCallback(ctx context.Context, callbackURL, studentID string, success bool) error {
	parsedURL, err := url.Parse(callbackURL)
	if err != nil {
		return err
	}
	query := parsedURL.Query()
	query.Set("studentId", studentID)
	query.Set("success", strconv.FormatBool(success))
	parsedURL.RawQuery = query.Encode()
	log.Printf("[PunchCallback] URL=%s", parsedURL.String())

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, parsedURL.String(), nil)
	if err != nil {
		return err
	}
	if c.Token != "" {
		req.Header.Set("Authorization", "Bearer "+c.Token)
	}

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return err
	}
	return validateCallbackResponse(resp, "punch callback")
}

// EmptyClassroomCallback 发送空教室查询结果回调到 Java 服务
func (c *JavaClient) EmptyClassroomCallback(ctx context.Context, callbackURL string, payload model.EmptyClassroomPayload) error {
	b, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	log.Printf("[EmptyClassroomCallback] URL=%s", callbackURL)

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, callbackURL, bytes.NewReader(b))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	if c.Token != "" {
		req.Header.Set("Authorization", "Bearer "+c.Token)
	}

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return err
	}
	return validateCallbackResponse(resp, "empty classroom callback")
}

// GradesCallback 发送成绩查询结果回调到 Java 服务
func (c *JavaClient) GradesCallback(ctx context.Context, callbackURL string, payload model.GradesPayload) error {
	b, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	log.Printf("[GradesCallback] URL=%s", callbackURL)

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, callbackURL, bytes.NewReader(b))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	if c.Token != "" {
		req.Header.Set("Authorization", "Bearer "+c.Token)
	}

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return err
	}
	return validateCallbackResponse(resp, "grades callback")
}

func validateCallbackResponse(resp *http.Response, operation string) error {
	defer resp.Body.Close()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("%s http status=%d", operation, resp.StatusCode)
	}

	body, err := io.ReadAll(io.LimitReader(resp.Body, 1<<20))
	if err != nil {
		return fmt.Errorf("%s read response: %w", operation, err)
	}
	if len(strings.TrimSpace(string(body))) == 0 {
		return fmt.Errorf("%s empty response", operation)
	}

	var result struct {
		Code *int `json:"code"`
	}
	if err := json.Unmarshal(body, &result); err != nil {
		return fmt.Errorf("%s invalid response: %w", operation, err)
	}
	if result.Code == nil {
		return fmt.Errorf("%s missing business code", operation)
	}
	if *result.Code != http.StatusOK {
		return fmt.Errorf("%s business code=%d", operation, *result.Code)
	}
	return nil
}
