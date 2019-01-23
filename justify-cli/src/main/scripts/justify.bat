@echo off

if defined JAVA_HOME (
  set JAVA="%JAVA_HOME%\bin\java"
) else (
  set JAVA=java
)

%JAVA% -jar lib\justify-cli-${project.version}.jar %*
