package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.Locale;
import io.nekohasekai.tmicro.utils.ThreadUtil;

public class LoadForm extends Form {

    public TextView textView;
    public String message;
    public LoadingThread thread;

    public LoadForm(String text) {
        message = text;

        setTitle(Locale.getCurrent().appName);
        getTitleStyle().setFgColor(0xffffff);
        getTitleStyle().setBgColor(0x448aff);
        getTitleStyle().setPadding(Container.LEFT, 8);
        getTitleStyle().setAlignment(Container.LEFT);

        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        addComponent(new Container(new BoxLayout(BoxLayout.Y_AXIS)) {{
            getStyle().setMargin(8, 8, 8, 8);
            addComponent(textView = new TextView(message));
        }});

        start();
    }

    public void start() {
        stop();
        thread = new LoadingThread();
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void finish(final String message) throws InterruptedException {

        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                textView.setText(message);
            }
        });

    }

    public class LoadingThread extends Thread {

        int step = 0;

        public LoadingThread() {
            super("Loading Thread");
        }

        public void run() {
            while (isAlive()) {
                String string = message + "    ";
                if (step == 0) {
                    string += "/";
                    step = 1;
                } else if (step == 1) {
                    string += "-";
                    step = 2;
                } else if (step == 2) {
                    string += "\\";
                    step = 3;
                } else if (step == 3) {
                    string += "|";
                    step = 0;
                }

                final String finalString = string;
                ThreadUtil.runOnUiThread(new Runnable() {
                    public void run() {
                        textView.setText(finalString);
                    }
                });

                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ignored) {
                    return;
                }

            }
        }
    }

}
