#!/usr/bin/env bash
set -e
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
WORKSPACE_DIR="$(cd "$PROJECT_DIR/.." && pwd)"
source "$WORKSPACE_DIR/android-env/scripts/android-env.sh" >/dev/null
exec "$WORKSPACE_DIR/android-env/gradle/gradle-8.7/bin/gradle" -p "$PROJECT_DIR" "$@"
