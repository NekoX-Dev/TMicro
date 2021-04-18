package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Command;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import io.nekohasekai.tmicro.Theme;

public class AlertDialog implements ActionListener {

    public Dialog dialog = new Dialog();
    public TextView1 contentArea;

    static {
        Dialog.setDefaultDialogType(Dialog.TYPE_NONE);
        Dialog.setDialogTitleCompatibilityMode(true);
    }

    public AlertDialog(final String title, final String content) {

        Theme.applyTitleBar(dialog.getComponentForm());
        dialog.setTitle(title);
        dialog.addComponent(contentArea = new TextView1(content));
        dialog.getComponentForm().getStyle().setPadding(0, 0, 0, 0);
        dialog.getComponentForm().getStyle().setMargin(0, 0, 0, 0);
        dialog.addCommand(new Command("", 0));
        dialog.addCommand(new Command("Ok", 1));
        dialog.addCommandListener(this);
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
        dialog.show();
    }

    public void dismiss() {
        dialog.dispose();
    }

    public void keyReleased(int keyCode) {
        dismiss();
    }

    public void actionPerformed(ActionEvent evt) {
        dismiss();
    }

}
