package io.nekohasekai.tmicro.messenger;

import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.utils.EncUtil;
import io.nekohasekai.tmicro.utils.RecordUtil;
import io.nekohasekai.tmicro.utils.rms.RecordDatabase;
import io.nekohasekai.wsm.WebSocket;
import io.nekohasekai.wsm.WebSocketClient;
import io.nekohasekai.wsm.WebSocketListener;
import j2me.util.HashMap;
import org.bouncycastle.util.Strings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ConnectionsManager extends WebSocketListener {

    private static ConnectionsManager INSTANCE;

    public static ConnectionsManager getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new ConnectionsManager(0);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(e.getMessage());
            }
        }
        return INSTANCE;
    }

    public static int STATUS_WAIT_PHONE = 0;
    public static int STATUS_WAIT_CODE = 1;
    public static int STATUS_WAIT_PSWD = 2;
    public static int STATUS_OK = 3;

    public RecordDatabase db;

    public int accountNum;
    public int accountStatus;
    public String accountToken = "";
    public long userId;

    public ConnectionsManager(int accountNum) throws IOException {
        this.accountNum = accountNum;
        this.db = RecordUtil.openPrivate("tgnet" + accountToken);

        loadConfig();
    }

    private void loadConfig() throws IOException {
        DataInputStream input;
        try {
            input = db.getIn(0);
        } catch (IOException ignored) {
            return;
        }

        int version = input.readInt();
        accountStatus = input.readInt();
        accountToken = input.readUTF();
        userId = input.readLong();
        input.close();
    }

    private void writeConfig() throws IOException {
        DataOutputStream output = db.getOut(0);
        output.writeInt(0);
        output.writeInt(accountStatus);
        output.writeUTF(accountToken);
        output.writeLong(userId);
    }

    public WebSocket socket;
    public byte[] key;

    public void connect() throws IOException {
        key = EncUtil.generateChaCha20Poly1305Key();

        HashMap headers = new HashMap();
        headers.put("Authorization", "Basic " + EncUtil.publicEncode(key));

        socket = WebSocketClient.open(TMicro.SERVER, "/", headers, this);
    }

    public void onPing(WebSocket socket, byte[] payload) {
        try {
            socket.pong(payload);
            System.out.println("Pong sent");
        } catch (IOException e) {
            System.err.println("Pong failed");
            e.printStackTrace();
        }
    }

    public void onPong(WebSocket socket, byte[] payload) {
        System.out.println("Pong received");
    }

    public void onMessage(WebSocket socket, byte[] message) {
        EncUtil.processChaCha20Poly1305(key, true, message);
        onMessage(socket, Strings.fromByteArray(message));
    }

    public void onMessage(WebSocket socket, String message) {
        System.out.println("Received: " + message);
    }

    public void onClose(WebSocket socket, int code, String reason) {
        System.out.println("Connection closed, code=" + code + ", reason=" + reason);
    }

    public void onFailure(WebSocket socket, Throwable t) {
        System.out.println("Connection failed: ");
        t.printStackTrace();
    }

    public void sendRequestRaw(String content) throws IOException {
        socket.send(EncUtil.processChaCha20Poly1305(key, true, Strings.toByteArray(content)));
    }

}
