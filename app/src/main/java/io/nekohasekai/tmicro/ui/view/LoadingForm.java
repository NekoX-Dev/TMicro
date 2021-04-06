package io.nekohasekai.tmicro.ui.view;

import javax.microedition.lcdui.StringItem;

public class LoadingForm extends BaseForm {

    private StringItem textItem;
    public volatile String text = "Loading...";

    public LoadingForm() {
        super();

        append(textItem = new StringItem(null, text));
    }

    public Thread loadThread;

    public void startLoad() {
        loadThread = new Thread() {
            public int step;

            public void run() {
                try {
                    while (isAlive()) {
                        String string = text + "  ";
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

                        textItem.setText(string);
                        Thread.sleep(500L);
                    }
                } catch (InterruptedException ignored) {
                    System.out.println("interrupted");
                }
            }
        };
        loadThread.start();
    }

    public void stopLoad() {
        if (loadThread != null) {
            loadThread.interrupt();
            loadThread = null;
        }
    }

}
