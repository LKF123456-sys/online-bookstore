@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM BookVerse 本地开发环境启动脚本（Windows）
REM 功能：
REM   1. 检查必要的开发工具是否已安装
REM   2. 启动基础设施（MySQL、Redis、Nacos）
REM   3. 构建所有 Maven 模块
REM   4. 按依赖顺序启动各微服务
REM   5. 打印各服务访问地址
REM ============================================================

set "PROJECT_DIR=%~dp0"
set "LOG_DIR=%PROJECT_DIR%logs"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo.
echo ============================================================
echo   BookVerse 本地开发环境启动脚本
echo ============================================================
echo.

REM ---------- Step 1: 检查必要工具 ----------
echo ========== Step 1: 检查开发工具 ==========

set "MISSING=0"

where java >nul 2>&1
if %errorlevel%==0 (
    echo [INFO]  Java 已安装
    java -version 2>&1 | findstr /i "version"
) else (
    echo [ERROR] Java 未安装，请安装 JDK 21: https://adoptium.net/
    set "MISSING=1"
)

where mvn >nul 2>&1
if %errorlevel%==0 (
    echo [INFO]  Maven 已安装
    mvn --version 2>&1 | findstr /i "Apache Maven"
) else (
    echo [ERROR] Maven 未安装，请安装: https://maven.apache.org/download.cgi
    set "MISSING=1"
)

where node >nul 2>&1
if %errorlevel%==0 (
    echo [INFO]  Node.js 已安装
    node --version
) else (
    echo [ERROR] Node.js 未安装，请安装: https://nodejs.org/
    set "MISSING=1"
)

where npm >nul 2>&1
if %errorlevel%==0 (
    echo [INFO]  npm 已安装
    npm --version
) else (
    echo [ERROR] npm 未安装
    set "MISSING=1"
)

where mysql >nul 2>&1
if %errorlevel%==0 (
    echo [INFO]  MySQL CLI 已安装
) else (
    echo [WARN]  MySQL CLI 未安装（可选，Docker 容器内也可使用）
)

where redis-cli >nul 2>&1
if %errorlevel%==0 (
    echo [INFO]  Redis CLI 已安装
) else (
    echo [WARN]  Redis CLI 未安装（可选，Docker 容器内也可使用）
)

if %MISSING%==1 (
    echo.
    echo [ERROR] 缺少必要工具，请安装后重试。
    pause
    exit /b 1
)

REM ---------- Step 2: 检查基础设施 ----------
echo.
echo ========== Step 2: 启动基础设施（Docker） ==========

where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker 未安装，请安装 Docker Desktop: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

echo [INFO]  启动 MySQL ...
docker compose -f "%PROJECT_DIR%docker-compose.yml" up -d mysql
echo [INFO]  等待 MySQL 就绪...
timeout /t 15 /nobreak >nul

echo [INFO]  启动 Redis ...
docker compose -f "%PROJECT_DIR%docker-compose.yml" up -d redis
echo [INFO]  等待 Redis 就绪...
timeout /t 5 /nobreak >nul

echo [INFO]  启动 Nacos ...
docker compose -f "%PROJECT_DIR%docker-compose.yml" up -d nacos
echo [INFO]  等待 Nacos 就绪（可能需要 30-60 秒）...
timeout /t 30 /nobreak >nul

echo [INFO]  基础设施已启动

REM ---------- Step 3: 构建 Maven 模块 ----------
echo.
echo ========== Step 3: 构建 Maven 模块 ==========

cd /d "%PROJECT_DIR%"
echo [INFO]  执行 mvn clean install -DskipTests ...
call mvn clean install -DskipTests -B
if %errorlevel% neq 0 (
    echo [ERROR] Maven 构建失败，请检查编译错误
    pause
    exit /b 1
)
echo [INFO]  Maven 构建完成

REM ---------- Step 4: 按顺序启动微服务 ----------
echo.
echo ========== Step 4: 启动微服务 ==========

set "JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseG1GC"
set "SPRING_PROFILES=-Dspring.profiles.active=dev"
set "DB_OPTS=-Dspring.datasource.url=jdbc:mysql://localhost:3306/bookstore?useUnicode=true^&characterEncoding=utf8^&serverTimezone=Asia/Shanghai^&useSSL=false^&allowPublicKeyRetrieval=true -Dspring.datasource.username=root -Dspring.datasource.password=root"
set "REDIS_OPTS=-Dspring.data.redis.host=localhost"
set "NACOS_OPTS=-Dspring.cloud.nacos.server-addr=localhost:8848"

REM 启动 Gateway (8080)
echo [INFO]  启动 bookstore-gateway (端口 8080) ...
start "bookstore-gateway" /min cmd /c "java %JAVA_OPTS% -Dserver.port=8080 %SPRING_PROFILES% %DB_OPTS% %REDIS_OPTS% %NACOS_OPTS% -jar "%PROJECT_DIR%bookstore-gateway\target\bookstore-gateway-1.0-SNAPSHOT.jar" > "%LOG_DIR%\bookstore-gateway.log" 2>&1"
timeout /t 8 /nobreak >nul

