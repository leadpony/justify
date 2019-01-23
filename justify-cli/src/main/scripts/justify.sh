#!/bin/bash

if [ -n "$JAVA_HOME" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

"$JAVA" -jar lib/justify-cli-${project.version}.jar $*
