package client

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
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
	_ = os.WriteFile("go_callback.json", b, 0644)
	log.Printf("[Callback] URL=%s Payload=%s", callbackURL, string(b))

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
	defer resp.Body.Close()

	// 判断是否成功响应
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("callback http status=%d", resp.StatusCode)
	}

	return nil
}

// PunchCallback 发送打卡结果回调到 Java 服务
func (c *JavaClient) PunchCallback(ctx context.Context, callbackURL, studentID string, success bool) error {
	url := fmt.Sprintf("%s?studentId=%s&success=%t", callbackURL, studentID, success)
	log.Printf("[PunchCallback] URL=%s", url)

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, nil)
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
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("punch callback http status=%d", resp.StatusCode)
	}

	return nil
}
