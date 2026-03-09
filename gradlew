#!/usr/bin/env bash
set -e
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
source /home/blue236/.openclaw/workspace/android-env/scripts/android-env.sh >/dev/null
exec /home/blue236/.openclaw/workspace/android-env/gradle/gradle-8.7/bin/gradle -p "$PROJECT_DIR" "$@"
