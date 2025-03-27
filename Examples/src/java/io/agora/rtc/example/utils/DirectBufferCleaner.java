package io.agora.rtc.example.utils;

import java.nio.ByteBuffer;

public class DirectBufferCleaner {

    public static boolean release(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect()) {
            return false;
        }

        buffer.clear();
        buffer.position(0);
        buffer.limit(0);

        buffer = null;

        return true;
    }
}