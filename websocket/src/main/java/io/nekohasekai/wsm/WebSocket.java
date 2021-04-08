package io.nekohasekai.wsm;

import javax.microedition.io.SocketConnection;
import java.io.IOException;

public interface WebSocket {

    SocketConnection getConnection();

    void send(String message) throws IOException;

    void send(byte[] message) throws IOException;

    void ping(byte[] payload) throws IOException;

    void pong(byte[] payload) throws IOException;

    void close(int code, String reason) throws IOException;

    void cancel();

    boolean isActive();

}
