package main

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"campus-spider-service/internal/config"
)

func TestRequireAPIToken(t *testing.T) {
	app := &App{cfg: config.Config{APIToken: "shared-secret"}}
	nextCalls := 0
	handler := app.requireAPIToken(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		nextCalls++
		w.WriteHeader(http.StatusNoContent)
	}))

	tests := []struct {
		name       string
		token      string
		wantStatus int
		wantCalls  int
	}{
		{name: "missing token", wantStatus: http.StatusUnauthorized, wantCalls: 0},
		{name: "wrong token", token: "wrong", wantStatus: http.StatusUnauthorized, wantCalls: 0},
		{name: "valid token", token: "shared-secret", wantStatus: http.StatusNoContent, wantCalls: 1},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			req := httptest.NewRequest(http.MethodPost, "/api/v1/task/submit", nil)
			if tt.token != "" {
				req.Header.Set("X-Spider-Token", tt.token)
			}
			recorder := httptest.NewRecorder()

			handler.ServeHTTP(recorder, req)

			if recorder.Code != tt.wantStatus {
				t.Fatalf("status = %d, want %d", recorder.Code, tt.wantStatus)
			}
		})
	}

	if nextCalls != 1 {
		t.Fatalf("next handler called %d times, want 1", nextCalls)
	}
}
