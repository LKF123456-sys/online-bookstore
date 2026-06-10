#!/usr/bin/env bash
# ============================================================
# BookVerse 本地开发环境启动脚本（Linux / macOS）
# 功能：
#   1. 检查必要的开发工具是否已安装
#   2. 启动基础设施（MySQL、Redis、Nacos）
#   3. 构建所有 Maven 模块
#   4. 按依赖顺序启动各微服务
#   5. 打印各服务访问地址
# ============================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录（脚本所在目录）
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_DIR/logs"
mkdir -p "$LOG_DIR"

# ---------- 工具函数 ----------
log_info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()  { echo -e "\n${CYAN}========== $1 ==========${NC}"; }

check_command() {
    local cmd=$1
    local name=$2
    if command -v "$cmd" &>/dev/null; then
        local version
        version=$("$cmd" --version 2>&1 | head -1)
        log_info "$name 已安装: $version"
        return 0
    else
        log_error "$name 未安装，请先安装 $name"
        return 1
    fi
}

# ---------- Step 1: 检查必要工具 ----------
log_step "Step 1: 检查开发工具"

MISSING=0
check_command "java"  "Java 21"   || MISSING=1
check_command "mvn"   "Maven"     || MISSING=1
check_command "node"  "Node.js"   || MISSING=1
check_command "npm"   "npm"       || MISSING=1
check_command "mysql" "MySQL CLI" || MISSING=1
check_command "redis-cli" "Redis CLI" || MISSING=1

if [ "$MISSING" -eq 1 ]; then
    echo ""
    log_error "缺少必要工具，请安装后重试。"
    log_warn "提示："
    log_warn "  Java 21:    https://adoptium.net/"
    log_warn "  Maven:      https://maven.apache.org/download.cgi"
    log_warn "  Node.js:    https://nodejs.org/"
    log_warn "  MySQL:      https://dev.mysql.com/downloads/"
    log_warn "  Redis:      https://redis.io/download/"
    exit 1
fi

# ---------- Step 2: 检查基础设施 ----------
log_step "Step 2: 检查基础设施状态"

# 检查 MySQL 是否可用
if mysqladmin ping -h 127.0.0.1 -u root -proot &>/dev/null; then
    log_info "MySQL 已在本地运行"
else
    log_warn "MySQL 未运行，正在通过 Docker 启动 MySQL..."
    docker compose -f "$PROJECT_DIR/docker-compose.yml" up -d mysql
    log_info "等待 MySQL 就绪..."
    for i in $(seq 1 30); do
        if mysqladmin ping -h 127.0.0.1 -u root -proot &>/dev/null 2>&1 || \
           docker exec bookstore-mysql mysqladmin ping -h localhost -u root -proot &>/dev/null 2>&1; then
            log_info "MySQL 已就绪 (${i}s)"
            break
        fi
        sleep 2
    done
fi

# 检查 Redis 是否可用
if redis-cli ping 2>/dev/null | grep -q PONG; then
    log_info "Redis 已在本地运行"
else
    log_warn "Redis 未运行，正在通过 Docker 启动 Redis..."
    docker compose -f "$PROJECT_DIR/docker-compose.yml" up -d redis
    log_info "等待 Redis 就绪..."
    for i in $(seq 1 15); do
        if redis-cli ping 2>/dev/null | grep -q PONG; then
            log_info "Redis 已就绪 (${i}s)"
            break
        fi
        sleep 1
    done
fi

# 启动 Nacos（通常本地开发不直接运行 Nacos，这里通过 Docker 启动）
log_warn "正在通过 Docker 启动 Nacos..."
docker compose -f "$PROJECT_DIR/docker-compose.yml" up -d nacos
log_info "等待 Nacos 就绪..."
for i in $(seq 1 45); do
    if curl -sf http://localhost:8848/nacos/v1/console/health/readiness &>/dev/null; then
        log_info "Nacos 已就绪 (${i}s)"
        break
    fi
    sleep 2
done

# ---------- Step 3: 构建 Maven 模块 ----------
log_step "Step 3: 构建 Maven 模块"

