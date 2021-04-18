package io.nekohasekai.tmicro.messenger;

import com.googlecode.compress_j2me.gzip.Gzip;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.tmnet.SerializedData;
import io.nekohasekai.tmicro.tmnet.TMApi;
import io.nekohasekai.tmicro.tmnet.TMStore;
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
import j2me.util.Iterator;
import j2me.util.LinkedList;
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
    public String relayAddress;

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
            relayAddress = "";
            writeConfig();
            return;
        }

        if (version > 1) {
            throw new IllegalStateException("Unknown database version " + version);
        }

        proxy = input.readString(true);
        accountSession = input.readByteArray(true);
        accountStatus = input.readInt32(true);
        relayAddress = input.readString(true);
        input.cleanup();
    }

    public void writeConfig() throws IOException {
        SerializedData output = db.getOut(1);
        output.writeInt32(1);
        output.writeString(proxy);
        output.writeByteArray(accountSession);
        output.writeInt32(accountStatus);
        output.writeString(relayAddress);
        output.flush();
        output.cleanup();
    }

    public WebSocket socket;
    public EncUtil.ChaChaSession chaChaSession;
    public int status;
    private final LinkedList listeners = new LinkedList();

    public void addListener(TMListener listener) {
        listeners.add(listener);
        if (!sendEvent) {
            sendEvent = true;
            processUpdates();
        }
    }

    public void removeListener(TMListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            sendEvent = false;
        }
    }

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

        String address = TMicro.SERVER;
        if (relayAddress.length() > 0) {
            address = relayAddress;
            int portIndex = address.lastIndexOf(':');
            if (portIndex != -1) {
                String port = address.substring(portIndex + 1);
                try {
                    Integer.parseInt(port);
                } catch (NumberFormatException invalidNumber) {
                    throw new IOException("Invalid port number: " + port);
                }
            }
        }

        try {
            socket = WebSocketClient.open(relayAddress.length() > 0 ? relayAddress : TMicro.SERVER, "/", headers, this);
        } catch (IllegalArgumentException invalidLink) {
            invalidLink.printStackTrace();
            throw new IOException(invalidLink.getMessage());
        }
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
                    TMicro.application.contentActivity.onDisconnected(400);
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

    public boolean isConnected() {
        return socket != null && socket.isActive();
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
            TMApi.Object update = TMStore.deserializeFromSteam(new SerializedData(message), true);
            processUpdate(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onOpen(WebSocket socket) {
        if (!sendEvent) {
            updates.add(new Integer(0));
            return;
        }
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) ((TMListener) iter.next()).onConnected();
    }

    protected void onClose(WebSocket socket, int code, String reason) {
        System.out.println("Connection closed, code=" + code + ", reason=" + reason);
        if (!sendEvent) {
            updates.add(new OnDisconnected(code));
            return;
        }
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) ((TMListener) iter.next()).onDisconnected(code);
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

    public void sendRequest(TMApi.Function request, TMCallback callback) throws IOException {
        if (TMicro.DEBUG) {
            LogUtil.info("Java send " + LogUtil.getClassName(request));
        }

        SerializedData data = new SerializedData();

        if (callback != null) {
            synchronized (this) {
                request.requestId = requestId++;
            }
            callbacks.put(new Integer(request.requestId), callback);
        }

        TMStore.serializeToStream(data, request);
        sendRaw(data.toByteArray());
    }

    private void processUpdate(TMApi.Object object) {
        if (object instanceof TMApi.Response) {
            TMApi.Response response = (TMApi.Response) object;
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
        } else if (object instanceof TMApi.Update) {
            TMApi.Update update = (TMApi.Update) object;
            if (TMicro.DEBUG) {
                LogUtil.info("Java received update " + LogUtil.getClassName(update));
            }
            processUpdate(update);
        } else {
            if (TMicro.DEBUG) {
                LogUtil.info("Java received unknown " + LogUtil.getClassName(object));
            }
        }
    }

    private transient boolean sendEvent = true;
    private final LinkedList updates = new LinkedList();

    private void processUpdate(TMApi.Update object) {
        if (!sendEvent) {
            updates.add(object);
        } else if (object instanceof TMApi.UpdateAuthorizationState) {
            TMApi.UpdateAuthorizationState update = (TMApi.UpdateAuthorizationState) object;
            Iterator iter = listeners.iterator();
            while (iter.hasNext()) ((TMListener) iter.next()).updateAuthorizationState(update.state);
        }
    }

    private static class OnDisconnected {
        int code;

        public OnDisconnected(int code) {
            this.code = code;
        }
    }

    private void processUpdates() {
        sendEvent = true;
        Iterator iter = updates.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof TMApi.Update) {
                processUpdate((TMApi.Update) obj);
            } else if (obj instanceof OnDisconnected) {
                Iterator iter1 = listeners.iterator();
                while (iter1.hasNext()) ((TMListener) iter1.next()).onDisconnected(((OnDisconnected) obj).code);
            } else if (obj instanceof Integer) {
                Iterator iter1 = listeners.iterator();
                switch (((Integer) obj).intValue()) {
                    case 0: {
                        while (iter1.hasNext()) ((TMListener) iter1.next()).onConnected();
                    }
                    break;
                }
            }
        }
    }

    public void pause() {
        sendEvent = false;
    }

    public void resume() {
        sendEvent = true;
        processUpdates();
    }

}