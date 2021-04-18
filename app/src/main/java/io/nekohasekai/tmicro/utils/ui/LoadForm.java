package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.Theme;
import io.nekohasekai.tmicro.utils.ThreadUtil;
import org.bouncycastle.util.Strings;

public class LoadForm extends Form {

    public TextView1 textView1;
    public String message;
    public LoadingThread thread;
    public transient boolean start;
    public Container contentArea;

    public LoadForm(String text) {
        message = text;
        Theme.applyTitleBar(this);
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        addComponent(contentArea = new Container(new BoxLayout(BoxLayout.Y_AXIS)) {{
            getStyle().setMargin(8, 8, 8, 8);
            addComponent(textView1 = new TextView1(message));
        }});

        start();
    }

    public void start() {
        stop();
        start = true;
        thread = new LoadingThread();
        thread.start();
    }

    public void stop() {
        start = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void finish(final String message) throws InterruptedException {
        cancel();
        ThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                textView1.setText(message);
            }
        });
    }

    public void cancel() {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = null;
    }

    public class LoadingThread extends Thread {

        int step = 0;

        public LoadingThread() {
            super("Loading Thread");
        }

        public void run() {
            while (isAlive() && start) {
                if (getComponentForm() == null) break;

                String[] lines = Strings.split(message, '\n');

                String string = lines[0] + "    ";
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

                lines[0] = string;


                final String finalString = Strings.join(lines);
                ThreadUtil.runOnUiThread(new Runnable() {
                    public void run() {
                        textView1.setText(finalString);
                    }
                });

                try {
                    Thread.sleep(300L);
                } catch (InterruptedException ignored) {
                    return;
                }

            }
        }
    }

}
