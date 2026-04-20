@echo off
set PROJECT_DIR=%~dp0
for %%I in ("%PROJECT_DIR%..") do set WORKSPACE_DIR=%%~fI
set JAVA_HOME=%WORKSPACE_DIR%\android-env\jdk\jdk-17.0.14+7
set ANDROID_SDK_ROOT=%WORKSPACE_DIR%\android-env\android-sdk
"%WORKSPACE_DIR%\android-env\gradle\gradle-8.7\bin\gradle.bat" -p %PROJECT_DIR% %*
