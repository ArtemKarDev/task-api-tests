#!/usr/bin/env bash
# Sends a test run summary to Telegram: a chart image with a caption, plus a list of failed tests if any.
# Needs env: TG_TOKEN, TG_CHAT (secrets), STAND, TESTS, BUILD_NUMBER, BUILD_URL, BUILD_RESULT, BUILD_DURATION_MS.
# Optional: NEED_BROWSER, BROWSER. Never prints the token. A failed notification must not break the build.
set -u

REPORTS_DIR="${REPORTS_DIR:-surefire-reports}"
RESULT="${BUILD_RESULT:-UNKNOWN}"
API="https://api.telegram.org/bot${TG_TOKEN}"

# Escapes characters that are special in Telegram HTML mode
esc() { printf '%s' "$1" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g'; }

send_text() {
  curl -s -S --max-time 20 "${API}/sendMessage" \
    --data-urlencode "chat_id=${TG_CHAT}" \
    --data-urlencode "parse_mode=HTML" \
    --data-urlencode "text=$1" \
    -o /dev/null -w "Telegram text: HTTP %{http_code}\n"
}

# ---------- counters from surefire reports ----------
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

# ---------- duration of the whole build as HH:MM:SS ----------
ms="${BUILD_DURATION_MS:-0}"
case "$ms" in ''|*[!0-9]*) ms=0 ;; esac
secs=$((ms / 1000))
duration=$(printf '%02d:%02d:%02d' $((secs / 3600)) $((secs % 3600 / 60)) $((secs % 60)))

case "$RESULT" in
  SUCCESS)  icon="✅" ;;
  UNSTABLE) icon="⚠️" ;;
  *)        icon="❌" ;;
esac

# ---------- caption (Telegram HTML) ----------
caption="${icon} <b>Results:</b>
<b>Stand:</b> $(esc "${STAND:-?}")
<b>Tests:</b> $(esc "${TESTS:-?}")
<b>Trigger:</b> $(esc "${TRIGGER:-manual}")"
if [ "${NEED_BROWSER:-false}" = "true" ]; then
  caption="${caption}
<b>Browser:</b> $(esc "${BROWSER:-?}")"
fi
caption="${caption}
<b>Build:</b> #${BUILD_NUMBER:-0} ${RESULT}
<b>Duration:</b> ${duration}"

if [ "$total" -eq 0 ]; then
  caption="${caption}
<b>Total scenarios:</b> no results (the run stopped before the tests finished)"
else
  pct=$((passed * 100 / total))
  caption="${caption}
<b>Total scenarios:</b> ${total}
<b>Total passed:</b> ${passed} (${pct} %)"
  [ "$failed" -gt 0 ] && caption="${caption}
<b>Failed:</b> ${failed}"
  [ "$skipped" -gt 0 ] && caption="${caption}
<b>Skipped:</b> ${skipped}"
fi
caption="${caption}
<b>Report available at the link:</b>
${BUILD_URL}allure/"

# ---------- chart: QuickChart renders a Chart.js config to PNG (optional) ----------
sent=0
if [ "$total" -gt 0 ]; then
  labels=""; data=""; colors=""
  add_segment() { # label value color: segments with zero value are not drawn
    [ "$2" -gt 0 ] || return 0
    labels="${labels:+$labels,}\"$1\""
    data="${data:+$data,}$2"
    colors="${colors:+$colors,}\"$3\""
  }
  add_segment "${passed} passed" "$passed" "#8bc34a"
  add_segment "${failed} failed" "$failed" "#e74c3c"
  add_segment "${skipped} skipped" "$skipped" "#95a5a6"

  config="{\"type\":\"doughnut\",\"data\":{\"labels\":[${labels}],\"datasets\":[{\"data\":[${data}],\"backgroundColor\":[${colors}],\"borderWidth\":0}]},\"options\":{\"cutoutPercentage\":78,\"title\":{\"display\":true,\"text\":\"${STAND:-?}: ${TESTS:-?}\"},\"legend\":{\"position\":\"right\"},\"plugins\":{\"doughnutlabel\":{\"labels\":[{\"text\":\"${total}\",\"font\":{\"size\":48}}]}}}}"

  chart_file="$(mktemp --suffix=.png)"
  if curl -s -S -f --max-time 20 -G "https://quickchart.io/chart" \
       --data-urlencode "c=${config}" --data-urlencode "w=520" --data-urlencode "h=300" \
       --data-urlencode "bkg=white" -o "$chart_file"; then
    # --form-string: the caption starts with "<", curl -F would treat that as "read from file"
    code=$(curl -s -S --max-time 30 "${API}/sendPhoto" \
      --form-string "chat_id=${TG_CHAT}" \
      -F "photo=@${chart_file}" \
      --form-string "parse_mode=HTML" \
      --form-string "caption=${caption}" \
      -o /dev/null -w "%{http_code}")
    echo "Telegram photo: HTTP ${code}"
    [ "$code" = "200" ] && sent=1
  else
    echo "Chart was not generated"
  fi
  rm -f "$chart_file"
fi

# No chart (no results or chart service unavailable): send the same caption as text
[ "$sent" = "1" ] || send_text "$caption"

# ---------- failed tests: separate message ----------
if [ "$failed" -gt 0 ]; then
  names=$(awk '
    /<testcase / {
      match($0, /classname="[^"]*"/); cls = substr($0, RSTART + 11, RLENGTH - 12)
      match($0, / name="[^"]*"/);      nm  = substr($0, RSTART + 7,  RLENGTH - 8)
      n = split(cls, parts, "."); short = parts[n]
    }
    /<failure|<error/ { print "• " short "." nm }
  ' "$REPORTS_DIR"/*.xml | head -10)
  failed_text="<b>Failed tests:</b>
$(esc "$names")"
  if [ "$failed" -gt 10 ]; then
    failed_text="${failed_text}
…and $((failed - 10)) more"
  fi
  send_text "$failed_text"
fi