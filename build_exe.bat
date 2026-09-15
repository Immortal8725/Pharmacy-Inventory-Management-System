@echo off
setlocal
cd /d "%~dp0"

set "CSC=%WINDIR%\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if not exist "%CSC%" set "CSC=%WINDIR%\Microsoft.NET\Framework\v4.0.30319\csc.exe"
if not exist "%CSC%" (
  echo Could not find csc.exe. Install .NET Framework 4.x Developer Pack or use healthfirst_pims.bat.
  exit /b 1
)

"%CSC%" /nologo /target:winexe /optimize+ /reference:System.Windows.Forms.dll /out:healthfirst_pims.exe launcher.cs
if errorlevel 1 exit /b 1
echo Built healthfirst_pims.exe
echo Rename to YourName_pims.exe before submission.
