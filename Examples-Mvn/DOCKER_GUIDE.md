# Agora Docker 完整指南

本文档包含Docker打包、部署和使用的完整说明，从快速入门到深入原理。

## 📑 目录
1. [快速开始（3步）](#快速开始)
2. [常用命令速查](#常用命令速查)
3. [Docker命令详解](#docker命令详解)
4. [打包原理深入解析](#打包原理)
5. [Dockerfile指令说明](#dockerfile指令)
6. [网络访问方式](#网络访问)
7. [故障排查](#故障排查)
8. [最佳实践](#最佳实践)
9. [完整部署流程](#完整部署流程)

---

## 快速开始

### 🚀 三步快速部署

```bash
# 1️⃣ 构建和打包
chmod +x build_docker.sh
./build_docker.sh

# 2️⃣ 部署（在目标服务器）
cd docker-output/
./deploy.sh

# 3️⃣ 测试
curl "http://localhost:18080/api/server/basic?taskName=SendPcmFileTest"
```

### 📝 手动步骤

```bash
# 1. 构建项目
mvn clean package

# 2. 构建Docker镜像
docker build -t agora-example:v1.0 .

# 3. 导出镜像
docker save -o agora-example-v1.0.tar agora-example:v1.0

# 4. 压缩
gzip agora-example-v1.0.tar

# 5. 部署到服务器
docker load < agora-example-v1.0.tar.gz
docker run -d --name agora-example -p 18080:18080 agora-example:v1.0
```

---

## 常用命令速查

### 📦 镜像操作
```bash
# 构建
docker build -t agora-example:v1.0 .
docker build --no-cache -t agora-example:v1.0 .  # 不使用缓存

# 查看
docker images
docker images | grep agora

# 导出
docker save -o image.tar agora-example:v1.0
docker save agora-example:v1.0 | gzip > image.tar.gz

# 导入
docker load -i image.tar
docker load < image.tar.gz

# 删除
docker rmi agora-example:v1.0
docker rmi -f agora-example:v1.0  # 强制删除
```

### 🔧 容器操作
```bash
# 运行
docker run -d --name agora-example -p 18080:18080 agora-example:v1.0

# 查看
docker ps                    # 运行中的容器
docker ps -a                 # 所有容器

# 启动/停止/重启
docker start agora-example
docker stop agora-example
docker restart agora-example

# 删除
docker rm agora-example
docker rm -f agora-example   # 强制删除（即使在运行）
```

### 📊 日志和调试
```bash
# 查看日志
docker logs agora-example
docker logs -f agora-example            # 实时跟踪
docker logs --tail 100 agora-example    # 最后100行
docker logs --since 10m agora-example   # 最近10分钟

# 进入容器
docker exec -it agora-example bash      # 进入bash
docker exec agora-example ls /app       # 执行单条命令

# 查看信息
docker inspect agora-example            # 详细信息
docker stats agora-example              # 资源使用
docker port agora-example               # 端口映射
docker top agora-example                # 容器内进程
```

### 📁 文件操作
```bash
# 从容器拷出
docker cp agora-example:/app/logs/app.log ./

# 拷入容器
docker cp config.json agora-example:/app/
```

### 🧹 清理操作
```bash
# 清理停止的容器
docker container prune

# 清理未使用的镜像
docker image prune

# 清理所有未使用资源
docker system prune

# 查看磁盘使用
docker system df
```

---

## Docker命令详解

### 1. `docker build` - 构建镜像

#### 基本语法
```bash
docker build [选项] <构建上下文路径>
```

#### 参数说明

| 参数 | 说明 | 示例 |
|------|------|------|
| `-t, --tag` | 镜像名称和标签 | `-t agora-example:v1.0` |
| `-f, --file` | 指定Dockerfile | `-f Dockerfile.prod` |
| `--build-arg` | 构建时传入变量 | `--build-arg VERSION=1.0` |
| `--no-cache` | 不使用缓存 | `--no-cache` |
| `--pull` | 总是拉取最新基础镜像 | `--pull` |
| `--target` | 多阶段构建目标 | `--target production` |

#### 完整示例
```bash
docker build \
    --tag agora-example:v1.0 \              # 镜像标签
    --tag agora-example:latest \            # 同时打latest标签
    --build-arg BUILD_DATE=$(date) \        # 传入构建日期
    --build-arg VERSION=1.0 \               # 传入版本号
    --no-cache \                            # 不使用缓存
    --pull \                                # 拉取最新基础镜像
    .                                       # 构建上下文（当前目录）
```

**构建上下文说明：**
- `.` 表示当前目录作为构建上下文
- Docker会将该目录下所有文件（除了.dockerignore中的）打包发送给Docker守护进程
- Dockerfile中的COPY/ADD命令都相对于这个上下文路径

---

### 2. `docker save/load` - 镜像导出导入

#### docker save - 导出镜像

```bash
docker save [选项] <镜像名称...>
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-o, --output` | 输出文件路径 | `-o image.tar` |
| 无选项 | 输出到标准输出 | `docker save image > image.tar` |

**示例：**
```bash
# 方式1: 使用 -o 参数
docker save -o agora-example.tar agora-example:v1.0

# 方式2: 使用重定向
docker save agora-example:v1.0 > agora-example.tar

# 方式3: 同时导出多个镜像
docker save -o images.tar agora-example:v1.0 nginx:latest

# 方式4: 直接压缩（推荐）
docker save agora-example:v1.0 | gzip > agora-example.tar.gz
```

**生成的tar文件内容：**
```
agora-example.tar/
├── manifest.json           # 镜像清单
├── repositories            # 仓库信息
├── <layer-id>/            # 每一层
│   ├── layer.tar          # 该层的文件系统
│   ├── json               # 该层的元数据
│   └── VERSION            # 版本信息
└── ...更多层
```

#### docker load - 导入镜像

```bash
docker load [选项]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-i, --input` | 从文件读取 | `-i image.tar` |
| `-q, --quiet` | 静默模式 | `-q` |

**示例：**
```bash
# 方式1: 从tar文件导入
docker load -i agora-example.tar

# 方式2: 从压缩文件导入
docker load < agora-example.tar.gz

# 方式3: 解压并导入
gunzip -c agora-example.tar.gz | docker load

# 查看导入的镜像
docker images
```

---

### 3. `docker run` - 运行容器

#### 基本语法
```bash
docker run [选项] <镜像名称> [命令]
```

#### 重要参数详解

| 参数 | 说明 | 示例 |
|------|------|------|
| `-d, --detach` | 后台运行 | `-d` |
| `--name` | 容器名称 | `--name agora-example` |
| `-p, --publish` | 端口映射 | `-p 18080:18080` |
| `-v, --volume` | 挂载卷 | `-v /host:/container` |
| `-e, --env` | 环境变量 | `-e JAVA_OPTS="-Xmx2g"` |
| `--restart` | 重启策略 | `--restart unless-stopped` |
| `--network` | 网络模式 | `--network bridge` |
| `-m, --memory` | 内存限制 | `-m 2g` |
| `--cpus` | CPU限制 | `--cpus 2` |
| `-it` | 交互式终端 | `-it` |
| `--rm` | 退出后自动删除 | `--rm` |
| `--log-opt` | 日志选项 | `--log-opt max-size=100m` |

#### 端口映射详解

```bash
# 格式: -p [宿主机IP:]宿主机端口:容器端口[/协议]

# 1. 基本映射
-p 18080:18080              # 宿主机18080 → 容器18080

# 2. 不同端口
-p 8080:18080               # 宿主机8080 → 容器18080

# 3. 指定IP（安全）
-p 127.0.0.1:18080:18080    # 只能从本机访问

# 4. 多个端口
-p 18080:18080 -p 8081:8081

# 5. 随机端口
-p 18080                    # Docker自动分配宿主机端口

# 6. 指定协议
-p 18080:18080/tcp
-p 53:53/udp
```

#### 卷挂载详解

```bash
# 格式: -v [宿主机路径:]容器路径[:选项]

# 1. 绑定挂载（推荐）
-v /home/user/.keys:/app/.keys:ro        # 只读
-v /home/user/logs:/app/logs             # 读写
-v $(pwd)/data:/app/data                 # 相对路径

# 2. 命名卷
-v agora-logs:/app/logs                  # Docker管理的命名卷

# 3. 临时文件系统
--tmpfs /app/tmp:size=100m               # 内存临时文件系统
```

#### 环境变量

```bash
# 1. 单个变量
-e JAVA_OPTS="-Xmx2g"

# 2. 多个变量
-e SERVER_PORT=18080 \
-e APP_ID=your_app_id \
-e LOG_LEVEL=debug

# 3. 从文件读取
--env-file .env              # 从.env文件读取所有变量
```

#### 重启策略

```bash
--restart no                 # 不自动重启（默认）
--restart on-failure         # 失败时重启
--restart on-failure:3       # 最多重启3次
--restart always             # 总是重启
--restart unless-stopped     # 除非手动停止（推荐生产环境）
```

#### 完整运行示例

##### 生产环境配置
```bash
docker run -d \
    --name agora-example \
    --restart unless-stopped \
    -p 18080:18080 \
    -v $(pwd)/.keys:/app/.keys:ro \
    -v $(pwd)/logs:/app/logs \
    -v $(pwd)/test_data:/app/test_data:ro \
    -e JAVA_OPTS="-Xmx2g -Xms512m -XX:+UseG1GC" \
    -e SERVER_PORT=18080 \
    --memory="2g" \
    --cpus="2" \
    --log-opt max-size=100m \
    --log-opt max-file=3 \
    agora-example:v1.0
```

##### 开发环境配置
```bash
docker run -d \
    --name agora-example-dev \
    -p 18080:18080 \
    -p 5005:5005 \
    -v $(pwd)/.keys:/app/.keys:ro \
    -v $(pwd)/logs:/app/logs \
    -e JAVA_OPTS="-Xmx1g -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" \
    agora-example:v1.0
```

---

### 4. Docker命令参数对照表

| 命令 | 参数 | 含义 | 示例 |
|------|------|------|------|
| `build` | `-t` | tag，镜像标签 | `-t name:v1.0` |
| | `-f` | file，指定Dockerfile | `-f Dockerfile.prod` |
| | `--no-cache` | 不使用缓存 | `--no-cache` |
| | `--build-arg` | 构建参数 | `--build-arg VER=1.0` |
| `save` | `-o` | output，输出文件 | `-o image.tar` |
| `load` | `-i` | input，输入文件 | `-i image.tar` |
| | `-q` | quiet，静默模式 | `-q` |
| `run` | `-d` | detach，后台运行 | `-d` |
| | `-it` | interactive + tty | `-it bash` |
| | `-p` | publish，端口映射 | `-p 8080:80` |
| | `-v` | volume，卷挂载 | `-v /host:/container` |
| | `-e` | environment，环境变量 | `-e KEY=VALUE` |
| | `--name` | 容器名称 | `--name myapp` |
| | `--rm` | remove，退出后删除 | `--rm` |
| | `--restart` | 重启策略 | `--restart always` |
| | `--network` | 网络模式 | `--network host` |
| | `--memory` | 内存限制 | `--memory 2g` |
| | `--cpus` | CPU限制 | `--cpus 2` |
| `logs` | `-f` | follow，跟踪日志 | `-f` |
| | `--tail` | 显示最后N行 | `--tail 100` |
| | `--since` | 时间之后的日志 | `--since 10m` |
| `exec` | `-it` | 交互式执行 | `-it bash` |
| | `-e` | 环境变量 | `-e VAR=value` |
| `ps` | `-a` | all，所有容器 | `-a` |
| | `-q` | quiet，只显示ID | `-q` |
| | `-f` | filter，过滤 | `-f name=myapp` |
| `rm` | `-f` | force，强制删除 | `-f` |
| | `-v` | volumes，删除关联卷 | `-v` |

---

## 打包原理

### 1. Docker 镜像分层架构

```
┌─────────────────────────────────────────┐
│  应用层 (Layer 4) - 39MB                │
│  agora-example.jar                      │
├─────────────────────────────────────────┤
│  库文件层 (Layer 3) - 65MB              │
│  libagora_rtc_sdk.so + 其他.so文件      │
├─────────────────────────────────────────┤
│  依赖层 (Layer 2) - 50MB                │
│  系统库: libstdc++, curl等              │
├─────────────────────────────────────────┤
│  运行时层 (Layer 1) - 100MB             │
│  OpenJDK 8 + 基础工具                   │
├─────────────────────────────────────────┤
│  基础OS层 (Layer 0) - 100MB             │
│  Debian Linux + GLIBC 2.28              │
└─────────────────────────────────────────┘

总大小: ~354MB
```

**分层的好处：**
1. **复用性**：多个镜像共享相同的基础层，节省存储空间
2. **增量更新**：只需下载变化的层，加快部署速度
3. **快速构建**：未改变的层使用缓存，提高构建效率

### 2. 镜像构建流程

```
Dockerfile指令 → 创建临时容器 → 执行命令 → 提交为新层 → 删除临时容器
```

**详细步骤：**
```bash
# Step 1: FROM openjdk:8-jdk
拉取基础镜像 → Layer 0 (基础OS + OpenJDK)

# Step 2: RUN apt-get install...
创建容器 → 执行apt-get → 提交文件系统变化 → Layer 1 (系统依赖)

# Step 3: COPY libs/...
复制.so文件到镜像 → Layer 2 (Native库)

# Step 4: COPY target/agora-example.jar
复制jar文件 → Layer 3 (应用)

# Step 5: CMD [...]
设置启动命令（元数据，不占空间）
```

### 3. UnionFS 联合文件系统

Docker使用UnionFS将多层只读层 + 1层可写层合并：

```
容器运行时视图：

可写层 (Container Layer)     ← 容器运行时产生的文件（日志、临时文件等）
   ↓
只读层4 (agora-example.jar)  ← 从镜像挂载（只读）
   ↓
只读层3 (.so库文件)
   ↓
只读层2 (系统依赖)
   ↓
只读层1 (OpenJDK)
   ↓
只读层0 (基础OS + GLIBC)

最终在容器内看到统一的文件系统
```

**写时复制（Copy-on-Write）原理：**
- **读取文件**：从上往下查找，第一个找到的版本
- **修改文件**：复制到可写层，修改副本（原层不变）
- **删除文件**：在可写层标记删除（原层仍存在）

### 4. docker save 打包原理

```bash
docker save agora-example:v1.0 -o image.tar
```

**生成的tar包结构：**
```
image.tar/
├── manifest.json              # 镜像清单
│   └── {
│       "Config": "sha256:abc...",
│       "RepoTags": ["agora-example:v1.0"],
│       "Layers": [
│           "layer1/layer.tar",
│           "layer2/layer.tar",
│           ...
│       ]
│   }
├── <config-hash>.json         # 镜像配置
│   └── {
│       "architecture": "amd64",
│       "os": "linux",
│       "rootfs": {
│           "type": "layers",
│           "diff_ids": [...]
│       },
│       "history": [...],
│       "config": {
│           "Env": ["PATH=...", "JAVA_OPTS=..."],
│           "Cmd": ["java", "-jar", "..."],
│           "ExposedPorts": {"18080/tcp": {}},
│           ...
│       }
│   }
├── <layer1-hash>/
│   ├── layer.tar             # 该层的文件系统tar包
│   ├── json                  # 该层元数据
│   └── VERSION               # 版本
└── ...更多层
```

**docker load 导入过程：**
1. 读取 `manifest.json` 获取镜像信息
2. 解析每一层的hash值
3. 解压每层的 `layer.tar` 到 `/var/lib/docker/overlay2/`
4. 创建镜像元数据
5. 更新镜像索引

### 5. 容器运行原理

```bash
docker run -d -p 18080:18080 agora-example:v1.0
```

**执行流程：**
```
1. 镜像层准备
   ├─ 检查镜像是否存在本地
   ├─ 准备所有只读层
   └─ 创建容器可写层

2. 命名空间隔离（Namespace）
   ├─ PID namespace: 独立进程空间
   ├─ NET namespace: 独立网络栈
   ├─ MNT namespace: 独立文件系统
   ├─ UTS namespace: 独立主机名
   └─ USER namespace: 独立用户

3. Cgroups资源限制
   ├─ 内存限制（--memory）
   ├─ CPU限制（--cpus）
   ├─ 磁盘IO限制
   └─ 网络带宽限制

4. 网络配置
   ├─ 创建veth pair（虚拟网卡对）
   ├─ 一端接入容器（eth0）
   ├─ 一端接入docker0网桥
   └─ 配置NAT规则（端口映射）

5. 启动进程
   ├─ 切换root到容器文件系统
   ├─ 设置环境变量
   ├─ 执行CMD指令
   └─ Java进程启动（PID 1）
```

### 6. 容器与宿主机的关系

```
┌─────────────────────────────────────────────┐
│              宿主机 (CentOS 7)              │
│  GLIBC 2.17 (容器不使用)                    │
├─────────────────────────────────────────────┤
│  Linux Kernel 3.10 (共享)                   │
│  ├─ 系统调用接口                            │
│  ├─ 进程调度                                │
│  ├─ 内存管理                                │
│  └─ 网络协议栈                              │
├─────────────────────────────────────────────┤
│  Docker Engine                              │
│  ├─ containerd (容器运行时)                 │
│  └─ runc (OCI运行时)                        │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐ │
│  │  Container: agora-example             │ │
│  │  ┌─────────────────────────────────┐ │ │
│  │  │  进程空间                        │ │ │
│  │  │  ├─ Java (PID 1)                │ │ │
│  │  │  └─ JNI → libagora_rtc_sdk.so  │ │ │
│  │  ├─────────────────────────────────┤ │ │
│  │  │  文件系统 (UnionFS)              │ │ │
│  │  │  ├─ /app/agora-example.jar      │ │ │
│  │  │  ├─ /app/libs/*.so              │ │ │
│  │  │  └─ /lib/x86_64-linux-gnu/      │ │ │
│  │  │     └─ libc.so.6 (GLIBC 2.28)  │ │ │  ← 容器自带
│  │  ├─────────────────────────────────┤ │ │
│  │  │  网络                            │ │ │
│  │  │  ├─ eth0: 172.17.0.2           │ │ │
│  │  │  └─ Port 18080                  │ │ │
│  │  └─────────────────────────────────┘ │ │
│  └───────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

**关键点：**
1. **共享内核**：容器使用宿主机内核（通过系统调用）
2. **隔离用户空间**：容器有自己的GLIBC和系统库
3. **namespace隔离**：进程、网络、文件系统完全独立
4. **cgroups限制**：资源使用受限，保护宿主机

**为什么不依赖宿主机GLIBC？**
- 容器镜像内包含完整的用户空间环境（包括GLIBC）
- libagora_rtc_sdk.so 链接到容器内的GLIBC 2.28
- 即使宿主机是CentOS 7（GLIBC 2.17），容器也能正常运行

---

## Dockerfile指令

### 基础指令完整说明

```dockerfile
# ===== 基础镜像 =====
# FROM - 指定基础镜像（必须是第一条指令）
FROM openjdk:8-jdk
FROM ubuntu:20.04 AS builder    # 多阶段构建别名

# ===== 元数据 =====
# LABEL - 添加元数据标签
LABEL maintainer="user@example.com"
LABEL version="1.0"
LABEL description="Agora Linux Java SDK"

# ===== 环境变量 =====
# ENV - 设置环境变量（在容器运行时也存在）
ENV JAVA_OPTS="-Xmx2g"
ENV PATH=/app/bin:$PATH
ENV LD_LIBRARY_PATH=/app/libs:$LD_LIBRARY_PATH

# ARG - 构建参数（只在构建时存在）
ARG BUILD_DATE
ARG VERSION=1.0
RUN echo "Building version ${VERSION}"

# ===== 工作目录 =====
# WORKDIR - 设置工作目录（自动创建）
WORKDIR /app
# 等价于: RUN mkdir -p /app && cd /app

# ===== 文件复制 =====
# COPY - 复制文件（推荐）
COPY src/ dest/                      # 目录
COPY file.txt /app/                  # 单文件
COPY --chown=user:group src dest     # 指定所有者

# ADD - 复制并自动解压（不推荐，特殊情况使用）
ADD archive.tar.gz /app/             # 自动解压tar.gz
ADD http://example.com/file /app/    # 下载URL（不推荐）

# ===== 执行命令 =====
# RUN - 执行命令（每个RUN创建一层）
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*      # 清理缓存，减小层大小

# ===== 端口声明 =====
# EXPOSE - 声明端口（仅文档作用，实际需要-p映射）
EXPOSE 18080
EXPOSE 8080/tcp 9090/udp

# ===== 启动命令 =====
# CMD - 容器默认命令（只能有一个，会被docker run命令覆盖）
CMD ["java", "-jar", "app.jar"]      # exec形式（推荐）
CMD java -jar app.jar                # shell形式

# ENTRYPOINT - 入口点（不会被覆盖，CMD作为参数）
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]              # CMD作为ENTRYPOINT参数
# 实际运行: java -jar app.jar

# ===== 卷声明 =====
# VOLUME - 声明卷（数据持久化）
VOLUME /app/logs
VOLUME ["/data", "/config"]

# ===== 用户切换 =====
# USER - 切换用户（安全实践）
USER appuser
RUN whoami                           # 输出: appuser

# ===== 健康检查 =====
# HEALTHCHECK - 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:18080/health || exit 1
```

### Dockerfile 优化技巧

```dockerfile
# ===== 技巧1: 合并RUN减少层数 =====
# ❌ 不好 - 创建3层
RUN apt-get update
RUN apt-get install -y curl
RUN rm -rf /var/lib/apt/lists/*

# ✅ 好 - 只创建1层
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# ===== 技巧2: 利用构建缓存 =====
# 把变化少的放前面，变化多的放后面
COPY pom.xml .                # 依赖文件（变化少）
RUN mvn dependency:go-offline
COPY src/ ./src/              # 源代码（变化多）
RUN mvn package

# ===== 技巧3: 多阶段构建减小镜像 =====
# 构建阶段
FROM maven:3.8-jdk-8 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# 运行阶段（只包含必要文件）
FROM openjdk:8-jre-slim
COPY --from=builder /build/target/app.jar /app/
CMD ["java", "-jar", "/app/app.jar"]
# 结果：镜像从800MB减小到200MB

# ===== 技巧4: 清理临时文件 =====
RUN apt-get update && \
    apt-get install -y package && \
    apt-get clean && \                      # 清理apt缓存
    rm -rf /var/lib/apt/lists/* && \        # 删除包列表
    rm -rf /tmp/* /var/tmp/*                # 删除临时文件
```

### Dockerfile 指令速查表

| 指令 | 说明 | 示例 |
|------|------|------|
| `FROM` | 基础镜像 | `FROM openjdk:8-jdk` |
| `LABEL` | 元数据标签 | `LABEL version="1.0"` |
| `RUN` | 执行命令 | `RUN apt-get update` |
| `COPY` | 复制文件 | `COPY app.jar /app/` |
| `ADD` | 复制并解压 | `ADD file.tar.gz /app/` |
| `WORKDIR` | 工作目录 | `WORKDIR /app` |
| `ENV` | 环境变量 | `ENV PATH=/app:$PATH` |
| `ARG` | 构建参数 | `ARG VERSION=1.0` |
| `EXPOSE` | 声明端口 | `EXPOSE 8080` |
| `VOLUME` | 声明卷 | `VOLUME /data` |
| `USER` | 切换用户 | `USER appuser` |
| `CMD` | 默认命令 | `CMD ["java", "-jar"]` |
| `ENTRYPOINT` | 入口点 | `ENTRYPOINT ["java"]` |
| `HEALTHCHECK` | 健康检查 | `HEALTHCHECK CMD curl ...` |

---

## 网络访问

### 1. 通过端口映射（最常用） ⭐推荐

```bash
# 启动时映射端口
docker run -d -p 18080:18080 agora-example:v1.0

# 访问方式
curl http://localhost:18080/api/...              # 本机
curl http://宿主机IP:18080/api/...                # 局域网/外网
curl http://192.168.1.100:18080/api/...          # 指定IP

# 优点：标准方式，适合生产环境
# 缺点：有轻微的NAT性能损耗（通常<5%）
```

### 2. 通过容器IP（仅宿主机访问）

```bash
# 获取容器IP
CONTAINER_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' agora-example)
echo $CONTAINER_IP
# 输出: 172.17.0.2

# 访问（只能从宿主机）
curl http://172.17.0.2:18080/api/...

# 优点：无NAT损耗
# 缺点：只能宿主机访问，IP可能变化
```

### 3. 通过容器名（容器间通信）

```bash
# 创建自定义网络
docker network create agora-network

# 启动多个容器在同一网络
docker run -d --name agora-example --network agora-network agora-example:v1.0
docker run -d --name nginx --network agora-network nginx:latest

# 在nginx容器内可以直接通过容器名访问
docker exec nginx curl http://agora-example:18080/api/...

# 优点：容器间通信方便
# 缺点：只适用于容器间
```

### 4. Host网络模式（高性能场景）

```bash
# 直接使用宿主机网络
docker run -d --network host agora-example:v1.0

# 访问（无需端口映射）
curl http://localhost:18080/api/...
curl http://宿主机IP:18080/api/...

# 优点：性能最好（无NAT）
# 缺点：
# - 端口直接占用宿主机
# - 隔离性差
# - 不推荐生产环境
```

### 网络访问对比表

| 方式 | 性能 | 隔离性 | 适用场景 | 推荐度 |
|------|------|--------|---------|--------|
| 端口映射 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 生产环境 | ⭐⭐⭐⭐⭐ |
| 容器IP | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 宿主机调试 | ⭐⭐⭐ |
| 容器名 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 容器间通信 | ⭐⭐⭐⭐ |
| Host模式 | ⭐⭐⭐⭐⭐ | ⭐ | 高性能需求 | ⭐⭐ |

---

## 故障排查

### 问题1: 容器启动失败

**症状：** 容器启动后立即退出

```bash
# 1. 查看容器状态
docker ps -a | grep agora

# 2. 查看完整日志
docker logs agora-example
docker logs --tail 200 agora-example

# 3. 查看容器退出原因
docker inspect agora-example | grep -A 10 State

# 4. 尝试交互式运行（调试模式）
docker run -it --rm agora-example:v1.0 bash
# 手动执行命令查看错误
java -jar /app/agora-example.jar
```

**常见原因：**
- .keys文件未挂载或路径错误
- Java内存配置过大
- 必要的.so库文件缺失
- 端口被占用

---

### 问题2: 端口被占用

**症状：** 
```
Error: Bind for 0.0.0.0:18080 failed: port is already allocated
```

```bash
# 1. 查看端口占用
lsof -ti :18080
netstat -tlnp | grep 18080

# 2. 停止占用端口的进程
sudo kill -9 $(lsof -ti :18080)

# 3. 或者更换端口
docker run -d -p 8080:18080 agora-example:v1.0
# 访问: http://localhost:8080/api/...
```

---

### 问题3: 无法访问服务

**症状：** 容器运行但无法访问

```bash
# 1. 检查容器是否运行
docker ps | grep agora

# 2. 检查端口映射是否正确
docker port agora-example

# 3. 检查容器内端口是否监听
docker exec agora-example netstat -tlnp | grep 18080
docker exec agora-example ss -tlnp | grep 18080

# 4. 检查容器内服务是否启动
docker exec agora-example ps aux | grep java

# 5. 测试从容器内访问
docker exec agora-example curl -I http://localhost:18080/health

# 6. 检查防火墙
sudo firewall-cmd --list-ports     # CentOS/RHEL
sudo ufw status                     # Ubuntu

# 7. 开放端口
sudo firewall-cmd --add-port=18080/tcp --permanent
sudo firewall-cmd --reload
sudo ufw allow 18080/tcp

# 8. 检查Docker网络
docker network inspect bridge
```

---

### 问题4: 内存不足/资源限制

**症状：** 容器OOM或性能差

```bash
# 1. 查看容器资源使用
docker stats agora-example
docker stats --no-stream agora-example

# 2. 查看容器日志是否有OOM错误
docker logs agora-example | grep -i "out of memory"
docker logs agora-example | grep -i "oom"

# 3. 动态调整资源限制（运行中容器）
docker update --memory="4g" agora-example
docker update --cpus="4" agora-example

# 4. 或重新运行with更多资源
docker stop agora-example
docker rm agora-example
docker run -d \
    --name agora-example \
    --memory="4g" \
    --cpus="4" \
    -p 18080:18080 \
    agora-example:v1.0
```

---

### 问题5: .keys文件或数据未挂载

**症状：** 找不到配置文件或数据

```bash
# 1. 检查挂载情况
docker inspect agora-example | grep -A 10 Mounts

# 2. 进入容器检查
docker exec -it agora-example bash
ls -la /app/.keys
cat /app/.keys

# 3. 确认宿主机文件存在
ls -la $(pwd)/.keys

# 4. 重新挂载（正确的方式）
docker stop agora-example
docker rm agora-example
docker run -d \
    --name agora-example \
    -p 18080:18080 \
    -v $(pwd)/.keys:/app/.keys:ro \        # 绝对路径
    -v $(pwd)/logs:/app/logs \
    agora-example:v1.0
```

---

### 问题6: 日志查看和分析

```bash
# 1. 实时跟踪日志
docker logs -f agora-example

# 2. 查看最近日志
docker logs --tail 100 agora-example
docker logs --since 10m agora-example
docker logs --since "2025-10-29T10:00:00" agora-example

# 3. 导出日志到文件
docker logs agora-example > /tmp/agora.log

# 4. 搜索特定错误
docker logs agora-example 2>&1 | grep -i error
docker logs agora-example 2>&1 | grep -i exception

# 5. 查看容器内日志文件
docker exec agora-example tail -f /app/logs/agora_logs/agorasdk.log
```

---

### 问题7: 镜像导入失败

**症状：** docker load失败

```bash
# 1. 检查tar文件完整性
gunzip -t agora-example-v1.0.tar.gz
# 或
file agora-example-v1.0.tar.gz

# 2. 查看文件大小是否合理
ls -lh agora-example-v1.0.tar.gz

# 3. 尝试解压后导入
gunzip agora-example-v1.0.tar.gz
docker load -i agora-example-v1.0.tar

# 4. 检查磁盘空间
df -h
docker system df

# 5. 清理Docker磁盘空间
docker system prune -a
```

---

## 最佳实践

### ✅ 推荐做法（DO）

```bash
# 1. 使用官方基础镜像
FROM openjdk:8-jdk

# 2. 合并RUN指令减少层数
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 3. 使用.dockerignore减小构建上下文
# 创建.dockerignore文件排除不必要的文件

# 4. 使用多阶段构建减小镜像
FROM maven:3.8-jdk-8 AS builder
# ... 构建
FROM openjdk:8-jre-slim
# ... 运行

# 5. 设置健康检查
HEALTHCHECK CMD curl -f http://localhost:18080/health || exit 1

# 6. 使用非root用户运行（安全）
USER appuser

# 7. 日志输出到stdout（便于docker logs查看）
# 不要写入容器文件系统

# 8. 敏感信息用volume或secret
docker run -v $(pwd)/.keys:/app/.keys:ro ...  # 只读挂载

# 9. 设置资源限制
docker run --memory="2g" --cpus="2" ...

# 10. 使用unless-stopped重启策略
docker run --restart unless-stopped ...

# 11. 标签语义化版本
docker build -t agora-example:1.2.3 .
docker build -t agora-example:latest .

# 12. 定期清理无用资源
docker system prune -a
```

### ❌ 避免做法（DON'T）

```bash
# 1. 不要在镜像中存储敏感信息
# ❌ COPY .keys /app/.keys
# ✅ docker run -v $(pwd)/.keys:/app/.keys:ro ...

# 2. 不要使用latest标签（生产环境）
# ❌ docker build -t agora-example:latest .
# ✅ docker build -t agora-example:1.2.3 .

# 3. 不要在一个RUN中做太多无关的事
# ❌ RUN apt-get update && apt-get install -y curl && wget ... && make ... && ...
# ✅ 分解成逻辑相关的RUN指令

# 4. 不要在生产环境使用host网络
# ❌ docker run --network host ...
# ✅ docker run -p 18080:18080 ...

# 5. 不要忘记清理apt缓存
# ❌ RUN apt-get install -y curl
# ✅ RUN apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 6. 不要使用root用户运行应用
# ❌ 默认root用户
# ✅ USER appuser

# 7. 不要把日志写入容器文件系统
# ❌ 日志文件在容器内（容器删除后丢失）
# ✅ 挂载volume: -v $(pwd)/logs:/app/logs

# 8. 不要使用过大的基础镜像
# ❌ FROM ubuntu:latest (1GB+)
# ✅ FROM openjdk:8-jre-slim (200MB)

# 9. 不要忽略.dockerignore
# 会增加构建时间和上下文大小

# 10. 不要在Dockerfile中执行apt-get upgrade
# 保持镜像可重现性
```

### 🎯 性能优化

```dockerfile
# 1. 使用Alpine或Slim基础镜像
FROM openjdk:8-jre-slim      # ~200MB
# 替代: FROM openjdk:8-jdk  # ~500MB

# 2. 多阶段构建
FROM maven:3.8 AS builder
# ... 构建逻辑
FROM openjdk:8-jre-slim
COPY --from=builder /app/target/*.jar /app/

# 3. 利用构建缓存
COPY pom.xml .               # 先复制依赖文件
RUN mvn dependency:go-offline
COPY src ./src               # 后复制源码
RUN mvn package

# 4. 减少层数
RUN apt-get update && \
    apt-get install -y package1 package2 && \
    rm -rf /var/lib/apt/lists/*
```

### 🔒 安全实践

```dockerfile
# 1. 使用非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# 2. 只复制必要文件
COPY --chown=appuser:appuser target/*.jar /app/

# 3. 使用只读挂载
docker run -v $(pwd)/.keys:/app/.keys:ro ...

# 4. 限制资源
docker run --memory="2g" --cpus="2" --pids-limit=100 ...

# 5. 定期更新基础镜像
docker pull openjdk:8-jdk
docker build --pull ...
```

---

## 完整部署流程

### 1. 准备阶段

**项目结构：**
```
Examples-Mvn/
├── Dockerfile              # Docker配置文件
├── .dockerignore          # 构建忽略文件
├── build_docker.sh        # 自动构建脚本
├── pom.xml                # Maven配置
├── src/                   # 源代码
├── target/
│   └── agora-example.jar  # 编译产物
├── libs/
│   └── native/
│       └── linux/
│           └── x86_64/
│               ├── libagora_rtc_sdk.so
│               ├── libaosl.so
│               └── ...其他.so
├── third_party/
│   ├── libffmpeg_utils.so
│   └── libmedia_utils.so
└── test_data/             # 测试数据
```

---

### 2. 构建镜像

```bash
# 方式1: 使用自动化脚本（推荐）
./build_docker.sh

# 方式2: 手动构建
mvn clean package
docker build -t agora-example:v1.0 .
```

**构建过程输出：**
```
Step 1/12 : FROM openjdk:8-jdk
 ---> e24ac6f5f1cb
Step 2/12 : LABEL maintainer="..."
 ---> Running in abc123
 ---> def456
Step 3/12 : RUN apt-get update...
 ---> Running in 789xyz
...安装依赖...
 ---> fedcba
...
Successfully built 1234567890ab
Successfully tagged agora-example:v1.0
```

---

### 3. 导出镜像

```bash
# 导出为tar
docker save -o agora-example-v1.0.tar agora-example:v1.0

# 压缩
gzip agora-example-v1.0.tar

# 查看大小
ls -lh agora-example-v1.0.tar.gz
# -rw-r--r-- 1 user user 350M Oct 29 10:00 agora-example-v1.0.tar.gz
```

---

### 4. 交付文件

```
docker-output/
├── agora-example-v1.0.tar.gz   # Docker镜像（~350MB）
├── deploy.sh                    # 部署脚本
└── README.txt                   # 部署说明
```

---

### 5. 客户端部署

```bash
# 1. 导入镜像
docker load < agora-example-v1.0.tar.gz
# Loaded image: agora-example:v1.0

# 2. 查看镜像
docker images | grep agora
# agora-example  v1.0  1234567890ab  350MB

# 3. 创建配置文件
cat > .keys << 'EOF'
appId=your_app_id_here
token=your_token_here
EOF

# 4. 创建必要目录
mkdir -p logs

# 5. 运行容器
docker run -d \
    --name agora-example \
    --restart unless-stopped \
    -p 18080:18080 \
    -v $(pwd)/.keys:/app/.keys:ro \
    -v $(pwd)/logs:/app/logs \
    -e JAVA_OPTS="-Xmx2g -Xms512m" \
    --memory="2g" \
    --cpus="2" \
    agora-example:v1.0

# 6. 检查容器状态
docker ps -f name=agora-example

# 7. 查看启动日志
docker logs -f agora-example

# 8. 等待服务启动（约30秒）
sleep 30

# 9. 测试服务
curl -I http://localhost:18080/actuator/health
curl "http://localhost:18080/api/server/basic?taskName=SendPcmFileTest"

# 10. 查看资源使用
docker stats agora-example --no-stream
```

---

### 6. 一键部署脚本

将下面的脚本保存为 `deploy.sh`：

```bash
#!/bin/bash
# ============================================
# Agora Docker 一键部署脚本
# ============================================

set -e

# 配置
IMAGE_FILE="agora-example-v1.0.tar.gz"
CONTAINER_NAME="agora-example"
HOST_PORT=18080
CONTAINER_PORT=18080

echo "======================================"
echo "Agora Docker 一键部署"
echo "======================================"

# 1. 检查Docker
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装"
    exit 1
fi
echo "✓ Docker已安装: $(docker --version)"

# 2. 检查镜像文件
if [ ! -f "$IMAGE_FILE" ]; then
    echo "错误: 镜像文件不存在: $IMAGE_FILE"
    exit 1
fi
echo "✓ 镜像文件存在: $IMAGE_FILE"

# 3. 检查.keys文件
if [ ! -f ".keys" ]; then
    echo "警告: .keys文件不存在，请创建"
    echo "创建默认.keys文件..."
    cat > .keys << 'EOF'
appId=your_app_id_here
token=your_token_here
EOF
    echo "请编辑.keys文件填写正确的appId和token"
fi
echo "✓ .keys文件存在"

# 4. 停止并删除旧容器
if [ "$(docker ps -aq -f name=${CONTAINER_NAME})" ]; then
    echo "停止并删除旧容器..."
    docker stop ${CONTAINER_NAME} 2>/dev/null || true
    docker rm ${CONTAINER_NAME} 2>/dev/null || true
    echo "✓ 旧容器已清理"
fi

# 5. 导入镜像
echo "导入Docker镜像..."
docker load < ${IMAGE_FILE}
echo "✓ 镜像导入成功"

# 6. 创建目录
mkdir -p logs
echo "✓ 日志目录已创建"

# 7. 运行容器
echo "启动容器..."
docker run -d \
    --name ${CONTAINER_NAME} \
    --restart unless-stopped \
    -p ${HOST_PORT}:${CONTAINER_PORT} \
    -v $(pwd)/.keys:/app/.keys:ro \
    -v $(pwd)/logs:/app/logs \
    -e JAVA_OPTS="-Xmx2g -Xms512m" \
    --memory="2g" \
    --cpus="2" \
    --log-opt max-size=100m \
    --log-opt max-file=3 \
    agora-example:v1.0

# 8. 等待启动
echo "等待服务启动（30秒）..."
sleep 30

# 9. 检查状态
echo ""
echo "======================================"
echo "容器状态"
echo "======================================"
docker ps -f name=${CONTAINER_NAME}

# 10. 显示日志
echo ""
echo "======================================"
echo "最近日志"
echo "======================================"
docker logs --tail 20 ${CONTAINER_NAME}

# 11. 测试服务
echo ""
echo "======================================"
echo "服务测试"
echo "======================================"
if curl -s -f http://localhost:${HOST_PORT}/actuator/health > /dev/null 2>&1; then
    echo "✓ 服务健康检查通过"
else
    echo "⚠ 服务可能还在启动中"
fi

# 12. 完成
echo ""
echo "======================================"
echo "部署完成！"
echo "======================================"
echo "访问地址: http://localhost:${HOST_PORT}"
echo "查看日志: docker logs -f ${CONTAINER_NAME}"
echo "停止服务: docker stop ${CONTAINER_NAME}"
echo "重启服务: docker restart ${CONTAINER_NAME}"
echo ""
echo "API示例："
echo "curl 'http://localhost:${HOST_PORT}/api/server/basic?taskName=SendPcmFileTest'"
echo "======================================"
```

**使用方式：**
```bash
chmod +x deploy.sh
./deploy.sh
```

---

### 7. 部署流程图

```
┌─────────────────────────────────────────┐
│  开发环境                                │
│  ┌─────────────────────────────────┐   │
│  │  1. 构建项目                     │   │
│  │     mvn clean package           │   │
│  └──────────────┬──────────────────┘   │
│                 ↓                       │
│  ┌─────────────────────────────────┐   │
│  │  2. 构建Docker镜像              │   │
│  │     docker build -t ...         │   │
│  └──────────────┬──────────────────┘   │
│                 ↓                       │
│  ┌─────────────────────────────────┐   │
│  │  3. 导出镜像                     │   │
│  │     docker save | gzip          │   │
│  └──────────────┬──────────────────┘   │
└─────────────────┼───────────────────────┘
                  ↓
      [ agora-example-v1.0.tar.gz ]
                  ↓
┌─────────────────┼───────────────────────┐
│  生产环境       ↓                       │
│  ┌─────────────────────────────────┐   │
│  │  4. 导入镜像                     │   │
│  │     docker load                 │   │
│  └──────────────┬──────────────────┘   │
│                 ↓                       │
│  ┌─────────────────────────────────┐   │
│  │  5. 运行容器                     │   │
│  │     docker run -d -p ...        │   │
│  └──────────────┬──────────────────┘   │
│                 ↓                       │
│  ┌─────────────────────────────────┐   │
│  │  6. 服务运行                     │   │
│  │     http://IP:18080/api/...     │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 总结

### 核心流程

```
Java项目 → mvn package → jar + .so文件
    ↓
Dockerfile → docker build → Docker镜像(分层)
    ↓
docker save → tar文件 → gzip → tar.gz压缩包（交付物）
    ↓
客户服务器 → docker load → 导入镜像
    ↓
docker run → 运行容器 → 提供服务
```

### 核心原理

1. **分层存储**：每个Dockerfile指令创建一层，层可复用
2. **UnionFS**：多层只读层 + 1层可写层合并视图
3. **namespace隔离**：独立的进程、网络、文件系统
4. **不依赖宿主GLIBC**：容器自带完整用户空间环境

### 关键命令

| 操作 | 命令 | 说明 |
|------|------|------|
| 构建 | `docker build -t name:tag .` | 构建镜像 |
| 导出 | `docker save name \| gzip > file.tar.gz` | 导出压缩 |
| 导入 | `docker load < file.tar.gz` | 导入镜像 |
| 运行 | `docker run -d -p 18080:18080 name` | 运行容器 |
| 查看 | `docker ps` / `docker logs` | 查看状态 |
| 调试 | `docker exec -it name bash` | 进入容器 |

### 最终交付物

```
✅ agora-example-v1.0.tar.gz    (~350-500MB)
   ├── 完整的运行环境（OS + GLIBC 2.28）
   ├── Java运行时（OpenJDK 8）
   ├── 应用程序（jar）
   ├── Native库（.so文件）
   └── 所有依赖

✅ 不依赖客户服务器的GLIBC版本
✅ 只需要Linux内核3.10+和Docker
✅ 一键部署，环境一致
```

---

## 📞 帮助资源

- **本地Docker版本**：`docker --version`
- **Docker官方文档**：https://docs.docker.com
- **Dockerfile参考**：https://docs.docker.com/engine/reference/builder/
- **Docker命令参考**：https://docs.docker.com/engine/reference/commandline/cli/
- **最佳实践**：https://docs.docker.com/develop/dev-best-practices/

---

**📌 提示：** 建议将此文档保存为书签，随时查阅！如有问题，请参考故障排查章节。

