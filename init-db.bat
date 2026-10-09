@echo off
echo ============================================
echo  Blitz 数据库初始化
echo ============================================
set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
echo.
echo [1/2] 创建数据库...
%MYSQL% -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS blitz DEFAULT CHARACTER SET utf8mb4" 2>nul
echo OK

echo [2/2] 执行建表脚本...
for %%f in (blitz-server\src\main\resources\db\migration\V*.sql) do (
    echo   %%~nxf
    %MYSQL% -u root -p123456 blitz < "%%f" 2>nul
)
echo.
echo ============================================
echo  数据库初始化完成！
echo  执行 start-local.bat 启动项目
echo ============================================
pause
