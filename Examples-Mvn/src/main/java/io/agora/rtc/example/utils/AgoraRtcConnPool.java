package io.agora.rtc.example.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.SampleLogger;

public class AgoraRtcConnPool {

    private final Queue<AgoraRtcConn> idleConnections = new ConcurrentLinkedQueue<>();
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final int maxPoolSize;

    public AgoraRtcConnPool(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public synchronized AgoraRtcConn getRtcConn(AgoraService service, RtcConnConfig ccfg, boolean forceNew) {
        AgoraRtcConn conn = null;

        if (!forceNew) {
            conn = idleConnections.poll();
        }

        if (conn == null) {
            if (totalConnections.get() < maxPoolSize) {
                conn = service.agoraRtcConnCreate(ccfg);
                SampleLogger.log("agoraRtcConnCreate conn: " + conn);
                totalConnections.incrementAndGet();
            } else {
                throw new RuntimeException("Connection pool reached max capacity");
            }
        }

        return conn;
    }

    public synchronized void addIdleConn(AgoraRtcConn conn) {
        idleConnections.offer(conn);
    }

    public synchronized void releaseAllConn() {
        AgoraRtcConn conn;
        while ((conn = idleConnections.poll()) != null) {
            conn.destroy();
            totalConnections.decrementAndGet();
        }
    }
}
