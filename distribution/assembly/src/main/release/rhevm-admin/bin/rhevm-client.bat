@echo off

setlocal

set DIRNAME=%~dp0%
set ARGS=%*

title Rhevm-Client

set KARAF_HOME=%DIRNAME%..
if not exist "%KARAF_HOME%" (
    call :warn KARAF_HOME is not valid: %KARAF_HOME%
    goto END
)

if "%SHIFT%" == "true" SET ARGS=%2 %3 %4 %5 %6 %7 %8
if not "%SHIFT%" == "true" SET ARGS=%1 %2 %3 %4 %5 %6 %7 %8    
cd %KARAF_HOME%
java -jar lib/karaf-client.jar -r 10 -d 5  %ARGS%

:END

endlocal

