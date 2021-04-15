package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Dialog;
import com.sun.lwuit.Font;
import com.sun.lwuit.TextArea;
import io.nekohasekai.tmicro.utils.ThreadUtil;

public class AlertDialog {

    public Dialog dialog = new Dialog();
    public TextArea contentArea;

    public AlertDialog(final String title, final String content) {
        ThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                dialog.setDialogType(Dialog.TYPE_NONE);
                dialog.setTitle(title);
                dialog.addComponent(new TextArea(content) {{
                    contentArea = this;
                    getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
                    setFocusable(false);
                    setEditable(false);
                    getStyle().setBorder(null);
                }});
            }
        });
    }

    public void setContent(final String content) {
        dialog.dispose();
        contentArea.setText(content);
        show();
    }

    public void setError(Throwable error) {
        String message = error.getClass().getName();
        message.substring(message.lastIndexOf('.') + 1);
        if (error.getMessage() != null && error.getMessage().length() > 0) {
            message += ", " + error.getMessage();
        }
        setContent("Error: " + message);
    }

    public void show() {
        new Thread() {
            public void run() {
                dialog.show();
            }
        }.start();
    }

    public void dismiss() {
        dialog.dispose();
    }

}
