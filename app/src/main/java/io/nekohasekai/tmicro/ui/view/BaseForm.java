package io.nekohasekai.tmicro.ui.view;

import io.nekohasekai.tmicro.Locale;
import io.nekohasekai.tmicro.TMicro;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

public class BaseForm extends Form implements CommandListener {

    public BaseForm() {
        super(Locale.APP_NAME);
        setCommandListener(this);
    }

    public void show() {
        TMicro.display.setCurrent(this);
    }

    public void addExit() {
        addCommand(new Command(Locale.EXIT, Command.EXIT, 0));
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command.getCommandType() == Command.EXIT) {
            TMicro.application.notifyDestroyed();
        }
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void onDestroy(boolean unconditional) {
    }


}
