package io.nekohasekai.tmicro.messenger;

import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.tmnet.TMApi;

import java.io.IOException;

public abstract class TMCallback {

    private Exception stacktrace;

    public TMCallback() {
        if (TMicro.DEBUG) {
            try {
                throw new IOException();
            } catch (IOException e) {
                stacktrace = e;
            }
        }
    }

    public abstract void onSuccess(TMApi.Object response);

    public void onFailure(int code, String message) {
        if (stacktrace != null) stacktrace.printStackTrace();
        StringBuffer error = new StringBuffer();
        System.err.println("RPC Error, code=" + code + ", message=" + message);
    }

}
