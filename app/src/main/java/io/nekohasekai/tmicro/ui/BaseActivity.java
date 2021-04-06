package io.nekohasekai.tmicro.ui;

import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.ui.view.BaseForm;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

public class BaseActivity {

    public BaseForm contentForm;

    public void onCreate() {
    }

    public void setContentForm(BaseForm contentForm) {
        this.contentForm = contentForm;
        this.contentForm.show();
    }

    public void onPause() {
        if (contentForm != null) {
            contentForm.onPause();
        }
    }

    public void onResume() {
        if (contentForm != null) {
            contentForm.onResume();
        }
    }

    public void onDestroy(boolean unconditional) {
        if (contentForm != null) {
            contentForm.onDestroy(unconditional);
        }
    }

    public Alert showAlert(String text, AlertType type) {
        return showAlert(text, type, contentForm);
    }

    public Alert showAlert(String text, AlertType type, BaseForm nextForm) {
        Alert alert = new Alert(contentForm.getTitle(), text, null, type);
        TMicro.display.setCurrent(alert, nextForm);
        return alert;
    }

}
