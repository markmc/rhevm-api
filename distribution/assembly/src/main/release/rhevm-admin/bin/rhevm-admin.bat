@echo off

setlocal

set DIRNAME=%~dp0%
set ARGS=%*

title Rhevm-Admin

if "%RHEVM_API_URL%" == "" (
  set RHEVM_API_URL=http://localhost:8080/rhevm-api-powershell
)

if "%JAVA_OPTS%" == "" (
  set JAVA_OPTS=-Drhevm.base.url=%RHEVM_API_URL% -Drhevm.auth.username=%RHEVM_USERNAME% -Drhevm.auth.password=%RHEVM_PASSWORD%
)

if "%SHIFT%" == "true" SET ARGS=%2 %3 %4 %5 %6 %7 %8
if not "%SHIFT%" == "true" SET ARGS=%1 %2 %3 %4 %5 %6 %7 %8    
%DIRNAME%\karaf.bat %ARGS%

endlocal

