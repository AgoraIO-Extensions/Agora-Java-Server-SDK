package io.agora.rtc.test.smallweb;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.test.pcm.MultipleConnectionPcmReceiveTest;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * PACD 集成 hello模板
 * -- 1 打包
 * # 前提条件 : jdk8 & javac命令 yum install java-1.8.0-openjdk-devel.x86_64
 * javac SmallWeb.java
 * jar cvf SmallWeb.jar SmallWeb.class GetHttpHandler.class
 * PostHttpHandler.class
 * <p>
 * <p>
 * -- 2 运行&验证
 * java -cp SmallWeb.jar SmallWeb
 * # get测试
 * curl http://127.0.0.1:18888?name=zhangsan
 * curl http://127.0.0.1:18888/get?name=china
 * <p>
 * # post测试
 * curl -H "Content-Type: application/json" -d '{"username":"china"}'
 * http://127.0.0.1:18888/post
 */
public class SmallWeb {
    // HTTP默认端口18888
    private static final Integer port = 18888;
    private static HashMap<String, MultipleConnectionPcmReceiveTest> processHashMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            // server.createContext("/", new GetHttpHandler());
            server.createContext("/get", new GetHttpHandler());
            server.createContext("/post", new PostHttpHandler());
            server.start();
            SampleLogger.log("http://localhost:" + port + " start...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static synchronized long getPidOfProcess(Process p) {
        long pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }

    static class GetHttpHandler implements HttpHandler {

        public void handle(HttpExchange httpExchange) throws IOException {
            StringBuilder response = new StringBuilder();
            String name = queryToMap(httpExchange.getRequestURI().getQuery(), "name");
            if (name == null || "".equals(name)) {
                name = "world";
            }
            SampleLogger.log("handle get " + name);
            response.append("hello " + name + " !\n");
            byte[] responseByte = response.toString().getBytes("utf-8");
            synchronized (processHashMap) {
                SampleLogger.log("handle process " + name);
                if (processHashMap.containsKey(name)) {
                    SampleLogger.log("handle process cleanup" + name);
                    MultipleConnectionPcmReceiveTest process = processHashMap.get(name);
                    process.cleanup();
                    processHashMap.remove(name);
                } else {
                    SampleLogger.log("handle process setup" + name);
                    MultipleConnectionPcmReceiveTest process = new MultipleConnectionPcmReceiveTest();
                    processHashMap.put(name, process);
                    process.setup();
                }
            }
            SampleLogger.log("handle return " + name);

            // 返回
            httpExchange.getResponseHeaders().add("Content-Type:", "text/html;charset=utf-8");
            httpExchange.sendResponseHeaders(200, responseByte.length);
            OutputStream out = httpExchange.getResponseBody();
            out.write(responseByte);
            out.flush();
            out.close();
        }

        public String queryToMap(String query, String key) {
            if (query != null && !"".equals(query)) {
                Map<String, String> result = new HashMap<String, String>();
                for (String param : query.split("&")) {
                    String pair[] = param.split("=");
                    if (pair.length > 1) {
                        if (pair[0].equalsIgnoreCase(key)) {
                            return pair[1];
                        }
                        result.put(pair[0], pair[1]);
                    } else {
                        result.put(pair[0], "");
                    }
                }
            }
            return "";
        }
    }

    static class PostHttpHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            SampleLogger.log("Method: " + method);

            InputStream is = httpExchange.getRequestBody();
            String response = "hello " + is2string(is) + " !";
            SampleLogger.log("response: " + response);
            is.close();

            byte[] responseByte = response.getBytes("utf-8");
            httpExchange.getResponseHeaders().add("Content-Type:", "text/html;charset=utf-8");
            httpExchange.sendResponseHeaders(200, responseByte.length);
            OutputStream out = httpExchange.getResponseBody();
            out.write(responseByte);
            out.flush();
            out.close();
        }

        private String is2string(InputStream is) throws IOException {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(is, "UTF-8");
            for (;;) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        }
    }
}