@echo off
echo ============================================
echo Blitz Smart Mall - Local Start
echo ============================================

echo Stopping old processes...
wmic process where "name='java.exe'" call terminate 2>nul

echo Starting server on port 8080...
C:\Java\jdk-21.0.11+10\bin\javaw -jar C:\tmp\blitz-local-v2.jar --spring.profiles.active=minimal --spring.datasource.password=123456 > C:\tmp\blitz-local.log 2>&1

echo Waiting 40 seconds for startup...
ping -n 41 127.0.0.1 > nul

start http://localhost:8080

echo ============================================
echo Server at http://localhost:8080
echo Log at C:\tmp\blitz-local.log
echo ============================================
pause
