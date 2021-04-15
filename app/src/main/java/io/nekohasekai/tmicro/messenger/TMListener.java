package io.nekohasekai.tmicro.messenger;

import io.nekohasekai.tmicro.tmnet.TMApi;

public abstract class TMListener {

    public void updateAuthorizationState(TMApi.AuthorizationState state) {
    }

}