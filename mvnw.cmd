@REM ----------------------------------------------------------------------------
@REM Maven Wrapper bootstrap script for Windows.
@REM This file is added to allow builds without a preinstalled Maven.
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set BASE_DIR=%~dp0
if "%BASE_DIR:~-1%"=="\" set BASE_DIR=%BASE_DIR:~0,-1%

set WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%WRAPPER_PROPERTIES%" (
  echo Error: Missing "%WRAPPER_PROPERTIES%"
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  echo Error: Missing "%WRAPPER_JAR%"
  echo This repository expects the Maven Wrapper jar to be committed alongside the scripts.
  exit /b 1
)

if not "%JAVA_HOME%"=="" (
  set JAVA_EXEC=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXEC=java.exe
)

"%JAVA_EXEC%" %MAVEN_WRAPPER_OPTS% -classpath "%WRAPPER_JAR%" ^
  -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
