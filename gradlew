#!/usr/bin/env sh
# Lightweight wrapper bootstrap for environments where the Gradle wrapper JAR is not included.
set -eu
VERSION="8.9"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/manual-wrapper"
DIST="$BASE/gradle-$VERSION"
if [ ! -x "$DIST/bin/gradle" ]; then
  mkdir -p "$BASE"
  ZIP="$BASE/gradle-$VERSION-bin.zip"
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Gradle $VERSION..."
    curl -fL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ZIP"
  fi
  unzip -q -o "$ZIP" -d "$BASE"
fi
exec "$DIST/bin/gradle" "$@"
