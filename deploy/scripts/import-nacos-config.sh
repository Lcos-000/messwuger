#!/bin/sh
set -e

BASE="http://campus-nacos:8848/nacos/v1/cs/configs"
NS_API="http://campus-nacos:8848/nacos/v1/console/namespaces"
NS_NAME="dev"

# 需要从 .env.secret 替换到 Nacos 配置文件中的占位符变量
SECRET_VARS="ALIYUN_OSS_ENDPOINT ALIYUN_OSS_ACCESS_KEY_ID ALIYUN_OSS_ACCESS_KEY_SECRET ALIYUN_OSS_BUCKET_NAME ALIYUN_OSS_URL_PREFIX"

echo "Waiting for Nacos namespaces API..."
tenant=""
i=1
while [ "$i" -le 30 ]; do
  ns_list=$(curl -fsSL "$NS_API" 2>/dev/null || echo "")
  tenant=$(echo "$ns_list" | grep -o "{[^}]*\"namespaceShowName\":\"$NS_NAME\"[^}]*}" | head -n1 | sed 's/.*"namespace":"\([^"]*\)".*/\1/')
  if [ -n "$tenant" ]; then
    echo "Found namespace $NS_NAME (id=$tenant)"
    break
  fi
  sleep 2
  i=$((i + 1))
done

if [ -z "$tenant" ]; then
  echo "Creating namespace $NS_NAME..."
  create_resp=$(curl -fsSL -X POST "$NS_API" \
    --data-urlencode "namespaceName=$NS_NAME" \
    --data-urlencode "namespaceDesc=$NS_NAME" 2>/dev/null || echo "")
  tenant=$(echo "$create_resp" | sed -n 's/.*"data":"\([^"]*\)".*/\1/p')
  if [ -z "$tenant" ]; then
    echo "Failed to create namespace $NS_NAME"
    exit 1
  fi
  echo "Created namespace $NS_NAME (id=$tenant)"
fi

# 用 .env.secret 中的值替换配置文件里的 ${VAR} 占位符
substitute_secrets() {
  content=$1
  for var in $SECRET_VARS; do
    eval "val=\${$var:-}"
    # 对值中的 sed 特殊字符进行转义
    esc_val=$(printf '%s\n' "$val" | sed 's/[&/|]/\\&/g')
    content=$(echo "$content" | sed "s|\${$var}|$esc_val|g")
  done
  echo "$content"
}

import_dir() {
  group=$1
  dir=$2
  for f in "$dir"/*.yaml; do
    [ -e "$f" ] || continue
    dataId=$(basename "$f")
    raw_content=$(cat "$f")
    content=$(substitute_secrets "$raw_content")
    echo "Importing $dataId to group=$group namespace=$NS_NAME"
    curl -fsSL -X POST "$BASE" \
      --data-urlencode "tenant=$tenant" \
      --data-urlencode "dataId=$dataId" \
      --data-urlencode "group=$group" \
      --data-urlencode "content=$content" >/dev/null
  done
}

import_dir DEFAULT_GROUP /nacos_config/DEFAULT_GROUP
import_dir DATASOURCE_GROUP /nacos_config/DATASOURCE_GROUP
import_dir GATEWAY_GROUP /nacos_config/GATEWAY_GROUP

echo "Nacos config import done"
