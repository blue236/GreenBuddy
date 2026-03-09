@echo off
set JAVA_HOME=/home/blue236/.openclaw/workspace/android-env/jdk/jdk-17.0.14+7
set ANDROID_SDK_ROOT=/home/blue236/.openclaw/workspace/android-env/android-sdk
/home/blue236/.openclaw/workspace/android-env/gradle/gradle-8.7/bin/gradle.bat -p %~dp0 %*
