#!/bin/sh
# Apache Maven Wrapper bootstrap script.
# Minimal wrapper script used for CI/preview environments.
# See https://maven.apache.org/wrapper/

MVNW_REPOURL="https://repo.maven.apache.org/maven2"
WRAPPER_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_PROJECTBASEDIR="$WRAPPER_DIR"

# Download the wrapper jar if missing
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$(dirname "$WRAPPER_JAR")"
  echo "Downloading Maven Wrapper..."
  curl -fsSL "$MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" -o "$WRAPPER_JAR"
fi

exec java -jar "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" "$@"
