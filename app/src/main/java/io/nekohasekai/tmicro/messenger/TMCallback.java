package io.nekohasekai.tmicro.messenger;

import io.nekohasekai.tmicro.tmnet.TMApi;

public abstract class TMCallback {

    public abstract void onSuccess(TMApi.Object response);
    public abstract void onFailure(int code, String message);

}
