#!/bin/bash
# ============================================================
# BookVerse AI Agent — 性能基准测试脚本
# 使用 wrk 工具对 SSE 流式和同步对话端点进行压测
#
# 前提条件：
#   1. wrk 已安装（brew install wrk / apt install wrk）
#   2. 所有微服务已启动并注册到 Nacos
#   3. 网关（8080）可达
#   4. AI 模型服务可用（OpenAI API 或本地 Ollama）
#
# 使用方法：
#   chmod +x benchmark.sh
#   ./benchmark.sh
# ============================================================

GATEWAY_HOST="http://localhost:8080"
USER_ID="test-user-001"
SESSION_ID="bench-$(date +%s)"

echo "=============================================="
echo "  BookVerse AI Agent 性能基准测试"
echo "  目标: ${GATEWAY_HOST}"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=============================================="

# ----------------------------------------------------------
# 测试 1: 同步对话端点（POST /api/agent/chat）
# 并发 10 个连接，持续 30 秒
# ----------------------------------------------------------
echo ""
echo ">>> 测试 1: 同步对话 (POST /api/agent/chat)"
echo "    并发: 10 连接, 持续: 30 秒"
echo "---"

# wrk 不支持 POST，使用 Lua 脚本
cat > /tmp/chat_post.lua << 'EOF'
wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"
wrk.headers["X-User-Id"] = "test-user-001"
wrk.body = '{"message":"推荐一本好书","sessionId":"bench-session","agentType":"auto"}'
EOF

wrk -t2 -c10 -d30s -s /tmp/chat_post.lua ${GATEWAY_HOST}/api/agent/chat 2>&1 | tee /tmp/bench_sync.txt

# ----------------------------------------------------------
# 测试 2: SSE 流式端点（GET /api/agent/chat/stream）
# 并发 20 个连接，持续 30 秒
# ----------------------------------------------------------
echo ""
echo ">>> 测试 2: SSE 流式对话 (GET /api/agent/chat/stream)"
echo "    并发: 20 连接, 持续: 30 秒"
echo "---"

STREAM_URL="${GATEWAY_HOST}/api/agent/chat/stream?message=你好&sessionId=${SESSION_ID}&agentType=auto"

wrk -t2 -c20 -d30s -H "X-User-Id: ${USER_ID}" "${STREAM_URL}" 2>&1 | tee /tmp/bench_stream.txt

# ----------------------------------------------------------
# 测试 3: 历史查询端点（GET /api/agent/history）
# 并发 50 个连接，持续 15 秒（纯 Redis + MySQL 查询）
# ----------------------------------------------------------
echo ""
echo ">>> 测试 3: 历史查询 (GET /api/agent/history)"
echo "    并发: 50 连接, 持续: 15 秒"
echo "---"

HISTORY_URL="${GATEWAY_HOST}/api/agent/history?sessionId=${SESSION_ID}"

wrk -t2 -c50 -d15s -H "X-User-Id: ${USER_ID}" "${HISTORY_URL}" 2>&1 | tee /tmp/bench_history.txt

# ----------------------------------------------------------
# 测试 4: 意图分类延迟（单请求测量）
# ----------------------------------------------------------
echo ""
echo ">>> 测试 4: 单次意图分类延迟"
echo "---"

for i in $(seq 1 5); do
    START=$(date +%s%3N)
    curl -s -o /dev/null -w "" \
        -H "X-User-Id: ${USER_ID}" \
        -H "Content-Type: application/json" \
        -d '{"message":"帮我取消订单ORD-123","sessionId":"latency-test","agentType":"auto"}' \
        ${GATEWAY_HOST}/api/agent/chat
    END=$(date +%s%3N)
    echo "  请求 #${i}: $((END - START)) ms"
done

# ----------------------------------------------------------
# 汇总
# ----------------------------------------------------------
echo ""
echo "=============================================="
echo "  压测结果已保存到："
echo "    /tmp/bench_sync.txt    — 同步对话"
echo "    /tmp/bench_stream.txt  — SSE 流式"
echo "    /tmp/bench_history.txt — 历史查询"
echo "=============================================="
echo ""
echo "提示：使用以下命令查看详细结果："
echo "  cat /tmp/bench_sync.txt"
echo "  cat /tmp/bench_stream.txt"
echo "  cat /tmp/bench_history.txt"
