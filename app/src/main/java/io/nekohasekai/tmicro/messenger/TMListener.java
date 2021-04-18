package io.nekohasekai.tmicro.messenger;

import io.nekohasekai.tmicro.tmnet.TMApi;

public abstract class TMListener {

    protected int currentAccount;

    public TMListener(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    public ConnectionsManager getConnectionsManager() {
        return ConnectionsManager.getInstance();
    }

    public void onConnected() {
    }

    public void onDisconnected(int code) {
    }

    public void updateAuthorizationState(TMApi.AuthorizationState state) {
    }

}