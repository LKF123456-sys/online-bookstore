#!/bin/bash
# ============================================================
# 在线书店 API 性能基准测试脚本 (Benchmark Script)
# ============================================================
# 功能：使用 curl + time 对关键API接口进行压力测试
# 统计：每个接口运行100次请求，计算平均/最小/最大响应时间
# 输出：格式化的测试结果表格
#
# 使用方法：
#   chmod +x run-benchmark.sh
#   ./run-benchmark.sh
#   ./run-benchmark.sh http://localhost:8080  # 指定网关地址
#
# 注意：
#   - 测试前请确保目标服务正在运行
#   - 生产环境慎用，大量请求可能对服务造成压力
# ============================================================

# ==================== 配置参数 ====================

# 网关地址（默认localhost:8080，可通过命令行参数覆盖）
BASE_URL="${1:-http://localhost:8080}"

# 每个接口的测试次数
REQUEST_COUNT=100

# 临时文件，用于存储每次请求的响应时间
TMPFILE=$(mktemp /tmp/benchmark_XXXXXX.txt)

# 颜色定义（终端输出美化）
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ==================== 工具函数 ====================

# 打印分隔线
print_separator() {
    echo "+------------------+--------+-----------+-----------+-----------+-----------+"
}

# 打印表格头部
print_header() {
    echo ""
    echo -e "${CYAN}======================================================${NC}"
    echo -e "${CYAN}   在线书店 API 性能基准测试报告${NC}"
    echo -e "${CYAN}======================================================${NC}"
    echo -e "${BLUE}目标地址: ${BASE_URL}${NC}"
    echo -e "${BLUE}每接口请求数: ${REQUEST_COUNT}${NC}"
    echo -e "${BLUE}测试时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo ""
    print_separator
    printf "| %-16s | %-6s | %-9s | %-9s | %-9s | %-9s |\n" \
        "接口" "次数" "平均(ms)" "最小(ms)" "最大(ms)" "成功率"
    print_separator
}

# 执行基准测试
# 参数：$1=接口名称, $2=HTTP方法(GET/POST), $3=URL路径, $4=POST请求体(可选), $5=Content-Type(可选)
run_benchmark() {
    local name="$1"
    local method="$2"
    local path="$3"
    local body="$4"
    local content_type="$5"
    local url="${BASE_URL}${path}"

    local total_time=0
    local min_time=999999
    local max_time=0
    local success_count=0

    # 清空临时文件
    > "$TMPFILE"

    for i in $(seq 1 $REQUEST_COUNT); do
        local start_time end_time elapsed http_code

        # 使用 date +%s%N 获取纳秒级时间戳
        start_time=$(date +%s%N)

        if [ "$method" = "POST" ] && [ -n "$body" ]; then
            # POST 请求（带请求体）
            http_code=$(curl -s -o /dev/null -w "%{http_code}" \
                -X POST \
                -H "Content-Type: ${content_type:-application/json}" \
                -d "$body" \
                "$url" 2>/dev/null)
        else
            # GET 请求
            http_code=$(curl -s -o /dev/null -w "%{http_code}" \
                -X GET \
                "$url" 2>/dev/null)
        fi

        end_time=$(date +%s%N)

        # 计算响应时间（毫秒）
        elapsed=$(( (end_time - start_time) / 1000000 ))

        # 判断请求是否成功（HTTP 2xx 或 3xx）
        if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 400 ] 2>/dev/null; then
            success_count=$((success_count + 1))
        fi

        # 更新统计值
        total_time=$((total_time + elapsed))
        if [ "$elapsed" -lt "$min_time" ]; then
            min_time=$elapsed
        fi
        if [ "$elapsed" -gt "$max_time" ]; then
            max_time=$elapsed
        fi
    done

    # 计算平均值
    local avg_time=$((total_time / REQUEST_COUNT))
    local success_rate=$(awk "BEGIN {printf \"%.1f\", ($success_count / $REQUEST_COUNT) * 100}")

    # 处理没有成功请求的情况
    if [ "$success_count" -eq 0 ]; then
        min_time=0
    fi

    # 输出结果行
    printf "| %-16s | %-6s | %-9s | %-9s | %-9s | %-8s%% |\n" \
        "$name" "$REQUEST_COUNT" "$avg_time" "$min_time" "$max_time" "$success_rate"

    # 根据成功率设置颜色提示
    if (( $(echo "$success_rate < 50" | bc -l 2>/dev/null || echo 0) )); then
        echo -e "  ${RED}  ^ 警告: 成功率低于50%，请检查服务是否正常运行${NC}"
    fi
}

# ==================== 主测试流程 ====================

# 打印表格头部
print_header

# ---- 测试1: 商品列表接口 ----
run_benchmark "商品列表" "GET" "/api/product/list?pageNum=1&pageSize=10"

# ---- 测试2: 商品详情接口 ----
# 先尝试获取一个有效的商品ID
SAMPLE_PRODUCT_ID=$(curl -s "${BASE_URL}/api/product/list?pageNum=1&pageSize=1" 2>/dev/null \
    | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$SAMPLE_PRODUCT_ID" ]; then
    SAMPLE_PRODUCT_ID="1"  # 使用默认ID
fi

run_benchmark "商品详情" "GET" "/api/product/${SAMPLE_PRODUCT_ID}"

# ---- 测试3: 用户登录接口 ----
# 使用测试账号进行登录测试
LOGIN_BODY='{"username":"admin","password":"admin123"}'
run_benchmark "用户登录" "POST" "/api/auth/login" "$LOGIN_BODY" "application/json"

# ---- 测试4: 健康检查接口 ----
run_benchmark "健康检查" "GET" "/actuator/health"

# ---- 测试5: 推荐商品接口 ----
run_benchmark "推荐商品" "GET" "/api/product/recommend?limit=5"

# 打印表格底部
print_separator

# ==================== 输出统计摘要 ====================

echo ""
echo -e "${GREEN}======================================================${NC}"
echo -e "${GREEN}   测试完成!${NC}"
echo -e "${GREEN}======================================================${NC}"
echo ""
echo -e "${YELLOW}提示:${NC}"
echo "  - 响应时间包含网络传输开销（非纯服务端处理时间）"
echo "  - 结果受当前系统负载和网络状况影响"
echo "  - 如需更精确的测试，建议使用 JMeter 或 wrk 等专业工具"
echo "  - 生产环境压测请在业务低峰期进行"
echo ""

# 清理临时文件
rm -f "$TMPFILE"
