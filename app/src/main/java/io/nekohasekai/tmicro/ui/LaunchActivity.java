package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.Locale;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.tmnet.TMApi;
import io.nekohasekai.tmicro.utils.LogUtil;
import io.nekohasekai.tmicro.utils.ui.ColoredButton;
import io.nekohasekai.tmicro.utils.ui.TextView;

import javax.microedition.lcdui.Alert;
import java.io.IOException;

public class LaunchActivity extends BaseActivity {

    public void onCreate() {
        super.onCreate();

        setContentForm(new SplashForm());

        if (ConnectionsManager.getInstance().accountStatus == 0) {
            setContentForm(new IntroForm());
        }

    }

    public class SplashForm extends Form {

        public SplashForm() {
            super();
            setLayout(new BorderLayout());

            try {
                addComponent(BorderLayout.CENTER, new Label(Image.createImage("/icon.png")) {{
                    getStyle().setAlignment(Container.CENTER);
                }});
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class IntroForm extends Form {

        public IntroForm() {
            super();
            setLayout(new BoxLayout(BoxLayout.Y_AXIS));

            try {
                addComponent(new Label(Image.createImage("/icon.png")) {{
                    getStyle().setMargin(Container.TOP, 16);
                    getStyle().setMargin(Container.BOTTOM, 8);
                    getStyle().setAlignment(Container.CENTER);
                }});
            } catch (IOException e) {
                e.printStackTrace();
            }

            addComponent(new Label("Telegram") {{
                getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
                getStyle().setMargin(Container.BOTTOM, 8);
                getStyle().setAlignment(Container.CENTER);
            }});

            addComponent(new TextView("The world's fastest messaging app.\nIt is free and secure.") {{
                center();
                getStyle().setMargin(Container.BOTTOM, 32);
            }});

            addComponent(new Container(new BoxLayout(BoxLayout.Y_AXIS)) {{
                getStyle().setPadding(0, 0, 32, 32);

                addComponent(new ColoredButton("Start Messaging", new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        connectServer();
                    }
                }));

                addComponent(new ColoredButton(Locale.getCurrent().exit, new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        TMicro.application.notifyDestroyed();
                    }
                }));

            }});
        }

    }

    private void connectServer() {

        if (!ConnectionsManager.getInstance().isConnected()) {
            loadingShow("Connecting to server...");

            new Thread() {
                public void run() {
                    try {
                        try {
                            ConnectionsManager.getInstance().connect();
                            loadingStop("Connected", 500L);
                            loadingShow("Loading...");
                            Thread.sleep(100L);
                            ConnectionsManager.getInstance().processUpdates();
                        } catch (IOException e) {
                            e.printStackTrace();
                            loadingStop(LogUtil.formatError(e), 3000L);
                            // TODO: SetProxy
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
            }.start();
        }
    }

    public void updateAuthorizationState(TMApi.AuthorizationState state) {

        if (state instanceof TMApi.AuthorizationStateWaitPhoneNumber) {
            loadingCancel();

            performActivity(new LoginActivity());
        }

    }


}
