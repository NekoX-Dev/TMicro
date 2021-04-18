package io.nekohasekai.tmicro;

import com.sun.lwuit.*;
import com.sun.lwuit.layouts.BorderLayout;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.ui.BaseActivity;
import io.nekohasekai.tmicro.ui.LaunchActivity;
import io.nekohasekai.tmicro.utils.LogUtil;
import io.nekohasekai.tmicro.utils.ResUtil;
import j2me.util.HashMap;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import java.io.IOException;

public class TMicro extends MIDlet {

    public static final boolean DEBUG = true;
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final int VERSION_INT = 1;

    public static String SERVER;
    public static TMicro application;

    public BaseActivity contentActivity;

    public void setContentActivity(BaseActivity activity) {
        if (contentActivity != null) {
            contentActivity.onStop();
            contentActivity = null;
        }

        contentActivity = activity;
        contentActivity.onCreate();
    }

    protected void startApp() throws MIDletStateChangeException {

        LogUtil.info("Start app");
        if (application == null) {
            LogUtil.info("Create app");
            Display.init(this);
            application = this;

            new SplashForm().show();

            new Thread() {
                public void run() {
                    HashMap properties = ResUtil.readPropertiesRes("/config.properties");
                    SERVER = (String) properties.get("SERVER");
                    ConnectionsManager.getInstance();
                }
            }.start();

            setContentActivity(new LaunchActivity());
        } else if (contentActivity != null) {
            LogUtil.info("Resume app");

            contentActivity.onResume();
        } else {
            LogUtil.info("Restart app");
        }
    }

    protected void pauseApp() {
        LogUtil.info("Pause app");

        if (contentActivity != null) {
            contentActivity.onPause();
        }
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        ConnectionsManager.getInstance().disconnect();

        if (contentActivity != null) {
            contentActivity.onDestroy(unconditional);
        }
    }

    public static class SplashForm extends Form {

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

}
