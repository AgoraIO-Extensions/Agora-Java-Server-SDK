#!/bin/bash
# ============================================
# Docker镜像构建和打包脚本
# ============================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
IMAGE_NAME="agora-example"
IMAGE_TAG="v1.0"
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"
OUTPUT_DIR="./docker-output"
TAR_FILE="${OUTPUT_DIR}/${IMAGE_NAME}-${IMAGE_TAG}.tar"
TAR_GZ_FILE="${TAR_FILE}.gz"

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}Agora Docker 镜像构建脚本${NC}"
echo -e "${GREEN}======================================${NC}"

# 步骤1: 检查前置条件
echo -e "\n${YELLOW}[1/6] 检查前置条件...${NC}"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装，请先安装Docker${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker已安装: $(docker --version)${NC}"

# 检查Docker镜像源配置（可选）
echo -e "${YELLOW}检查Docker镜像源配置...${NC}"
if timeout 5 docker pull eclipse-temurin:8-jdk --platform linux/amd64 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Docker镜像拉取正常${NC}"
else
    echo -e "${YELLOW}⚠ Docker镜像拉取较慢或失败，建议配置国内镜像源${NC}"
    read -p "是否现在配置Docker镜像加速器？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}配置Docker镜像加速器...${NC}"
        
        # 备份原有配置
        if [ -f /etc/docker/daemon.json ]; then
            echo "备份现有配置..."
            sudo cp /etc/docker/daemon.json /etc/docker/daemon.json.bak.$(date +%Y%m%d_%H%M%S)
        fi
        
        # 创建或更新 daemon.json
        sudo tee /etc/docker/daemon.json > /dev/null <<DOCKER_EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com",
    "https://docker.m.daocloud.io"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
DOCKER_EOF
        
        echo -e "${GREEN}✓ 配置文件已更新: /etc/docker/daemon.json${NC}"
        
        # 重启Docker服务
        echo "重启Docker服务..."
        sudo systemctl daemon-reload
        sudo systemctl restart docker
        
        # 检查Docker状态
        if sudo systemctl is-active --quiet docker; then
            echo -e "${GREEN}✓ Docker服务运行正常${NC}"
        else
            echo -e "${RED}✗ Docker服务启动失败${NC}"
            exit 1
        fi
        
        # 验证配置
        docker info | grep -A 5 "Registry Mirrors" || echo "镜像源配置完成"
    else
        echo -e "${YELLOW}跳过镜像源配置，如果构建失败，请手动运行: sudo ./fix-docker-registry.sh${NC}"
    fi
fi

# 检查jar文件是否存在
if [ ! -f "target/agora-example.jar" ]; then
    echo -e "${YELLOW}⚠ jar文件不存在，开始构建...${NC}"
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo -e "${RED}错误: Maven构建失败${NC}"
        exit 1
    fi
fi
echo -e "${GREEN}✓ jar文件已准备: target/agora-example.jar${NC}"

# 检查Native库
if [ ! -d "libs/native/linux/x86_64" ]; then
    echo -e "${RED}错误: Native库目录不存在: libs/native/linux/x86_64${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Native库已准备${NC}"

# 步骤2: 构建Docker镜像
echo -e "\n${YELLOW}[2/6] 构建Docker镜像...${NC}"
echo -e "镜像名称: ${FULL_IMAGE_NAME}"

docker build \
    --tag ${FULL_IMAGE_NAME} \
    --tag ${IMAGE_NAME}:latest \
    --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
    --build-arg VERSION=${IMAGE_TAG} \
    .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 镜像构建成功${NC}"
else
    echo -e "${RED}错误: 镜像构建失败${NC}"
    exit 1
fi

# 步骤3: 查看镜像信息
echo -e "\n${YELLOW}[3/6] 镜像信息...${NC}"
docker images ${IMAGE_NAME}

# 获取镜像大小
IMAGE_SIZE=$(docker images ${FULL_IMAGE_NAME} --format "{{.Size}}")
echo -e "${GREEN}镜像大小: ${IMAGE_SIZE}${NC}"

# 步骤4: 创建输出目录
echo -e "\n${YELLOW}[4/6] 准备导出目录...${NC}"
mkdir -p ${OUTPUT_DIR}
echo -e "${GREEN}✓ 输出目录: ${OUTPUT_DIR}${NC}"

# 步骤5: 导出镜像为tar文件
echo -e "\n${YELLOW}[5/6] 导出Docker镜像...${NC}"
echo "导出到: ${TAR_FILE}"

docker save -o ${TAR_FILE} ${FULL_IMAGE_NAME}

if [ $? -eq 0 ]; then
    TAR_SIZE=$(du -h ${TAR_FILE} | cut -f1)
    echo -e "${GREEN}✓ tar文件已生成: ${TAR_SIZE}${NC}"
else
    echo -e "${RED}错误: 镜像导出失败${NC}"
    exit 1
fi

# 步骤6: 压缩tar文件
echo -e "\n${YELLOW}[6/6] 压缩镜像文件...${NC}"
echo "压缩到: ${TAR_GZ_FILE}"

gzip -f ${TAR_FILE}

if [ $? -eq 0 ]; then
    GZ_SIZE=$(du -h ${TAR_GZ_FILE} | cut -f1)
    echo -e "${GREEN}✓ 压缩完成: ${GZ_SIZE}${NC}"
else
    echo -e "${RED}错误: 压缩失败${NC}"
    exit 1
fi

# 生成部署脚本
echo -e "\n${YELLOW}生成部署脚本...${NC}"
cat > ${OUTPUT_DIR}/deploy.sh << 'EOF'
#!/bin/bash
# Docker镜像部署脚本

set -e

IMAGE_FILE="agora-example-v1.0.tar.gz"
CONTAINER_NAME="agora-example"
HOST_PORT=18080

echo "======================================"
echo "Agora Docker 部署脚本"
echo "======================================"

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装"
    exit 1
fi

# 检查并创建.keys文件
if [ ! -f ".keys" ]; then
    echo "警告: .keys文件不存在，创建默认配置文件..."
    cat > .keys << 'KEYS_EOF'
APP_ID=your_app_id_here
TOKEN=your_token_here
KEYS_EOF
    echo "✓ .keys文件已创建"
    echo "⚠ 请编辑 .keys 文件，填入正确的APP_ID和TOKEN"
    echo ""
    read -p "是否现在编辑.keys文件？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ${EDITOR:-vi} .keys
    else
        echo "请稍后手动编辑 .keys 文件，然后重新运行此脚本"
        exit 0
    fi
else
    echo "✓ .keys文件已存在"
fi

# 如果.keys被错误地创建为目录，删除它
if [ -d ".keys" ]; then
    echo "检测到.keys是目录（错误），正在删除..."
    rm -rf .keys
    echo "请重新运行此脚本以创建正确的.keys文件"
    exit 1
fi

# 停止并删除旧容器
if [ "$(docker ps -aq -f name=${CONTAINER_NAME})" ]; then
    echo "停止旧容器..."
    docker stop ${CONTAINER_NAME} 2>/dev/null || true
    docker rm ${CONTAINER_NAME} 2>/dev/null || true
fi

# 导入镜像
echo "导入Docker镜像..."
docker load < ${IMAGE_FILE}

# 运行容器
echo "启动容器..."
docker run -d \
    --name ${CONTAINER_NAME} \
    --restart unless-stopped \
    -p ${HOST_PORT}:18080 \
    -v $(pwd)/.keys:/app/.keys:ro \
    -v $(pwd)/logs:/app/logs \
    -e JAVA_OPTS="-Xmx2g -Xms512m" \
    agora-example:v1.0

# 等待启动
echo "等待服务启动..."
sleep 10

# 检查状态
docker ps -f name=${CONTAINER_NAME}

echo ""
echo "======================================"
echo "部署完成！"
echo "======================================"

# 简单验证服务是否启动
echo ""
echo "验证服务启动..."
for i in {1..20}; do
    if docker logs ${CONTAINER_NAME} 2>&1 | grep -q "Started DemoApplication"; then
        echo "✓ 服务已成功启动"
        break
    fi
    if [ $i -eq 20 ]; then
        echo "⚠ 服务可能还在启动中..."
        echo "  使用以下命令查看日志: docker logs -f ${CONTAINER_NAME}"
    fi
    sleep 1
done

# 显示访问信息和测试命令
echo ""
echo "======================================"
echo "访问信息"
echo "======================================"
echo "服务地址: http://localhost:${HOST_PORT}"
echo "局域网访问: http://$(hostname -I | awk '{print $1}'):${HOST_PORT}"
echo ""
echo "======================================"
echo "测试命令（复制粘贴即可）"
echo "======================================"
echo ""
echo "# 测试API端点（发送PCM文件）"
echo "curl \"http://localhost:${HOST_PORT}/api/server/basic?taskName=SendPcmFileTest\""
echo ""
echo "# 测试其他端点"
echo "curl \"http://localhost:${HOST_PORT}/api/server/basic?taskName=ReceiverPcmDirectSendTest\""
echo ""
echo "======================================"
echo "常用命令"
echo "======================================"
echo "查看日志:  docker logs -f ${CONTAINER_NAME}"
echo "查看状态:  docker ps -f name=${CONTAINER_NAME}"
echo "进入容器:  docker exec -it ${CONTAINER_NAME} bash"
echo "停止服务:  docker stop ${CONTAINER_NAME}"
echo "重启服务:  docker restart ${CONTAINER_NAME}"
echo "删除容器:  docker rm -f ${CONTAINER_NAME}"
echo "======================================"
EOF

chmod +x ${OUTPUT_DIR}/deploy.sh

# 生成README
cat > ${OUTPUT_DIR}/README.txt << EOF
====================================
Agora Docker 镜像部署包
====================================

文件列表：
- ${IMAGE_NAME}-${IMAGE_TAG}.tar.gz  Docker镜像文件（压缩）
- deploy.sh                          一键部署脚本
- README.txt                         本文件

部署步骤：
1. 确保目标服务器已安装Docker
2. 上传整个目录到目标服务器
3. 创建.keys文件（包含APP_ID和TOKEN）
   格式示例：
   APP_ID=your_app_id_here
   TOKEN=your_token_here
4. 执行部署脚本：./deploy.sh
5. 访问服务：http://服务器IP:18080

手动部署：
1. 导入镜像：
   docker load < ${IMAGE_NAME}-${IMAGE_TAG}.tar.gz

2. 运行容器：
   docker run -d \\
     --name agora-example \\
     -p 18080:18080 \\
     -v \$(pwd)/.keys:/app/.keys:ro \\
     agora-example:${IMAGE_TAG}

3. 查看状态：
   docker ps
   docker logs -f agora-example

测试接口：
curl "http://localhost:18080/api/server/basic?taskName=SendPcmFileTest"

故障排查：
1. 查看容器日志：
   docker logs -f agora-example

2. 检查容器状态：
   docker ps -a -f name=agora-example

3. 进入容器调试：
   docker exec -it agora-example bash

4. 检查端口监听：
   docker exec agora-example netstat -tlnp

5. 查看资源使用：
   docker stats agora-example

====================================
EOF

# 输出摘要
echo -e "\n${GREEN}======================================"
echo "构建完成！"
echo "======================================${NC}"
echo -e "镜像名称: ${GREEN}${FULL_IMAGE_NAME}${NC}"
echo -e "镜像大小: ${GREEN}${IMAGE_SIZE}${NC}"
echo -e "压缩包:   ${GREEN}${TAR_GZ_FILE}${NC}"
echo -e "压缩大小: ${GREEN}${GZ_SIZE}${NC}"
echo ""
echo -e "${YELLOW}交付文件位置: ${OUTPUT_DIR}/${NC}"
ls -lh ${OUTPUT_DIR}/

echo -e "\n${GREEN}可以将 ${OUTPUT_DIR} 目录整个交付给客户${NC}"
echo ""

