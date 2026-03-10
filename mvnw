#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper bootstrap script for POSIX (Linux/macOS).
# This file is generated/added to allow builds without a preinstalled Maven.
#
# It will download and cache the Maven distribution configured in:
#   .mvn/wrapper/maven-wrapper.properties
# ----------------------------------------------------------------------------

# Resolve the project base directory (where .mvn lives).
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

# Prefer JAVA_HOME if set; otherwise rely on java on PATH.
if [ -n "$JAVA_HOME" ]; then
  JAVA_EXEC="$JAVA_HOME/bin/java"
else
  JAVA_EXEC="java"
fi

if ! command -v "$JAVA_EXEC" >/dev/null 2>&1; then
  echo "Error: Java executable not found. Please set JAVA_HOME or ensure 'java' is on PATH." >&2
  exit 1
fi

WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"

if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "Error: Missing $WRAPPER_PROPERTIES" >&2
  exit 1
fi

# The wrapper jar is committed to the repo for hermetic CI builds.
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Error: Missing $WRAPPER_JAR" >&2
  echo "This repository expects the Maven Wrapper jar to be committed alongside the scripts." >&2
  exit 1
fi

# Allow users to add extra JVM opts for the wrapper itself.
# Example: export MAVEN_WRAPPER_OPTS="-Xmx256m"
exec "$JAVA_EXEC" ${MAVEN_WRAPPER_OPTS:-} -classpath "$WRAPPER_JAR" \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
