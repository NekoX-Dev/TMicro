package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.Locale;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.Theme;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.utils.LogUtil;
import io.nekohasekai.tmicro.utils.ui.ColoredButton;
import io.nekohasekai.tmicro.utils.ui.TextView;

import javax.microedition.lcdui.Alert;
import java.io.IOException;

public class LaunchActivity extends BaseActivity {

    public void onCreate() {

        setContentForm(new IntroForm());

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
                getStyle().setBgColor(0x000000);

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

        loadingShow("Connecting to server...");

        new Thread() {
            public void run() {
                try {
                    try {
                        ConnectionsManager.getInstance().connect();
                        loadingFinish("Connected", 500L);
                    } catch (IOException e) {
                        new Alert(Locale.getCurrent().appName);
                        loadingFinish(LogUtil.formatError(e), 3000L);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }.start();

    }
}
