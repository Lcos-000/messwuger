package client

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	"campus-spider-service/internal/model"
)

func TestCallbackRejectsBusinessErrorWithHTTP200(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"code":401,"message":"rejected"}`))
	}))
	defer server.Close()

	err := NewJavaClient("").Callback(context.Background(), server.URL, model.CallbackPayload{})
	if err == nil {
		t.Fatal("expected business error to be returned")
	}
}

func TestCallbackAcceptsSuccessResult(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(`{"code":200,"message":"ok"}`))
	}))
	defer server.Close()

	if err := NewJavaClient("").Callback(context.Background(), server.URL, model.CallbackPayload{}); err != nil {
		t.Fatalf("expected success result, got %v", err)
	}
}

func TestCallbackRejectsMissingBusinessCode(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(`{"message":"missing code"}`))
	}))
	defer server.Close()

	if err := NewJavaClient("").Callback(context.Background(), server.URL, model.CallbackPayload{}); err == nil {
		t.Fatal("expected missing business code to be rejected")
	}
}
