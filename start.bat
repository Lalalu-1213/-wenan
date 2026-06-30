@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title 智能文案生成助手 - 一键启动

echo.
echo =============================================
echo     智能文案生成助手 - 一键启动中...
echo =============================================
echo.

:: ============================================
:: 1. 检测 Java 环境
:: ============================================
echo [1/5] 检测 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java，请安装 JDK 17 或更高版本
    echo 下载地址: https://adoptium.net/zh-CN/download/
    pause
    exit /b 1
)
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%i
echo [通过] Java 已就绪: !JAVA_VER!

:: ============================================
:: 2. 检测 Node.js 环境
:: ============================================
echo [2/5] 检测 Node.js 环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Node.js，请安装 Node.js 18 或更高版本
    echo 下载地址: https://nodejs.org/zh-cn/
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('node --version') do set NODE_VER=%%i
echo [通过] Node.js 已就绪: !NODE_VER!

:: ============================================
:: 3. 检测 MySQL 服务
:: ============================================
echo [3/5] 检测 MySQL 服务...
set MYSQL_OK=0

:: 方式1: 尝试 mysqladmin ping
mysqladmin ping -h localhost -u root -p111111 2>nul | findstr "alive" >nul
if %errorlevel% equ 0 (
    set MYSQL_OK=1
    echo [通过] MySQL 连接正常
)

:: 方式2: 尝试 TCP 端口探测
if !MYSQL_OK! equ 0 (
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue" 2>nul | findstr "True" >nul
    if %errorlevel% equ 0 (
        set MYSQL_OK=1
        echo [通过] MySQL 端口 3306 已监听
    )
)

:: 方式3: 检查 Windows 服务
if !MYSQL_OK! equ 0 (
    sc query MySQL80 >nul 2>&1
    if %errorlevel% equ 0 (
        for /f "tokens=*" %%i in ('sc query MySQL80 ^| findstr "STATE"') do set MYSQL_STATE=%%i
        echo !MYSQL_STATE! | findstr "RUNNING" >nul
        if %errorlevel% equ 0 (
            set MYSQL_OK=1
            echo [通过] MySQL80 服务正在运行
        )
    )
)

if !MYSQL_OK! equ 0 (
    echo [警告] MySQL 服务未检测到运行，尝试启动...
    sc start MySQL80 >nul 2>&1
    if %errorlevel% equ 0 (
        echo [信息] 已发送 MySQL80 启动命令，等待就绪...
        timeout /t 5 /nobreak >nul
        set MYSQL_OK=1
    ) else (
        echo [错误] 无法启动 MySQL，请手动启动后重试
        echo   服务名可能不是 MySQL80，请检查 Windows 服务列表
        pause
        exit /b 1
    )
)

:: ============================================
:: 4. 安装前端依赖（如果未安装）
:: ============================================
echo [4/5] 检查前端依赖...
if not exist "front\node_modules" (
    echo [信息] 首次运行，正在安装前端依赖...
    cd front
    call npm install
    cd ..
    if %errorlevel% neq 0 (
        echo [错误] 前端依赖安装失败
        pause
        exit /b 1
    )
    echo [通过] 前端依赖安装完成
) else (
    echo [通过] 前端依赖已就绪
)

:: ============================================
:: 5. 启动后端
:: ============================================
echo [5/5] 启动后端服务...

:: 杀掉可能残留的后端进程（端口 8080）
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)

:: 在新窗口中启动后端
start "智能文案助手 - 后端" cmd /k "cd /d %~dp0back && mvnw.cmd spring-boot:run"

:: 等待后端就绪
echo.
echo 等待后端启动（最多等待 120 秒）...
set /a COUNT=0

:WAIT_BACKEND
timeout /t 3 /nobreak >nul
set /a COUNT+=3

:: 使用 PowerShell 轮询健康检查接口
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8080/api/copywriting/health' -UseBasicParsing -TimeoutSec 3; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 goto BACKEND_READY

if !COUNT! geq 120 (
    echo [警告] 后端启动超时，将继续启动前端（后端可能需要更多时间）
    goto START_FRONTEND
)

echo 已等待 !COUNT! 秒，继续等待...
goto WAIT_BACKEND

:BACKEND_READY
echo [通过] 后端已就绪 (localhost:8080)

:: ============================================
:: 6. 启动前端
:: ============================================
:START_FRONTEND
echo 启动前端开发服务器...

:: 杀掉可能残留的前端进程（端口 5173）
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)

:: 在新窗口中启动前端
start "智能文案助手 - 前端" cmd /k "cd /d %~dp0front && npm run dev"

:: 等待前端 Vite 启动
echo 等待前端就绪...
set /a COUNT2=0

:WAIT_FRONTEND
timeout /t 2 /nobreak >nul
set /a COUNT2+=2

powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:5173' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 goto FRONTEND_READY

if !COUNT2! geq 60 (
    echo.
    echo =============================================
    echo     前端可能仍在启动中，请稍后手动访问
    echo =============================================
    goto SHOW_URL
)

goto WAIT_FRONTEND

:FRONTEND_READY
echo [通过] 前端已就绪

:: ============================================
:: 完成
:: ============================================
:SHOW_URL
echo.
echo =============================================
echo     ★ 启动完成！★
echo =============================================
echo.
echo   前端访问地址:  http://localhost:5173
echo   后端健康检查:  http://localhost:8080/api/copywriting/health
echo.
echo   后端和前端在独立窗口中运行，关闭对应窗口即可停止服务
echo.
echo =============================================
echo.
echo 按任意键关闭此窗口（不影响后端和前端运行）...
pause >nul
endlocal