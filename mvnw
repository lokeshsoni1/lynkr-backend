#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Batch script for Unix/Linux/macOS
# ----------------------------------------------------------------------------

export MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(pwd)}"
WRAPPER_JAR="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"
WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    mkdir -p "${MAVEN_PROJECTBASEDIR}/.mvn/wrapper"
    if command -v curl >/dev/null 2>&1; then
        curl -sH "User-Agent: Mozilla/5.0" -o "$WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
    fi
fi

exec java -classpath "$WRAPPER_JAR" "$WRAPPER_LAUNCHER" "$@"
