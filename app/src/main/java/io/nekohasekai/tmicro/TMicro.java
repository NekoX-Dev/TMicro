package io.nekohasekai.tmicro;

import com.sun.lwuit.Display;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.ui.LaunchActivity;
import io.nekohasekai.tmicro.utils.ResUtil;
import j2me.util.HashMap;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class TMicro extends MIDlet {

    public static String VERSION;
    public static boolean DEBUG;
    public static String SERVER;

    public static TMicro application;

    protected void startApp() throws MIDletStateChangeException {
        if (application == null) {
            Display.init(this);

            HashMap properties = ResUtil.readPropertiesRes("/config.properties");
            VERSION = (String) properties.get("VERSION");
            DEBUG = "true".equalsIgnoreCase((String) properties.get("DEBUG"));
            SERVER = (String) properties.get("SERVER");
            application = this;
            onCreate();

        } else {

        }
    }

    private void onCreate() {
        if (ConnectionsManager.getInstance().accountStatus != ConnectionsManager.STATUS_OK) {
            new LaunchActivity().show();
        }

    }

    protected void pauseApp() {

    }

    protected void destroyApp(boolean b) throws MIDletStateChangeException {

    }
}
