package io.nekohasekai.tmicro.messenger;

import com.googlecode.compress_j2me.gzip.Gzip;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.utils.EncUtil;
import io.nekohasekai.tmicro.utils.IoUtil;
import io.nekohasekai.tmicro.utils.RecordUtil;
import io.nekohasekai.tmicro.utils.rms.RecordDatabase;
import io.nekohasekai.wsm.WebSocket;
import io.nekohasekai.wsm.WebSocketClient;
import io.nekohasekai.wsm.WebSocketListener;
import j2me.util.HashMap;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.util.Strings;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.*;

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
        if (TMicro.DEBUG) {
            try {
                RecordStore.deleteRecordStore("tgnet" + accountNum);
            } catch (RecordStoreException ignored) {
            }
        }
        db = RecordUtil.openPrivate("tgnet" + accountNum);

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
    public EncUtil.ChaChaSession chaChaSession;

    public void connect() throws IOException {
        byte[] key = EncUtil.mkChaChaKey();
        chaChaSession = new EncUtil.ChaChaSession(key);

        HashMap headers = new HashMap();
        headers.put("Authorization", "Basic " + EncUtil.publicEncode(key));

        socket = WebSocketClient.open(TMicro.SERVER, "/", headers, this);
    }

    public void disconnect() {

        socket.cancel();

    }

    public void onPing(final WebSocket socket, final byte[] payload) {
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
        try {
            if (message[0] == (byte) 0x1f && message[1] == (byte) 0x8b) {
                ByteArrayOutputStream gzOut = new ByteArrayOutputStream();
                Gzip.gunzip(new ByteArrayInputStream(message), gzOut);
                message = gzOut.toByteArray();
            }
            onMessage(socket, Strings.fromByteArray(chaChaSession.readMessage(message)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CryptoException e) {
            e.printStackTrace();
        }
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

    public void sendRequestRaw(byte[] request) throws IOException {
        try {
            request = chaChaSession.mkMessage(request);
            if (request.length > 1024) {
                ByteArrayOutputStream gzOut = new ByteArrayOutputStream();
                Gzip.gzip(IoUtil.getIn(request), gzOut);
                request = gzOut.toByteArray();
            }
            socket.send(request);
        } catch (CryptoException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

}
