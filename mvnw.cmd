@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "TOOLS_DIR=%SCRIPT_DIR%.tools"
set "MAVEN_VERSION=3.9.15"

for /d %%D in ("%TOOLS_DIR%\jdk-*") do (
    if exist "%%~fD\bin\java.exe" (
        set "JAVA_HOME=%%~fD"
        goto :java_ready
    )
)

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_ready

echo Java was not found for this project.
echo Run: powershell -ExecutionPolicy Bypass -File ".\setup-local-toolchain.ps1"
exit /b 1

:java_ready
set "PATH=%JAVA_HOME%\bin;%PATH%"

if exist "%TOOLS_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" (
    call "%TOOLS_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" %*
    set EXIT_CODE=%ERRORLEVEL%
    endlocal & exit /b %EXIT_CODE%
)

where mvn >nul 2>nul
if %ERRORLEVEL%==0 (
    call mvn %*
    set EXIT_CODE=%ERRORLEVEL%
    endlocal & exit /b %EXIT_CODE%
)

echo Maven was not found for this project.
echo Run: powershell -ExecutionPolicy Bypass -File ".\setup-local-toolchain.ps1"
exit /b 1