cd "$PROJECT_DIR"
log_info "执行 mvn clean install -DskipTests ..."
mvn clean install -DskipTests -B
log_info "Maven 构建完成"

# ---------- Step 4: 按顺序启动微服务 ----------
log_step "Step 4: 启动微服务"

# 服务定义：模块名:端口
SERVICES=(
    "bookstore-gateway:8080"
    "bookstore-user:8081"
    "bookstore-product:8082"
    "bookstore-order:8083"
    "bookstore-promotion:8085"
    "bookstore-admin:8086"
    "bookstore-message:8087"
)

PIDS=()

start_service() {
    local module=$1
    local port=$2
    local jar_path="$PROJECT_DIR/$module/target/$module-1.0-SNAPSHOT.jar"

    if [ ! -f "$jar_path" ]; then
        # 尝试不带版本号查找
        jar_path=$(find "$PROJECT_DIR/$module/target" -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" 2>/dev/null | head -1)
    fi

    if [ -z "$jar_path" ] || [ ! -f "$jar_path" ]; then
        log_error "未找到 $module 的 jar 包，跳过"
        return 1
    fi

    log_info "启动 $module (端口 $port)..."
    nohup java \
        -Xms128m -Xmx256m \
        -XX:+UseG1GC \
        -Dserver.port="$port" \
        -Dspring.profiles.active=dev \
        -Dspring.datasource.url="jdbc:mysql://localhost:3306/bookstore?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" \
        -Dspring.datasource.username=root \
        -Dspring.datasource.password=root \
        -Dspring.data.redis.host=localhost \
        -Dspring.cloud.nacos.server-addr=localhost:8848 \
        -jar "$jar_path" \
        > "$LOG_DIR/$module.log" 2>&1 &

    local pid=$!
    PIDS+=("$pid")
    log_info "$module 已启动 (PID: $pid)"
}

for svc in "${SERVICES[@]}"; do
    IFS=':' read -r module port <<< "$svc"
    start_service "$module" "$port"
    # 网关先启动，等待几秒再启动其他服务
    if [ "$module" = "bookstore-gateway" ]; then
        sleep 5
    else
        sleep 2
    fi
done

# ---------- Step 5: 打印访问地址 ----------
log_step "Step 5: 服务访问地址"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}   BookVerse 本地开发环境已启动！${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo -e "  ${GREEN}基础设施:${NC}"
echo -e "    MySQL:          ${CYAN}jdbc:mysql://localhost:3306/bookstore${NC}"
echo -e "    Redis:          ${CYAN}redis://localhost:6379${NC}"
echo -e "    Nacos Console:  ${CYAN}http://localhost:8848/nacos${NC}  (nacos/nacos)"
echo ""
echo -e "  ${GREEN}微服务:${NC}"
echo -e "    Gateway:        ${CYAN}http://localhost:8080${NC}"
echo -e "    User Service:   ${CYAN}http://localhost:8081${NC}"
echo -e "    Product Service:${CYAN}http://localhost:8082${NC}"
echo -e "    Order Service:  ${CYAN}http://localhost:8083${NC}"
echo -e "    Promotion:      ${CYAN}http://localhost:8085${NC}"
echo -e "    Admin Service:  ${CYAN}http://localhost:8086${NC}"
echo -e "    Message Service:${CYAN}http://localhost:8087${NC}"
echo ""
echo -e "  ${GREEN}前端（需手动启动）:${NC}"
echo -e "    User Frontend:  ${CYAN}cd bookstore-frontend && npm run dev${NC}"
echo -e "    Admin Frontend: ${CYAN}cd bookstore-admin-frontend && npm run dev${NC}"
echo ""
echo -e "  ${YELLOW}日志目录: $LOG_DIR${NC}"
echo -e "  ${YELLOW}停止所有服务: kill ${PIDS[*]}${NC}"
echo ""

# 等待所有后台进程
echo -e "${GREEN}按 Ctrl+C 停止所有服务...${NC}"
wait
