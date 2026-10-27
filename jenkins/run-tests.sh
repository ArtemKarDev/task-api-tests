#!/usr/bin/env bash
set -eu
cd /workspace

# Если задан удалённый браузер, ждём, пока Selenium будет готов принимать сессии
if [ -n "${REMOTE_URL:-}" ]; then
  STATUS_URL="${REMOTE_URL%/wd/hub}/status"
  echo "Ожидание браузера: $STATUS_URL"
  for i in $(seq 1 60); do
    if curl -sf "$STATUS_URL" | grep -Eq 'ready"?: *true'; then
      echo "Браузер готов"
      break
    fi
    if [ "$i" -eq 60 ]; then
      echo "Браузер не стал готов за 2 минуты"
      exit 1
    fi
    sleep 2
  done
fi

mvn -B test "-Dgroups=${TEST_GROUPS}"