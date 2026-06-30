#!/bin/bash
# 智能文案生成助手 - 一键启动脚本 (Unix / Git Bash / macOS)

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_PORT=8080
FRONTEND_PORT=5173
HEALTH_URL="http://localhost:${BACKEND_PORT}/api/copywriting/health"
FRONTEND_URL="http://localhost:${FRONTEND_PORT}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo ""
echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}    智能文案生成助手 - 一键启动中...${NC}"
echo -e "${CYAN}=============================================${NC}"
echo ""

# ============================================
# 1. 检测 Java 环境
# ============================================
echo -e "[1/5] 检测 Java 环境..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}[错误] 未找到 Java，请安装 JDK 17 或更高版本${NC}"
    exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo -e "${GREEN}[通过] Java 已就绪: ${JAVA_VER}${NC}"

# ============================================
# 2. 检测 Node.js 环境
# ============================================
echo -e "[2/5] 检测 Node.js 环境..."
if ! command -v node &> /dev/null; then
    echo -e "${RED}[错误] 未找到 Node.js，请安装 Node.js 18 或更高版本${NC}"
    exit 1
fi
NODE_VER=$(node --version)
echo -e "${GREEN}[通过] Node.js 已就绪: ${NODE_VER}${NC}"

# ============================================
# 3. 检测 MySQL 服务
# ============================================
echo -e "[3/5] 检测 MySQL 服务..."
MYSQL_OK=0

# 尝试 TCP 连接检测
if command -v nc &> /dev/null; then
    if nc -z localhost 3306 2>/dev/null; then
        MYSQL_OK=1
        echo -e "${GREEN}[通过] MySQL 端口 3306 已监听${NC}"
    fi
fi

# 替代方案：使用 /dev/tcp (bash built-in)
if [ "$MYSQL_OK" -eq 0 ] && command -v timeout &> /dev/null; then
    timeout 2 bash -c 'echo > /dev/tcp/localhost/3306' 2>/dev/null && MYSQL_OK=1 && echo -e "${GREEN}[通过] MySQL 端口 3306 可连接${NC}" || true
fi

# 尝试 mysqladmin ping
if [ "$MYSQL_OK" -eq 0 ] && command -v mysqladmin &> /dev/null; then
    if mysqladmin ping -h localhost -u root -p111111 --silent 2>/dev/null; then
        MYSQL_OK=1
        echo -e "${GREEN}[通过] MySQL 连接正常${NC}"
    fi
fi

if [ "$MYSQL_OK" -eq 0 ]; then
    echo -e "${YELLOW}[警告] 无法检测 MySQL 服务，请确保 MySQL 正在运行${NC}"
    echo -e "${YELLOW}  预期连接: localhost:3306, 用户 root, 密码 111111${NC}"
fi

# ============================================
# 4. 安装前端依赖（如果未安装）
# ============================================
echo -e "[4/5] 检查前端依赖..."
if [ ! -d "$SCRIPT_DIR/front/node_modules" ]; then
    echo -e "${YELLOW}[信息] 首次运行，正在安装前端依赖...${NC}"
    cd "$SCRIPT_DIR/front"
    npm install
    cd "$SCRIPT_DIR"
    echo -e "${GREEN}[通过] 前端依赖安装完成${NC}"
else
    echo -e "${GREEN}[通过] 前端依赖已就绪${NC}"
fi

# ============================================
# 5. 清理残留进程
# ============================================
echo -e "[5/5] 启动服务..."

# 清理可能占用端口的残留进程
kill_port_process() {
    local port=$1
    local pid=$(lsof -ti :"$port" 2>/dev/null || netstat -ano 2>/dev/null | grep ":$port" | grep LISTENING | awk '{print $5}' | head -1)
    if [ -n "$pid" ]; then
        kill -9 "$pid" 2>/dev/null || true
        echo "  已清理端口 $port 上的残留进程 (PID: $pid)"
    fi
}

kill_port_process $BACKEND_PORT
kill_port_process $FRONTEND_PORT

# ============================================
# 6. 启动后端
# ============================================
echo ""
echo "启动后端服务..."

if [ -f "$SCRIPT_DIR/back/mvnw" ]; then
    # Unix 环境下确保 mvnw 可执行
    chmod +x "$SCRIPT_DIR/back/mvnw" 2>/dev/null || true
    (cd "$SCRIPT_DIR/back" && ./mvnw spring-boot:run) &
else
    (cd "$SCRIPT_DIR/back" && mvn spring-boot:run) &
fi
BACKEND_PID=$!

# 等待后端就绪
echo "等待后端就绪（最多等待 120 秒）..."
for i in $(seq 1 40); do
    sleep 3
    if curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" 2>/dev/null | grep -q "200"; then
        echo -e "${GREEN}[通过] 后端已就绪 (localhost:${BACKEND_PORT})${NC}"
        break
    fi
    echo "  已等待 $((i * 3)) 秒..."
done

# ============================================
# 7. 启动前端
# ============================================
echo ""
echo "启动前端开发服务器..."

(cd "$SCRIPT_DIR/front" && npm run dev) &
FRONTEND_PID=$!

# 等待前端就绪
echo "等待前端就绪..."
for i in $(seq 1 30); do
    sleep 2
    if curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_URL" 2>/dev/null | grep -q "200"; then
        echo -e "${GREEN}[通过] 前端已就绪${NC}"
        break
    fi
done

# ============================================
# 完成
# ============================================
echo ""
echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}    ★ 启动完成！★${NC}"
echo -e "${CYAN}=============================================${NC}"
echo ""
echo -e "  前端访问地址:  ${GREEN}${FRONTEND_URL}${NC}"
echo -e "  后端健康检查:  ${GREEN}${HEALTH_URL}${NC}"
echo ""
echo "  关闭此窗口会同时停止后端和前端服务"
echo "  按 Ctrl+C 安全退出"
echo ""
echo -e "${CYAN}=============================================${NC}"

# 等待用户中断
wait $BACKEND_PID $FRONTEND_PID 2>/dev/null