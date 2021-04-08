package io.nekohasekai.wsm;

import com.googlecode.compress_j2me.gzip.Gzip;
import j2me.security.SecureRandom;
import j2me.util.HashMap;
import j2me.util.Iterator;
import j2me.util.Map;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import java.io.*;

public class WebSocketClient implements WebSocket {

    private static final int B0_FLAG_FIN = 128;
    private static final int B0_FLAG_RSV1 = 64;
    private static final int B0_FLAG_RSV2 = 32;
    private static final int B0_FLAG_RSV3 = 16;
    private static final int B0_MASK_OPCODE = 15;

    private static final int OPCODE_FLAG_CONTROL = 8;
    private static final int B1_FLAG_MASK = 128;
    private static final int B1_MASK_LENGTH = 127;

    public static final int OPCODE_CONTINUATION = 0x0;
    public static final int OPCODE_TEXT = 0x1;
    public static final int OPCODE_BINARY = 0x2;
    public static final int OPCODE_CONTROL_CLOSE = 0x8;
    public static final int OPCODE_CONTROL_PING = 0x9;
    public static final int OPCODE_CONTROL_PONG = 0xa;

    private static final long PAYLOAD_BYTE_MAX = 125L;
    private static final long CLOSE_MESSAGE_MAX = PAYLOAD_BYTE_MAX - 2;
    private static final int PAYLOAD_SHORT = 126;
    private static final long PAYLOAD_SHORT_MAX = 0xffffL;
    private static final int PAYLOAD_LONG = 127;
    private static final int CLOSE_CLIENT_GOING_AWAY = 1001;
    private static final int CLOSE_NO_STATUS_CODE = 1005;
    private static final long DEFAULT_MINIMUM_DEFLATE_SIZE = 1024L;

    private boolean isRunning;
    private Thread inboundThread;
    private SocketConnection connection;
    private DataInputStream input;
    private DataOutputStream output;
    private WebSocketListener listener;

    public SocketConnection getConnection() {
        return connection;
    }

    private byte[] maskKey = new byte[4];
    private String wsKey;
    private boolean messageDeflate;

    private WebSocketClient(String host, String path, Map headers, WebSocketListener listener) throws IOException {
        this.listener = listener;
        this.connection = (SocketConnection) Connector.open("socket://" + host, Connector.READ_WRITE);
        this.input = connection.openDataInputStream();
        this.output = connection.openDataOutputStream();

        byte[] nonce = new byte[16];
        SecureRandom.INSTANCE.nextBytes(nonce);
        this.wsKey = Base64.toBase64String(nonce);

        HashMap requestHeaders = new HashMap();
        requestHeaders.put("Host", host);
        requestHeaders.put("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Mobile Safari/537.36");

        requestHeaders.putAll(headers);
        requestHeaders.put("Upgrade", "WebSocket");
        requestHeaders.put("Connection", "Upgrade");
        requestHeaders.put("Sec-WebSocket-Version", "13");
        requestHeaders.put("Sec-WebSocket-Extensions", "permessage-deflate");
        requestHeaders.put("Sec-WebSocket-Key", wsKey);

        String EOL = "\r\n";

        StringBuffer request = new StringBuffer();
        request.append("GET ");
        request.append(path);
        request.append(" HTTP/1.1");
        request.append(EOL);

        //noinspection SpellCheckingInspection
        Iterator iter = requestHeaders.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry header = (Map.Entry) iter.next();
            request.append((String) header.getKey());
            request.append(": ");
            request.append((String) header.getValue());
            request.append(EOL);
        }

        request.append(EOL);

        output.write(Strings.toByteArray(request.toString()));
        output.flush();

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        int lB1, lB2 = 0, lB3 = 0, lB4 = 0;

        do {
            int data = input.read();
            if (data == -1) throw new IOException("Unexpected EOF.");
            response.write(data);

            lB1 = lB2;
            lB2 = lB3;
            lB3 = lB4;
            lB4 = data;

            // build mini queue to check for \r\n\r\n sequence in handshake
        } while (lB1 != 13 || lB2 != 10 || lB3 != 13 || lB4 != 10);

