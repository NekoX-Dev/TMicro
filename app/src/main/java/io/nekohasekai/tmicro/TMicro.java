package io.nekohasekai.tmicro;

import com.sun.lwuit.Display;
import io.nekohasekai.tmicro.ui.LaunchActivity;
import io.nekohasekai.tmicro.utils.ResUtil;
import j2me.util.HashMap;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class TMicro extends MIDlet {

    public static final boolean DEBUG = true;
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final int VERSION_INT = 1;

    public static String SERVER;
    public static TMicro application;

    protected void startApp() throws MIDletStateChangeException {
        if (application == null) {
            Display.init(this);

            HashMap properties = ResUtil.readPropertiesRes("/config.properties");
            SERVER = (String) properties.get("SERVER");
            application = this;
            onCreate();

        } else {

        }
    }

    private void onCreate() {
        new LaunchActivity().show();
    }

    protected void pauseApp() {

    }

    protected void destroyApp(boolean b) throws MIDletStateChangeException {

    }
}
