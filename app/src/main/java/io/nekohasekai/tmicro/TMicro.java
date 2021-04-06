package io.nekohasekai.tmicro;

import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.ui.BaseActivity;
import io.nekohasekai.tmicro.ui.LoginActivity;
import io.nekohasekai.tmicro.utils.ResUtil;
import j2me.util.HashMap;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class TMicro extends MIDlet {

    public static String VERSION;
    public static boolean DEBUG;
    public static String SERVER;

    public static Display display;
    public static TMicro application;

    public BaseActivity contentActivity;

    protected void startApp() throws MIDletStateChangeException {
        if (contentActivity == null) {
            HashMap properties = ResUtil.readPropertiesRes("/config.properties");
            VERSION = (String) properties.get("VERSION");
            DEBUG = "true".equalsIgnoreCase((String) properties.get("DEBUG"));
            SERVER = (String) properties.get("SERVER");

            display = Display.getDisplay(this);
            application = this;
            onCreate();

        } else {
            contentActivity.onResume();
        }
    }

    private void onCreate() {
        if (ConnectionsManager.getInstance().accountStatus != ConnectionsManager.STATUS_OK) {
            performActivity(new LoginActivity());
        }
    }

    public void performActivity(BaseActivity contentActivity) {
        this.contentActivity = contentActivity;
        contentActivity.onCreate();
    }

    protected void pauseApp() {
        if (contentActivity != null) {
            contentActivity.onPause();
        }
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        if (contentActivity != null) {
            contentActivity.onDestroy(unconditional);
        }
    }

}
