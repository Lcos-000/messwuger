FROM golang:1.24-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o server ./cmd/server

FROM python:3.11-slim
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && pip install --no-cache-dir requests pycryptodome urllib3

COPY --from=builder /app/server /app/server
COPY scripts/ /app/scripts/
RUN mkdir -p /app/data/sessions

ENV HTTP_ADDR=:8082
ENV PYTHON_PATH=/usr/local/bin/python3
ENV SPIDER_SCRIPT=/app/scripts/spider_cli.py
ENV CHECKIN_SCRIPT=/app/scripts/checkin_cli.py
ENV SESSION_DIR=/app/data/sessions
ENV PYTHONUNBUFFERED=1

EXPOSE 8082
ENTRYPOINT ["/app/server"]
