@echo off
setlocal
cd /d "%~dp0"
java -jar "%~dp0dist\healthfirst_pims.jar" %*
if errorlevel 1 pause
