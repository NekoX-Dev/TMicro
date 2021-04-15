package io.nekohasekai.tmicro.utils;

import com.sun.lwuit.Display;

public class ThreadUtil {

    public static void runOnUiThread(Runnable runnable) {
        if (Display.getInstance().isEdt()) {
            runnable.run();
        } else {
            Display.getInstance().callSerially(runnable);
        }
    }

}
