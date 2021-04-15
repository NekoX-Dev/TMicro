package io.nekohasekai.tmicro.messenger;

import com.googlecode.compress_j2me.gzip.Gzip;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.tmnet.SerializedData;
import io.nekohasekai.tmicro.tmnet.TMApi;
import io.nekohasekai.tmicro.tmnet.TMClassStore;
import io.nekohasekai.tmicro.utils.EncUtil;
import io.nekohasekai.tmicro.utils.IoUtil;
import io.nekohasekai.tmicro.utils.LogUtil;
import io.nekohasekai.tmicro.utils.RecordUtil;
import io.nekohasekai.tmicro.utils.rms.RecordDatabase;
import io.nekohasekai.wsm.WebSocket;
import io.nekohasekai.wsm.WebSocketClient;
import io.nekohasekai.wsm.WebSocketListener;
import j2me.lang.IllegalStateException;
import j2me.util.HashMap;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.util.encoders.Base64;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.ByteArrayOutputStream;
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

    public RecordDatabase db;

    public int accountNum;
    public String proxy;
    public byte[] accountSession;
    public int accountStatus;

    private ECPrivateKeyParameters accountPk;

    public ConnectionsManager(int accountNum) throws IOException {
        this.accountNum = accountNum;
        if (TMicro.DEBUG) {
            try {
                RecordStore.deleteRecordStore("tmnet" + accountNum);
            } catch (RecordStoreException ignored) {
            }
        }
        db = RecordUtil.openPrivate("tmnet" + accountNum);

        loadConfig();
    }

    private void loadConfig() throws IOException {
        SerializedData input;
        int version;
        try {
            input = db.getIn(1);
            version = input.readInt32(false);
            if (version == 0) {
                throw new IOException();
            }
        } catch (IOException ignored) {
            proxy = "";
            accountSession = EncUtil.generateSM2PrivateKey().getD().toByteArray();
            writeConfig();
            return;
        }

        if (version > 1) {
            throw new IllegalStateException("Unknown database version " + version);
        }

        proxy = input.readString(true);
        accountSession = input.readByteArray(true);
        accountStatus = input.readInt32(true);
        input.cleanup();
    }

    private void writeConfig() throws IOException {
        SerializedData output = db.getOut(1);
        output.writeInt32(1);
        output.writeString(proxy);
        output.writeByteArray(accountSession);
        output.writeInt32(accountStatus);
        output.flush();
    }

    public WebSocket socket;
    public EncUtil.ChaChaSession chaChaSession;
    public int status;

    public void connect() throws IOException {
        if (socket != null && socket.isActive()) {
            LogUtil.warn("Already connected");
            throw new IOException("Already connected");
        }

        int time = (int) (System.currentTimeMillis() / 1000);
        byte[] key = EncUtil.mkChaChaKey();
        chaChaSession = new EncUtil.ChaChaSession(key, time);

        HashMap headers = new HashMap();
        SerializedData data = new SerializedData();
        data.writeByteArray(key);
        data.writeInt32(time);
        String authorization = Base64.toBase64String(EncUtil.publicEncode(data.toByteArray()));
        headers.put("Authorization", "Basic " + authorization);

        socket = WebSocketClient.open(TMicro.SERVER, "/", headers, this);
        TMApi.InitConnection request = new TMApi.InitConnection();
        request.layer = TMApi.LAYER;
        request.appVersion = TMicro.VERSION_INT;
        request.platform = System.getProperty("microedition.platform");
        if (request.platform == null) {
            request.platform = "n/a";
        }
        request.systemVersion = System.getProperty("microedition.profiles");
        if (request.systemVersion == null) {
            request.systemVersion = "n/a";
        } else {
            request.systemVersion += " / " + System.getProperty("microedition.configuration");
        }
        accountPk = EncUtil.loadSM2PrivateKey(accountSession);
        request.session = EncUtil.generateSM2PublicKey(accountPk).getQ().getEncoded(true);
        sendRequest(request, new TMCallback() {
            public void onSuccess(TMApi.Object response) {
                TMApi.ConnInitTemp temp = (TMApi.ConnInitTemp) response;
                try {
                    tmContinue(EncUtil.processSM2(accountPk, false, temp.data));
                } catch (IOException e) {
                    LogUtil.error(e, "Verify connection failed");
                    TMicro.application.contentActivity.onDisconnected("Verify connection failed");
                    disconnect();
                }
            }
        });
    }

    private void tmContinue(byte[] verified) throws IOException {
        TMApi.VerifyConnection request = new TMApi.VerifyConnection();
        request.data = verified;
        sendRequest(request, new TMCallback() {
            public void onSuccess(TMApi.Object response) {
                System.out.println("Init connection finished: " + response);
            }
        });
    }

    public void disconnect() {
        if (socket != null) socket.cancel();
    }

    protected void onPing(final WebSocket socket, final byte[] payload) {
        try {
            socket.pong(payload);
            System.out.println("Pong sent");
        } catch (IOException e) {
            System.err.println("Pong failed");
            e.printStackTrace();
        }
    }

    protected void onPong(WebSocket socket, byte[] payload) {
        System.out.println("Pong received");
    }

    protected void onMessage(WebSocket socket, byte[] message) {
        try {
            if (message[0] == (byte) 0x1f && message[1] == (byte) 0x8b) {
                ByteArrayOutputStream gzOut = IoUtil.out();
                Gzip.gunzip(IoUtil.getIn(message), gzOut);
                message = gzOut.toByteArray();
            }
            message = chaChaSession.readMessage(message);
            TMApi.Object update = TMClassStore.deserializeFromSteam(new SerializedData(message), true);
            processUpdate(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onClose(WebSocket socket, int code, String reason) {
        System.out.println("Connection closed, code=" + code + ", reason=" + reason);
    }

    protected void onFailure(WebSocket socket, Throwable t) {
        System.out.println("Connection failed: ");
        t.printStackTrace();
    }

    private void sendRaw(byte[] request) throws IOException {
        if (socket == null) {
            throw new IOException("Not started");
        } else if (!socket.isActive()) {
            throw new IOException("Not connected");
        }
        try {
            request = chaChaSession.mkMessage(request);
            if (request.length > 1024) {
                ByteArrayOutputStream gzOut = IoUtil.out();
                Gzip.gzip(IoUtil.getIn(request), gzOut);
                request = gzOut.toByteArray();
            }
            socket.send(request);
        } catch (CryptoException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private volatile int requestId = 0;
    private final HashMap callbacks = new HashMap();

    private void sendRequest(TMApi.Function request, TMCallback callback) throws IOException {
        if (TMicro.DEBUG) {
            LogUtil.info("Java send " + LogUtil.getClassName(request));
        }

        SerializedData data = new SerializedData();

        if (callback == null) {
            TMClassStore.serializeToStream(data, request);
        } else {
            TMApi.RpcRequest requestWithId = new TMApi.RpcRequest();
            synchronized (this) {
                requestWithId.requestId = requestId++;
            }
            callbacks.put(new Integer(requestWithId.requestId), callback);
            requestWithId.request = request;
            TMClassStore.serializeToStream(data, requestWithId);
        }

        sendRaw(data.toByteArray());
    }

    private void processUpdate(TMApi.Object update) {
        if (update instanceof TMApi.RpcResponse) {
            TMApi.RpcResponse response = (TMApi.RpcResponse) update;
            if (TMicro.DEBUG) {
                LogUtil.info("Java received " + LogUtil.getClassName(response.response));
            }
            TMCallback callback = (TMCallback) callbacks.remove(new Integer(response.requestId));
            if (callback == null) {
                System.err.println("Unknown response of requestId " + response.requestId);
                return;
            }
            if (response.response instanceof TMApi.Error) {
                TMApi.Error error = (TMApi.Error) response.response;
                callback.onFailure(error.code, error.message);
            } else {
                callback.onSuccess(response.response);
            }
        } else {
            if (TMicro.DEBUG) {
                LogUtil.info("Java received unknown " + LogUtil.getClassName(update));
            }
        }
    }

}
