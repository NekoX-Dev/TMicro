package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;

public class EditText extends TextField implements ActionListener {

    public Command backSpace = new Command("Back", 10);

    public EditText() {
        super();
    }

    public EditText(int constraint) {
        super(constraint);
    }

    public EditText(int columns, int constraint) {
        super(columns, constraint);
    }

    {
        getUnselectedStyle().setPadding(Container.LEFT, 4);
        getSelectedStyle().setPadding(Container.LEFT, 4);

        setUseNativeTextInput(true);
        setUseSoftkeys(false);

        addFocusListener(new FocusListener() {
            public void focusGained(Component cmp) {
                Form form = cmp.getComponentForm();
                if (form == null) return;
                MenuBar menu = form.getMenuBar();
                if (menu == null) return;
                menu.addCommand(backSpace);
                form.addCommandListener(EditText.this);
            }

            public void focusLost(Component cmp) {
                Form form = cmp.getComponentForm();
                if (form == null) return;
                MenuBar menu = form.getMenuBar();
                if (menu == null) return;
                menu.removeCommand(backSpace);
                form.removeCommandListener(EditText.this);
            }
        });
    }


    public void actionPerformed(final ActionEvent evt) {
        if (evt.getCommand().getId() == backSpace.getId()) {
            deleteChar();
        }
    }
}
