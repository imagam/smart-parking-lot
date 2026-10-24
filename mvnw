#!/usr/bin/env sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
MAVEN_VERSION=3.9.9
MAVEN_HOME="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION"
MAVEN_ZIP="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION-bin.zip"
if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$WRAPPER_DIR"
  URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
  echo "Maven $MAVEN_VERSION is not cached. Downloading it now..."
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$URL" -o "$MAVEN_ZIP"
  elif command -v wget >/dev/null 2>&1; then wget -q "$URL" -O "$MAVEN_ZIP"
  else echo "Please install curl or wget to bootstrap Maven." >&2; exit 1; fi
  unzip -q "$MAVEN_ZIP" -d "$WRAPPER_DIR"
  rm -f "$MAVEN_ZIP"
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
