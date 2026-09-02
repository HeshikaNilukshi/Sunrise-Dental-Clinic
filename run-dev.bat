@echo off
set JAVA_HOME=E:\java
start "Tailwind Watcher" /min cmd /k "tailwindcss.exe -i src/main/resources/static/css/input.css -o src/main/resources/static/css/output.css --watch"

echo    ▲ Spring Boot
echo    - Local:        http://localhost:8080
echo    - Environments: default
echo.
echo  starting dev...
echo.

call .\mvnw.cmd spring-boot:run