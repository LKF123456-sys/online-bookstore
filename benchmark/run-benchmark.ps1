param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$RequestCount = 100
)

# ============================================================
# BookVerse 在线书店 API 性能基准测试脚本（PowerShell）
# ============================================================
# 使用 curl.exe 对关键 API 进行压力测试
# 统计：每个接口运行指定次数请求，计算平均/最小/最大响应时间
# ============================================================

$ErrorActionPreference = "Stop"
$results = @()

Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   BookVerse API 性能基准测试报告" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "目标地址: $BaseUrl" -ForegroundColor Blue
Write-Host "每接口请求数: $RequestCount" -ForegroundColor Blue
Write-Host "测试时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Blue
Write-Host ""

function Run-Benchmark {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Path,
        [string]$Body = $null,
        [string]$ContentType = "application/json"
    )

    $url = "$BaseUrl$Path"
    $totalTime = 0
    $minTime = [long]::MaxValue
    $maxTime = [long]::MinValue
    $successCount = 0
    $times = @()

    Write-Host " 测试 $Name ... " -NoNewline

    for ($i = 0; $i -lt $RequestCount; $i++) {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        
        try {
            if ($Method -eq "POST" -and $Body) {
                $response = curl.exe -s -o nul -w "%{http_code}" -X POST -H "Content-Type: $ContentType" -d $Body $url 2>$null
            } else {
                $response = curl.exe -s -o nul -w "%{http_code}" -X GET $url 2>$null
            }
            $sw.Stop()
            $elapsed = $sw.ElapsedMilliseconds
            
            if ($response -ge 200 -and $response -lt 400) { $successCount++ }
        } catch {
            $sw.Stop()
            $elapsed = $sw.ElapsedMilliseconds
        }

        $totalTime += $elapsed
        $times += $elapsed
        if ($elapsed -lt $minTime) { $minTime = $elapsed }
        if ($elapsed -gt $maxTime) { $maxTime = $elapsed }
    }

    $avgTime = [math]::Round($totalTime / $RequestCount, 2)
    $successRate = [math]::Round(($successCount / $RequestCount) * 100, 1)
    
    # 计算 P50, P95, P99
    $sorted = $times | Sort-Object
    $p50 = $sorted[[math]::Floor($RequestCount * 0.50)]
    $p95 = $sorted[[math]::Floor($RequestCount * 0.95)]
    $p99 = $sorted[[math]::Floor($RequestCount * 0.99)]

    Write-Host "完成 ($successCount/$RequestCount 成功)" -ForegroundColor Green

    $result = [PSCustomObject]@{
        Name = $Name
        Method = $Method
        Path = $Path
        Count = $RequestCount
        SuccessRate = $successRate
        AvgMs = $avgTime
        MinMs = $minTime
        MaxMs = $maxTime
        P50Ms = $p50
        P95Ms = $p95
        P99Ms = $p99
    }
    
    $global:results += $result
    return $result
}

# ==================== 执行测试 ====================

# 1. 健康检查
Run-Benchmark -Name "健康检查" -Path "/actuator/health"

# 2. 商品列表
Run-Benchmark -Name "商品列表" -Path "/api/product/list?pageNum=1&pageSize=10"

# 3. 商品详情
$pid = (curl.exe -s "$BaseUrl/api/product/list?pageNum=1&pageSize=1" 2>$null | Select-String -Pattern '"id":"[^"]*"' | Select-Object -First 1)
if (-not $pid) { $pid = "1" } else { $pid = $pid.Matches.Value -replace '"id":"|"', '' }
Run-Benchmark -Name "商品详情" -Path "/api/product/$pid"

# 4. 分类列表
Run-Benchmark -Name "分类列表" -Path "/api/category/list"

# 5. 用户登录
$loginBody = '{"username":"admin","password":"admin123"}'
Run-Benchmark -Name "用户登录" -Method "POST" -Path "/api/auth/login" -Body $loginBody

# 6. 商品搜索
Run-Benchmark -Name "商品搜索" -Path "/api/products/search?keyword=Java"

# 7. 推荐商品
Run-Benchmark -Name "推荐商品" -Path "/api/product/recommend?limit=5"

# 8. 热点商品
Run-Benchmark -Name "热点商品" -Path "/api/product/hot?limit=5"

# ==================== 输出结果 ====================

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   测试结果汇总" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host ("{0,-14} {1,7} {2,8} {3,8} {4,8} {5,8} {6,8} {7,8}" -f "接口", "请求数", "平均(ms)", "最小(ms)", "最大(ms)", "P50(ms)", "P95(ms)", "成功率%")
Write-Host ("{0,-14} {1,7} {2,8} {3,8} {4,8} {5,8} {6,8} {7,8}" -f "------", "------", "--------", "--------", "--------", "-------", "-------", "-------")

foreach ($r in $results) {
    Write-Host ("{0,-14} {1,7} {2,8} {3,8} {4,8} {5,8} {6,8} {7,7}%" -f $r.Name, $r.Count, $r.AvgMs, $r.MinMs, $r.MaxMs, $r.P50Ms, $r.P95Ms, $r.SuccessRate)
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Green
Write-Host "   测试完成!" -ForegroundColor Green
Write-Host "======================================================" -ForegroundColor Green
Write-Host ""
Write-Host "提示:"
Write-Host "  - 响应时间包含网络传输开销（非纯服务端处理时间）"
Write-Host "  - 结果受当前系统负载和网络状况影响"
Write-Host "  - 如需更精确的测试，建议使用 wrk2 / JMeter"
Write-Host "  - 生产环境压测请在业务低峰期进行"
