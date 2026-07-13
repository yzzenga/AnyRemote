@if "%DEBUG%"=="" @echo off
setlocal enabledelayedexpansion

set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

if defined JAVA_HOME (
    set "JAVA_HOME=!JAVA_HOME:"=!"
)

if "!JAVA_HOME!"=="" (
    where java >nul 2>nul
    if !ERRORLEVEL! equ 0 (
        for /f "delims=" %%i in ('where java') do set "JAVA_EXE=%%i"
        goto execute
    )
    echo ERROR: JAVA_HOME is not set and no 'java' command could be found.
    exit /b 1
)

if exist "!JAVA_HOME!\bin\java.exe" (
    set "JAVA_EXE=!JAVA_HOME!\bin\java.exe"
    goto execute
)

echo ERROR: JAVA_HOME is set to an invalid directory: !JAVA_HOME!
exit /b 1

:execute
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
