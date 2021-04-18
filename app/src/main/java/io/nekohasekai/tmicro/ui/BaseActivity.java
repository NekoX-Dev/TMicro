package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.Form;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.messenger.TMListener;
import io.nekohasekai.tmicro.utils.LogUtil;
import io.nekohasekai.tmicro.utils.ui.LoadForm;

public class BaseActivity extends TMListener {

    public BaseActivity() {
        super(0);
    }

    public Form contentForm;
    public LoadForm loadingForm;

    public void setContentForm(Form from) {
        contentForm = from;
        contentForm.show();
    }

    public void onCreate() {
    }

    public void onStop() {
    }

    public void onPause() {
    }

    public void onResume() {
        if (contentForm != null) {
            contentForm.showBack();
        }
    }

    public void onDestroy(boolean unconditional) {
    }

    public void loadingShow(String message) {
        if (loadingForm != null) {
            LogUtil.warn("Loading form already exists!");
            loadingUpdate(message);
            return;
        }
        loadingShow(new LoadForm(message));
    }

    public void loadingStatic(String message) {
        if (loadingForm != null) {
            LogUtil.warn("Loading form already exists!");
            loadingUpdate(message);
            if (loadingForm.start) {
                loadingForm.stop();
            }
            return;
        }
        loadingStatic(new LoadForm(message));
    }

    public void loadingShow(LoadForm form) {
        loadingForm = form;
        loadingForm.show();
        loadingForm.start();
    }

    public void loadingStatic(LoadForm form) {
        loadingForm = form;
        loadingForm.show();
    }

    public void loadingUpdate(String message) {
        if (loadingForm == null) {
            LogUtil.warn("Loading form not exists!");
            return;
        }
        loadingForm.message = message;
    }

    public void loadingCancel() {
        if (loadingForm == null) return;
        loadingForm.cancel();
        loadingForm = null;
    }

    public void loadingStop(String message, long delay) throws InterruptedException {
        if (loadingForm == null) {
            LogUtil.warn("Loading form not exists!");
            return;
        }
        loadingForm.finish(message);
        Thread.sleep(delay);
        loadingForm = null;
    }

    public void loadingFinish(String message, long delay) throws InterruptedException {
        if (loadingForm == null) {
            LogUtil.warn("Loading form not exists!");
            return;
        }
        loadingForm.finish(message);
        Thread.sleep(delay);
        contentForm.showBack();
        loadingForm = null;
    }

    public void performActivity(BaseActivity activity) {
        TMicro.application.setContentActivity(activity);
    }

}