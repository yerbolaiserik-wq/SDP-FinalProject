@echo off
setlocal
cd /d "%~dp0"
set "APP_JAVA=java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "APP_JAVA=%JAVA_HOME%\bin\java.exe"
if exist "%USERPROFILE%\.jdks\openjdk-27\bin\java.exe" set "APP_JAVA=%USERPROFILE%\.jdks\openjdk-27\bin\java.exe"
"%APP_JAVA%" -jar "Restaurant_Ordering_System.jar"
if errorlevel 1 pause
endlocal
