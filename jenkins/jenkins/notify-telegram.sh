#!/usr/bin/env bash
# Sends a short test run summary to Telegram.
# Needs env: TG_TOKEN, TG_CHAT (secrets), STAND, TESTS, BUILD_NUMBER, BUILD_URL, BUILD_RESULT.
# Never prints the token. A failed notification must not break the build.
set -u

REPORTS_DIR="${REPORTS_DIR:-surefire-reports}"
RESULT="${BUILD_RESULT:-UNKNOWN}"

total=0
failed=0
skipped=0
for report in "$REPORTS_DIR"/*.xml; do
  [ -f "$report" ] || continue
  suite=$(grep -m1 '<testsuite ' "$report" || true)
  t=$(echo "$suite" | sed -n 's/.* tests="\([0-9]*\)".*/\1/p')
  e=$(echo "$suite" | sed -n 's/.* errors="\([0-9]*\)".*/\1/p')
  f=$(echo "$suite" | sed -n 's/.* failures="\([0-9]*\)".*/\1/p')
  s=$(echo "$suite" | sed -n 's/.* skipped="\([0-9]*\)".*/\1/p')
  total=$((total + ${t:-0}))
  failed=$((failed + ${e:-0} + ${f:-0}))
  skipped=$((skipped + ${s:-0}))
done
passed=$((total - failed - skipped))

case "$RESULT" in
  SUCCESS)  icon="✅" ;;
  UNSTABLE) icon="⚠️" ;;
  *)        icon="❌" ;;
esac

if [ "$total" -eq 0 ]; then
  summary="Tests: no results (the run stopped before the tests finished)"
else
  summary="Tests: ${total}, passed: ${passed}, failed: ${failed}, skipped: ${skipped}"
fi

failed_list=""
if [ "$failed" -gt 0 ]; then
  failed_list=$(awk '
    /<testcase / {
      match($0, /classname="[^"]*"/); cls = substr($0, RSTART + 11, RLENGTH - 12)
      match($0, / name="[^"]*"/);      nm  = substr($0, RSTART + 7,  RLENGTH - 8)
      n = split(cls, parts, "."); short = parts[n]
    }
    /<failure|<error/ { print "• " short "." nm }
  ' "$REPORTS_DIR"/*.xml | head -10)
  failed_list=$'\n\nFailed:\n'"$failed_list"
  if [ "$failed" -gt 10 ]; then
    failed_list="${failed_list}"$'\n'"…and $((failed - 10)) more"
  fi
fi

text="${icon} ${STAND:-?}: ${TESTS:-?}
Build #${BUILD_NUMBER}: ${RESULT}
${summary}${failed_list}

Build: ${BUILD_URL}
Allure: ${BUILD_URL}allure/"

curl -s -S --max-time 20 \
  "https://api.telegram.org/bot${TG_TOKEN}/sendMessage" \
  --data-urlencode "chat_id=${TG_CHAT}" \
  --data-urlencode "text=${text}" \
  -o /dev/null -w "Telegram: HTTP %{http_code}\n"