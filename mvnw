#!/bin/sh
# ----------------------------------------------------------------------------
# Shopizer (modern-java21) Maven entrypoint.
#
# Preview environments may block outbound downloads, which breaks the standard
# Maven Wrapper flow (it downloads a Maven distribution and sometimes the wrapper
# JAR itself). This repository already vendors a Maven distribution under:
#   .tools/apache-maven-*/bin/mvn
#
# Therefore, this script prefers the vendored Maven by default and only falls
# back to the Maven Wrapper JAR flow when explicitly requested.
#
# Environment variables:
#   SHOPIZER_MAVEN_USE_WRAPPER_JAR=1  -> force using .mvn/wrapper/maven-wrapper.jar
#   MAVEN_WRAPPER_OPTS               -> extra JVM opts when using wrapper jar
# ----------------------------------------------------------------------------

# Resolve the project base directory (where .mvn lives).
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

# 1) Prefer vendored Maven distribution (offline-friendly)
if [ "${SHOPIZER_MAVEN_USE_WRAPPER_JAR:-0}" != "1" ]; then
  VENDORED_MVN=""
  for candidate in "$BASE_DIR"/.tools/apache-maven-*/bin/mvn; do
    if [ -f "$candidate" ]; then
      VENDORED_MVN="$candidate"
      break
    fi
  done

  if [ -n "$VENDORED_MVN" ]; then
    # Avoid relying on chmod in restricted environments: execute via sh if needed.
    if [ -x "$VENDORED_MVN" ]; then
      exec "$VENDORED_MVN" "$@"
    else
      exec sh "$VENDORED_MVN" "$@"
    fi
  fi
fi

# 2) Fallback to Maven Wrapper JAR flow (may require network to fetch distribution)
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

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Error: Missing $WRAPPER_JAR" >&2
  echo "Tip: this repo can run Maven without downloads via the vendored distribution under .tools/." >&2
  echo "     Ensure '.tools/apache-maven-*/bin/mvn' exists, or vendor the wrapper jar." >&2
  exit 1
fi

# Allow users to add extra JVM opts for the wrapper itself.
# Example: export MAVEN_WRAPPER_OPTS="-Xmx256m"
exec "$JAVA_EXEC" ${MAVEN_WRAPPER_OPTS:-} -classpath "$WRAPPER_JAR" \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
