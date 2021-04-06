package io.nekohasekai.tmicro.ui;

import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.ui.view.BaseForm;
import io.nekohasekai.tmicro.ui.view.LoadingForm;
import org.json.me.JSONObject;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

public class LoginActivity extends BaseActivity {

    public InputPhoneForm inputPhoneForm;

    public void onCreate() {
        super.onCreate();
        inputPhoneForm = new InputPhoneForm();
        setContentForm(inputPhoneForm);
    }

    public class InputPhoneForm extends BaseForm {

        private TextField phoneNumber;
        private LoadingForm loadingForm;
        private LoginThread loginThread;

        public InputPhoneForm() {
            addExit();
            append(phoneNumber = new TextField("Phone number", "", 256, TextField.PHONENUMBER));

            addCommand(new Command("Login", Command.OK, 0));
        }

        public void commandAction(Command command, Displayable displayable) {
            super.commandAction(command, displayable);
            if (command.getCommandType() == Command.OK) {
                if (phoneNumber.getString().length() < 4) {
                    showAlert("Please input phone numer", AlertType.INFO);
                    return;
                }

                loadingForm = new LoadingForm();
                loadingForm.show();
                loadingForm.startLoad();

                loginThread = new LoginThread();
                loginThread.start();
            }
        }

        public void onPause() {
            super.onPause();

            if (loadingForm != null) loadingForm.stopLoad();
        }

        public void onResume() {
            super.onResume();

            if (loadingForm != null) loadingForm.startLoad();
        }

        public class LoginThread extends Thread {

            public void run() {

                try {
                    JSONObject resp = ConnectionsManager.sendRequest("", new JSONObject());
                    System.out.println(resp.toString(4));
                } catch (Exception e) {
                    showAlert("Connect failed.", AlertType.ERROR);
                    e.printStackTrace();
                }

            }
        }

    }

}
