package io.nekohasekai.wsm;

public abstract class WebSocketListener {

    public void onOpen(WebSocket socket) {
    }

    public void onPing(WebSocket socket, byte[] payload) {
    }

    public void onPong(WebSocket socket, byte[] payload) {
    }

    public void onMessage(WebSocket socket, String message) {
    }

    public void onMessage(WebSocket socket, byte[] message) {
    }

    public void onClose(WebSocket socket, int code, String reason) {
    }

    public void onFailure(WebSocket socket, Throwable t) {
    }

}
