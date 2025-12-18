# Agora Linux Java SDK 演示项目

本项目展示了如何在 Spring Boot 应用中使用 Agora Linux Java SDK。

## 创建 Spring Boot 工程

首先，使用以下 Maven 命令创建一个空的 Spring Boot 工程：

```
mvn archetype:generate -DgroupId=io.agora -DartifactId=example -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

这个命令会创建一个基本的 Maven 项目结构。之后，您需要修改 `pom.xml` 文件以添加 Spring Boot 依赖和插件。

## 前置要求

- Java JDK
- Maven
- Linux 环境

## 设置和安装

1. 解压 Agora SDK JAR 文件：

   ```
   jar xvf agora-sdk.jar
   ```

2. 将本地 JAR 安装到 Maven 本地仓库：

   ```
   mvn install:install-file -Dfile=libs/agora-sdk.jar -DgroupId=io.agora.rtc -DartifactId=linux-java-sdk -Dversion=4.4.32.100 -Dpackaging=jar
   ```

   如果要同时安装 javadoc，可以使用以下命令（需要先准备 javadoc jar 文件）：

   ```
   mvn install:install-file -Dfile=libs/agora-sdk.jar -DgroupId=io.agora.rtc -DartifactId=linux-java-sdk -Dversion=4.4.32.100 -Dpackaging=jar -Djavadoc=libs/agora-sdk-javadoc.jar
   ```

3. 构建项目：

   ```
   mvn clean package
   ```

## 运行应用

### 配置 keys

在项目根目录下创建 `.keys` 文件，并添加以下内容：

```
appId=XXX
token=XXX
```

### 本地 jar 运行

使用以下命令运行应用：

```
LD_LIBRARY_PATH="$LD_LIBRARY_PATH:libs/native/linux/x86_64:third_party" java -Dserver.port=18080 -jar target/agora-example.jar

mvn clean package && sudo lsof -ti :18080 | xargs -r sudo kill -9 && LD_LIBRARY_PATH="$LD_LIBRARY_PATH:libs/native/linux/x86_64:third_party" java -Dserver.port=18080 -jar target/agora-example.jar
```

此命令执行以下操作：

- 设置必要的库路径以包含原生库
- 配置应用在 18080 端口上运行
- 运行 Spring Boot 应用 JAR 文件

要启动一个房间，使用以下 API 端点：

```
http://10.200.0.85:18080/api/server/start?configFileName=stress_recv_yuv.json
```

### tomcat 运行

```
sudo cp -f target/agora-example.war /opt/tomcat/webapps/
sudo cp -f target/agora-example.war /opt/tomcat8/webapps/

sudo /opt/tomcat/bin/catalina.sh run
sudo /opt/tomcat8/bin/catalina.sh run

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/xxx/Examples-Mvn/libs/native/linux/x86_64:third_party
export JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/home/xxx/Examples-Mvn/libs/native/linux/x86_64:third_party"
```

要启动一个房间，使用以下 API 端点：

```
http://10.200.0.85:18080/api/server/start?configFileName=pcm_send.json
http://10.200.0.85:18080/api/server/start?configFileName=pcm_yuv_send.json
http://10.200.0.85:18080/api/server/start?configFileName=pcm_h264_send.json
http://10.200.0.85:18080/api/server/start?configFileName=encoded_audio_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=aac_send.json
http://10.200.0.85:18080/api/server/start?configFileName=opus_send.json
http://10.200.0.85:18080/api/server/start?configFileName=yuv_send.json
http://10.200.0.85:18080/api/server/start?configFileName=yuv_send_dual_stream.json
http://10.200.0.85:18080/api/server/start?configFileName=h264_send.json
http://10.200.0.85:18080/api/server/start?configFileName=h264_send_dual_stream.json
http://10.200.0.85:18080/api/server/start?configFileName=rgba_send.json
http://10.200.0.85:18080/api/server/start?configFileName=rgba_pcm_send.json
http://10.200.0.85:18080/api/server/start?configFileName=vp8_send.json
http://10.200.0.85:18080/api/server/start?configFileName=vp8_pcm_send.json
http://10.200.0.85:18080/api/server/start?configFileName=mp4_send.json
http://10.200.0.85:18080/api/server/start?configFileName=data_stream_send.json
http://10.200.0.85:18080/api/server/start?configFileName=data_stream_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=pcm_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=mixed_audio_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=yuv_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=h264_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=pcm_h264_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=ai_pcm_send.json
http://10.200.0.85:18080/api/server/start?configFileName=ai_pcm_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=stress_recv_pcm_h264.json
http://10.200.0.85:18080/api/server/start?configFileName=stress_recv_yuv.json
http://10.200.0.85:18080/api/server/start?configFileName=stress_send_pcm_h264.json
http://10.200.0.85:18080/api/server/start?configFileName=stress_send_yuv.json
http://10.200.0.85:18080/api/server/start?configFileName=h265_recv.json
http://10.200.0.85:18080/api/server/start?configFileName=h265_send.json

curl "http://127.0.0.1:18080/api/server/start?configFileName=stress_recv_pcm_h264.json"

http://10.200.0.85:18080/api/server/basic?taskName=ReceiverPcmDirectSendTest
http://10.200.0.85:18080/api/server/basic?taskName=ReceiverPcmH264Test
http://10.200.0.85:18080/api/server/basic?taskName=ReceiverPcmVadTest
http://10.200.0.85:18080/api/server/basic?taskName=ReceiverPcmYuvTest
http://10.200.0.85:18080/api/server/basic?taskName=SendH264Test
http://10.200.0.85:18080/api/server/basic?taskName=SendH265Test
http://10.200.0.85:18080/api/server/basic?taskName=SendAv1Test
http://10.200.0.85:18080/api/server/basic?taskName=SendMp4Test
http://10.200.0.85:18080/api/server/basic?taskName=SendOpusTest
http://10.200.0.85:18080/api/server/basic?taskName=SendPcmFileTest
http://10.200.0.85:18080/api/server/basic?taskName=SendPcmRealTimeTest
http://10.200.0.85:18080/api/server/basic?taskName=SendReceiverStreamMessageTest
http://10.200.0.85:18080/api/server/basic?taskName=SendYuvTest
http://10.200.0.85:18080/api/server/basic?taskName=AIReceiverSendPcmTest

```

## 停止运行

```
 sudo lsof -i :18080
 sudo kill -9 <PID>
 sudo lsof -ti :18080 | xargs -r sudo kill -9
```

## 注意事项

- 确保 `libs/native/linux/x86_64` 目录包含所有必要的原生库。
- 应用默认在 18080 端口上运行。您可以通过修改 `-Dserver.port` 参数来更改端口。
- 如果您的服务器 IP 不同，请确保更新 API 使用示例中的 IP 地址。

## 故障排除

如果遇到任何问题：

- 验证所有依赖项是否正确安装
- 检查 Agora SDK JAR 是否正确解压和安装
- 确保原生库位于正确位置且可访问

## 其他资源

- [Agora.io 文档](https://docs.agora.io/cn/)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
