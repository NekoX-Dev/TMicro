package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.utils.ui.ColoredButton;
import io.nekohasekai.tmicro.utils.ui.LoadForm;

import java.io.IOException;

public abstract class NetActivity extends BaseActivity {

    private final boolean requireConnection;

    public NetActivity() {
        this(true);
    }

    public NetActivity(boolean requireConnection) {
        this.requireConnection = requireConnection;
    }

    public void onCreate() {
        super.onCreate();

        getConnectionsManager().addListener(this);

        if (requireConnection) {
            startConnect();
        }
    }

    public void onStop() {
        super.onStop();

        getConnectionsManager().removeListener(this);
    }

    public void onPause() {
        super.onPause();

        if (requireConnection) {
            getConnectionsManager().pause();
        }
    }

    public void onResume() {
        super.onResume();

        if (requireConnection) {
            getConnectionsManager().resume();
        }
    }

    public abstract NetActivity continueSelf();

    private Thread reconnect;

    public void startConnect() {
        stopConnect();
        if (ConnectionsManager.getInstance().isConnected()) {
            onConnected();
            return;
        }
        reconnect = new Thread() {
            public void run() {
                int retry = 0;
                while (isAlive()) {
                    try {
                        long start = System.currentTimeMillis();
                        try {
                            getConnectionsManager().connect();
                            break;
                        } catch (IOException e) {
                            retry++;
                            e.printStackTrace();
                            loadingUpdate("Reconnecting (" + retry + ") ...\nError: " + e.getMessage());
                            long end = System.currentTimeMillis();
                            end = end - start;
                            end = 5000 - end;
                            //noinspection BusyWait
                            Thread.sleep(end);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        loadingShow(new ConnectForm());
        reconnect.start();
    }

    public void stopConnect() {
        if (reconnect != null) {
            reconnect.interrupt();
            reconnect = null;
        }
    }

    private boolean paused;

    public void onDisconnected(int code) {
        paused = true;
        startConnect();
    }

    public void onConnected() {
        if (paused && contentForm != null) {
            contentForm.showBack();
        }
    }

    public class ConnectForm extends LoadForm {

        public ColoredButton setRelay;
        public ColoredButton cancel;

        public ConnectForm() {
            super("Connecting...");

            contentArea.addComponent(setRelay = new ColoredButton("Set Relay", new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stopSelf();
                    performActivity(new RelayActivity(continueSelf()));
                }
            }) {{
                setMargin(8, 0, 0, 0);
            }});

            contentArea.addComponent(cancel = new ColoredButton("Exit", new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stopSelf();
                    TMicro.application.notifyDestroyed();
                }
            }));

        }

        public void finish(String message) throws InterruptedException {
            setRelay.setVisible(false);
            cancel.setVisible(false);
            repaint();
            super.finish(message);
        }

        private void stopSelf() {
            try {
                if (reconnect.isAlive()) {
                    reconnect.interrupt();
                }
            } catch (Exception ignored) {
            }
            stop();
        }

    }


}