        // There is no toString(format) in J2ME!!!
        //noinspection StringOperationCanBeSimplified
        String responseText = new String(response.toByteArray(), "UTF-8");

        System.out.println("Request: ");
        System.out.println(request.toString());

        System.out.println("Response: \n");
        System.out.println(responseText);

        messageDeflate = responseText.indexOf("permessage-deflate") != -1;

        listener.onOpen(this);

        inboundThread = new Thread() {
            public void run() {
                loopMessages();
            }
        };
        inboundThread.start();
    }

    public static WebSocketClient open(String host, String path, Map headers, WebSocketListener listener) throws IOException {
        return new WebSocketClient(host, path, headers, listener);
    }

    public void send(byte[] data, int opCode) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        boolean messageDeflate = this.messageDeflate && data.length >= DEFAULT_MINIMUM_DEFLATE_SIZE;

        out.write(((!messageDeflate ? 1 << 7 : 11 << 6) | opCode) & 0xFF);
        if (data.length < 126) {
            out.write(((1 << 7) | data.length) & 0xFF);
        } else if (data.length < 2 << 16) {
            out.write(((1 << 7) | 126) & 0xFF);
            out.writeShort(data.length);
        } else {
            out.write(((1 << 7) | 127) & 0xFF);
            out.writeLong(data.length);
        }

        if (messageDeflate) {
            ByteArrayInputStream deflateIn = new ByteArrayInputStream(data);
            ByteArrayOutputStream deflateOut = new ByteArrayOutputStream();
            Gzip.deflate(deflateIn, deflateOut);
            data = deflateOut.toByteArray();
        }

        SecureRandom.INSTANCE.nextBytes(maskKey);
        out.write(maskKey);

        for (int i = 0; i < data.length; i++) {
            byte oci = data[i];
            int j = i % 4;
            byte mkj = maskKey[j];
            data[i] = (byte) (oci ^ mkj);
        }

        out.write(data);
        out.flush();
        out.close();

        output.write(bytes.toByteArray());
        output.flush();
    }

    public void send(String message) throws IOException {
        send(Strings.toByteArray(message), OPCODE_TEXT);
    }

    public void send(byte[] message) throws IOException {
        send(message, OPCODE_BINARY);
    }

    public void ping(byte[] payload) throws IOException {
        send(payload, OPCODE_CONTROL_PING);
    }

    public void pong(byte[] payload) throws IOException {
        send(payload, OPCODE_CONTROL_PONG);
    }

    public void close(int code, String reason) throws IOException {
        ByteArrayOutputStream controlFrame = new ByteArrayOutputStream();
        DataOutputStream controlOutput = new DataOutputStream(controlFrame);
        controlOutput.writeShort(code);
        controlOutput.writeUTF(reason);

        send(controlFrame.toByteArray(), OPCODE_CONTROL_CLOSE);
    }

    public void cancel() {
        isRunning = false;
        inboundThread.interrupt();

        try {
            output.close();
        } catch (Exception ignored) {
        }
        try {
            input.close();
        } catch (Exception ignored) {
        }
        try {
            connection.close();
        } catch (Exception ignored) {
        }
    }

    private int opcode;
    private ByteArrayOutputStream frameBuffer;

    private void loopMessage() throws IOException {

        int b0 = input.read() & 0xFF;
        int opcode = b0 & B0_MASK_OPCODE;

        boolean isFinalFrame = (b0 & B0_FLAG_FIN) != 0;
        boolean isControlFrame = (b0 & OPCODE_FLAG_CONTROL) != 0;
        if (isControlFrame && !isFinalFrame) {
            throw new IOException("Control frames must be final.");
        }
        boolean reservedFlag1 = (b0 & B0_FLAG_RSV1) != 0;
        boolean readingCompressedMessage = false;
        if (reservedFlag1) {
            if (opcode == OPCODE_BINARY || opcode == OPCODE_TEXT) {
                readingCompressedMessage = true;
            } else if (opcode != OPCODE_CONTINUATION) {
                throw new IOException("Unexpected rsv1 flag, opcode=" + opcode);
            }
        }
        boolean reservedFlag2 = (b0 & B0_FLAG_RSV2) != 0;
        if (reservedFlag2) throw new IOException("Unexpected rsv2 flag");
        boolean reservedFlag3 = (b0 & B0_FLAG_RSV3) != 0;
        if (reservedFlag3) throw new IOException("Unexpected rsv3 flag");

        int b1 = input.read() & 0xFF;
        boolean isMusked = (b1 & B1_FLAG_MASK) != 0;
        if (isMusked) {
            throw new IOException("Server-sent frames must not be masked.");
        }
        long frameLength = b1 & B1_MASK_LENGTH;
        if (frameLength == PAYLOAD_SHORT) {
            frameLength = input.readShort() & 0xFFFF;
        } else if (frameLength == PAYLOAD_LONG) {
            frameLength = input.readLong();
            if (frameLength < 0L) {
                throw new IOException("Frame length " + frameLength + " > 0x7FFFFFFFFFFFFFFF");
            }
        }

        if (frameLength > Integer.MAX_VALUE) {
            throw new IOException("Frame length too long: " + frameLength);
        }

        if (isControlFrame && frameLength > PAYLOAD_BYTE_MAX) {
            throw new IOException("Control frame must be less than " + PAYLOAD_BYTE_MAX + "B.");
        }

        byte[] message = new byte[(int) frameLength];
        if (frameLength > 0L) {
            input.readFully(message);
        }

        if (isControlFrame) {

            if (opcode == OPCODE_CONTROL_PING) {
                listener.onPing(this, message);
                return;
            } else if (opcode == OPCODE_CONTROL_PONG) {
                listener.onPong(this, message);
                return;
            } else if (opcode == OPCODE_CONTROL_CLOSE) {
                int code = CLOSE_NO_STATUS_CODE;
                String reason = "";

                if (frameLength == 1) {
                    throw new IOException("Malformed close payload length of 1.");
                } else if (frameLength > 0) {
                    DataInputStream closeIn = new DataInputStream(new ByteArrayInputStream(message));
                    code = closeIn.readShort();
                    reason = closeIn.readUTF();
                }

                listener.onClose(this, code, reason);
                return;
            } else {
                throw new IOException("Unknown control opcode: 0x" + Integer.toHexString(opcode));
            }
        } else if (opcode != OPCODE_TEXT && opcode != OPCODE_BINARY && opcode != OPCODE_CONTINUATION) {
            throw new IOException("Unknown opcode:  0x" + Integer.toHexString(opcode));
        } else if (!isFinalFrame) {
            if (opcode != 0) {
                this.opcode = opcode;
            }
            if (frameBuffer == null) {
                frameBuffer = new ByteArrayOutputStream();
            }
            frameBuffer.write(message);
            return;
        } else if (frameBuffer != null) {
            if (opcode == OPCODE_CONTINUATION) {
                opcode = this.opcode;
                frameBuffer.write(message);
                message = frameBuffer.toByteArray();
                frameBuffer = null;
            } else {
                throw new IOException("Unfinished message.");
            }
        }

        if (readingCompressedMessage) {
            ByteArrayInputStream inflateIn = new ByteArrayInputStream(message);
            ByteArrayOutputStream inflateOut = new ByteArrayOutputStream();
            Gzip.inflate(inflateIn, inflateOut);
            message = inflateOut.toByteArray();
        }

        if (opcode == OPCODE_TEXT) {
            listener.onMessage(this, Strings.fromByteArray(message));
        } else {
            listener.onMessage(this, message);
        }

    }

    private void loopMessages() {
        isRunning = true;

        try {
            while (isRunning) loopMessage();
        } catch (Throwable ex) {
            isRunning = false;
            listener.onFailure(this, ex);
        }
    }

    public boolean isActive() {
        return isRunning;
    }

}
