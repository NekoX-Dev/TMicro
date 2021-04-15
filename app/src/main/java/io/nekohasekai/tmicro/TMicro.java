package io.nekohasekai.tmicro;

import com.sun.lwuit.Display;
import com.sun.lwuit.plaf.DefaultLookAndFeel;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.UIManager;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.ui.BaseActivity;
import io.nekohasekai.tmicro.ui.LaunchActivity;
import io.nekohasekai.tmicro.utils.FileUtil;
import io.nekohasekai.tmicro.utils.LogUtil;
import io.nekohasekai.tmicro.utils.ResUtil;
import j2me.util.HashMap;
import j2me.util.LinkedList;

import javax.microedition.io.Connector;
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
    public LinkedList listeners;

    public TMicro() {
        listeners = new LinkedList();
    }

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

            HashMap properties = ResUtil.readPropertiesRes("/config.properties");
            SERVER = (String) properties.get("SERVER");
            application = this;

            try {
                Connector.open("socket://localhost");
                FileUtil.getFile("non-exists-file").close();
            } catch (SecurityException e) {
                TMicro.application.notifyDestroyed();
                return;
            } catch (Exception ignored) {
            }

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
}
