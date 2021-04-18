package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.Locale;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.tmnet.TMApi;
import io.nekohasekai.tmicro.utils.FileUtil;
import io.nekohasekai.tmicro.utils.ui.ColoredButton;
import io.nekohasekai.tmicro.utils.ui.TextView;

import javax.microedition.io.Connector;
import java.io.IOException;

public class LaunchActivity extends NetActivity {

    private final boolean skipIntro;

    public LaunchActivity() {
        this(false);
    }

    public LaunchActivity(boolean skipIntro) {
        super(false);
        this.skipIntro = skipIntro;
    }

    public NetActivity continueSelf() {
        return new LaunchActivity(true);
    }

    public void onCreate() {
        super.onCreate();

        if (!skipIntro) {
            try {
                Connector.open("socket://localhost");
                FileUtil.getFile("non-exists-file").close();
            } catch (SecurityException e) {
                TMicro.application.notifyDestroyed();
                return;
            } catch (Exception ignored) {
            }
        }

        if (ConnectionsManager.getInstance().accountStatus == 0) {
            if (skipIntro) {
                startConnect();
            } else {
                setContentForm(new IntroForm());
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
                        startConnect();
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

    public void updateAuthorizationState(TMApi.AuthorizationState state) {
        if (state instanceof TMApi.AuthorizationStateWaitPhoneNumber) {
            loadingCancel();
            performActivity(new LoginActivity());
        }
    }
}