REM 启动 User Service (8081)
echo [INFO]  启动 bookstore-user (端口 8081) ...
start "bookstore-user" /min cmd /c "java %JAVA_OPTS% -Dserver.port=8081 %SPRING_PROFILES% %DB_OPTS% %REDIS_OPTS% %NACOS_OPTS% -jar "%PROJECT_DIR%bookstore-user\target\bookstore-user-1.0-SNAPSHOT.jar" > "%LOG_DIR%\bookstore-user.log" 2>&1"
timeout /t 3 /nobreak >nul

REM 启动 Product Service (8082)
echo [INFO]  启动 bookstore-product (端口 8082) ...
start "bookstore-product" /min cmd /c "java %JAVA_OPTS% -Dserver.port=8082 %SPRING_PROFILES% %DB_OPTS% %REDIS_OPTS% %NACOS_OPTS% -jar "%PROJECT_DIR%bookstore-product\target\bookstore-product-1.0-SNAPSHOT.jar" > "%LOG_DIR%\bookstore-product.log" 2>&1"
timeout /t 3 /nobreak >nul

REM 启动 Order Service (8083)
echo [INFO]  启动 bookstore-order (端口 8083) ...
start "bookstore-order" /min cmd /c "java %JAVA_OPTS% -Dserver.port=8083 %SPRING_PROFILES% %DB_OPTS% %REDIS_OPTS% %NACOS_OPTS% -jar "%PROJECT_DIR%bookstore-order\target\bookstore-order-1.0-SNAPSHOT.jar" > "%LOG_DIR%\bookstore-order.log" 2>&1"
timeout /t 3 /nobreak >nul

REM 启动 Promotion Service (8085)
echo [INFO]  启动 bookstore-promotion (端口 8085) ...
start "bookstore-promotion" /min cmd /c "java %JAVA_OPTS% -Dserver.port=8085 %SPRING_PROFILES% %DB_OPTS% %REDIS_OPTS% %NACOS_OPTS% -jar "%PROJECT_DIR%bookstore-promotion\target\bookstore-promotion-1.0-SNAPSHOT.jar" > "%LOG_DIR%\bookstore-promotion.log" 2>&1"
timeout /t 3 /nobreak >nul

REM 启动 Admin Service (8086)
echo [INFO]  启动 bookstore-admin (端口 8086) ...
start "bookstore-admin" /min cmd /c "java %JAVA_OPTS% -Dserver.port=8086 %SPRING_PROFILES% %DB_OPTS% %REDIS_OPTS% %NACOS_OPTS% -jar "%PROJECT_DIR%bookstore-admin\target\bookstore-admin-1.0-SNAPSHOT.jar" > "%LOG_DIR%\bookstore-admin.log" 2>&1"
timeout /t 3 /nobreak >nul

REM 启动 Message Service (8087)
echo [INFO]  启动 bookstore-message (端口 8087) ...
start "bookstore-message" /min cmd /c "java %JAVA_OPTS% -Dserver.port=8087 %SPRING_PROFILES% %DB_OPTS% %REDIS_OPTS% %NACOS_OPTS% -jar "%PROJECT_DIR%bookstore-message\target\bookstore-message-1.0-SNAPSHOT.jar" > "%LOG_DIR%\bookstore-message.log" 2>&1"
timeout /t 3 /nobreak >nul

REM ---------- Step 5: 打印访问地址 ----------
echo.
echo ============================================================
echo    BookVerse 本地开发环境已启动！
echo ============================================================
echo.
echo   基础设施:
echo     MySQL:          jdbc:mysql://localhost:3306/bookstore
echo     Redis:          redis://localhost:6379
echo     Nacos Console:  http://localhost:8848/nacos  (nacos/nacos)
echo.
echo   微服务:
echo     Gateway:        http://localhost:8080
echo     User Service:   http://localhost:8081
echo     Product Service:http://localhost:8082
echo     Order Service:  http://localhost:8083
echo     Promotion:      http://localhost:8085
echo     Admin Service:  http://localhost:8086
echo     Message Service:http://localhost:8087
echo.
echo   前端（需手动启动，在新终端窗口中执行）:
echo     User Frontend:  cd bookstore-frontend ^&^& npm run dev
echo     Admin Frontend: cd bookstore-admin-frontend ^&^& npm run dev
echo.
echo   日志目录: %LOG_DIR%
echo.
echo   停止所有服务: 关闭所有 "bookstore-*" 窗口，或执行:
echo     for /f "tokens=2" %a in ('tasklist /fi "WINDOWTITLE eq bookstore-*" /fo list ^| findstr "PID:"') do taskkill /PID %a /F
echo.
pause
