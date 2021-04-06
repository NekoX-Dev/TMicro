package io.nekohasekai.tmicro.messenger;

import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.utils.IoUtil;
import io.nekohasekai.tmicro.utils.RecordUtil;
import io.nekohasekai.tmicro.utils.rms.RecordDatabase;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import java.io.*;

public class ConnectionsManager {

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

    public static JSONObject sendRequest(String path, JSONObject request) throws IOException {

        String json = request.toString();

        HttpConnection connection = (HttpConnection) Connector.open(TMicro.SERVER + "/" + path);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("UserAgent", "TMicro/" + TMicro.VERSION + " (J2ME/MIDP; TMicro; U; en)");

        OutputStream out = connection.openOutputStream();
        IoUtil.writeUTF8(out, json);
        out.close();

        int response = connection.getResponseCode();
        InputStream in = connection.openInputStream();
        String content = IoUtil.readUTF8(in);

        if (response != 200) {
            throw new IOException("HTTP " + response + ": " + content);
        }

        try {
            return new JSONObject(content);
        } catch (JSONException e) {
            throw new IOException("Invalid json format: " + e.getMessage());
        }

    }

}
