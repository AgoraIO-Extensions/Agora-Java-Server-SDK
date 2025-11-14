# ============================================
# Agora Linux Java SDK Docker 镜像
# ============================================

# 基础镜像：选择OpenJDK 8运行时环境
# - eclipse-temurin:8-jdk 是OpenJDK的官方发行版
# - 基于Ubuntu，包含更新的系统库（GLIBCXX 3.4.29+）
# - 大小约400MB，兼容性更好
FROM eclipse-temurin:8-jdk

# 镜像元数据
LABEL maintainer="your-email@example.com"
LABEL description="Agora Linux Java SDK Spring Boot Application"
LABEL version="1.0.0"

# 安装必要的系统依赖
# - libstdc++6: C++标准库，Agora SDK依赖
# - ca-certificates: HTTPS证书，网络请求需要
# - curl: 健康检查和调试工具
# - procps: 提供ps命令，便于调试
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libstdc++6 \
        ca-certificates \
        curl \
        procps \
        net-tools && \
    rm -rf /var/lib/apt/lists/*

# 设置工作目录
# 所有后续命令都在此目录下执行
WORKDIR /app

# 复制应用jar包
# target/agora-example.jar -> /app/agora-example.jar
COPY target/agora-example.jar /app/agora-example.jar

# 复制Native库文件（.so）
# 这些是Agora SDK的核心库
COPY libs/native/linux/x86_64/*.so /app/libs/
COPY third_party/*.so /app/libs/

# 复制测试数据（可选，如果需要的话）
COPY test_data/ /app/test_data/

# 设置环境变量
# LD_LIBRARY_PATH: 告诉系统去哪里找.so库文件
ENV LD_LIBRARY_PATH=/app/libs:$LD_LIBRARY_PATH

# Java虚拟机参数
ENV JAVA_OPTS="-Xmx2g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 应用端口
ENV SERVER_PORT=18080

# 日志配置
ENV LOG_PATH=/app/logs

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
# 声明容器运行时监听的端口（文档作用，实际需要-p映射）
EXPOSE ${SERVER_PORT}

# 健康检查（可选）
# 注意：需要应用配置spring-boot-starter-actuator才能使用
# 如果没有actuator，可以注释掉此健康检查
# HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#     CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# 容器启动命令
# 使用exec形式（JSON数组），确保Java进程是PID 1
# 这样可以正确接收信号（如SIGTERM）
CMD ["sh", "-c", "java ${JAVA_OPTS} -Djava.library.path=/app/libs -Dserver.port=${SERVER_PORT} -jar agora-example.jar"]

