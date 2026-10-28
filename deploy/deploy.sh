#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"

SERVICE_URL_FILE=./nginx/service-url.inc
APP_PORT=8080

docker compose up -d postgres

docker compose pull app-blue

if docker compose ps --services --status running | grep -qx app-blue; then
  CURRENT=blue
  NEXT=green
else
  CURRENT=green
  NEXT=blue
fi
echo "현재: $CURRENT → 새 버전: $NEXT"

if [ ! -f "$SERVICE_URL_FILE" ]; then
  echo "set \$service_url http://app-$NEXT:$APP_PORT;" > "$SERVICE_URL_FILE"
fi
docker compose up -d nginx

docker compose up -d --force-recreate "app-$NEXT"

for i in $(seq 1 30); do
  if docker compose exec -T nginx wget -q -O /dev/null "http://app-$NEXT:$APP_PORT/v3/api-docs"; then
    echo "app-$NEXT 정상 기동 확인"
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "app-$NEXT 기동 실패, 배포 중단"
    docker compose logs --tail 50 "app-$NEXT"
    docker compose stop "app-$NEXT"
    exit 1
  fi
  sleep 3
done

echo "set \$service_url http://app-$NEXT:$APP_PORT;" > "$SERVICE_URL_FILE"
docker compose exec -T nginx nginx -s reload
echo "nginx가 app-$NEXT로 전환됨"

docker compose stop "app-$CURRENT"

docker image prune -f
