@echo off
setlocal

if defined JAVA_HOME (
  set JAVA="%JAVA_HOME%\bin\java"
) else (
  set JAVA=java
)

set DIR=%~dp0

%JAVA% -jar %DIR%\lib\justify-cli-${project.version}.jar %*
