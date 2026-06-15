@echo off
cd /d C:\project1\carbonbuddy-api
set PATH=%TEMP%\apache-maven-3.9.6\bin;%PATH%
start /B /MIN java -jar target\carbonbuddy-api-1.0.0.jar --spring.profiles.active=demo --server.port=8082 > target\app3.log 2>&1
echo App starting on port 8082...
