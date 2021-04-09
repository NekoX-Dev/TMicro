package io.nekohasekai.wsm;

public abstract class WebSocketListener {

    protected void onOpen(WebSocket socket) {
    }

    protected void onPing(WebSocket socket, byte[] payload) {
    }

    protected void onPong(WebSocket socket, byte[] payload) {
    }

    protected void onMessage(WebSocket socket, String message) {
    }

    protected void onMessage(WebSocket socket, byte[] message) {
    }

    protected void onClose(WebSocket socket, int code, String reason) {
    }

    protected void onFailure(WebSocket socket, Throwable t) {
    }

}
