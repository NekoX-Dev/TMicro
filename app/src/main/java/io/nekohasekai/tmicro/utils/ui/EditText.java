package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;

public class EditText extends TextField implements ActionListener {

    public Command backSpace = new Command("Back", 10);
    public Command input = new Command("Input", 11);

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
                if (getConstraint() == TextArea.ANY) {
                    menu.addCommand(input);
                }
                form.addCommandListener(EditText.this);
            }

            public void focusLost(Component cmp) {
                Form form = cmp.getComponentForm();
                if (form == null) return;
                MenuBar menu = form.getMenuBar();
                if (menu == null) return;
                menu.removeCommand(backSpace);
                if (getConstraint() == TextArea.ANY) {
                    menu.removeCommand(input);
                }
                form.removeCommandListener(EditText.this);
            }
        });
    }

    public void actionPerformed(final ActionEvent evt) {
        switch (evt.getCommand().getId()) {
            case 10: {
                deleteChar();
            }
            break;
            case 11: {
                evt.consume();
                editString();
            }
        }
    }

    public void setMargin(int top, int bottom, int left, int right) {
        getUnselectedStyle().setMargin(top, bottom, left, right);
        getSelectedStyle().setMargin(top, bottom, left, right);
    }

    public void setPadding(int top, int bottom, int left, int right) {
        getUnselectedStyle().setPadding(top, bottom, left, right);
        getSelectedStyle().setPadding(top, bottom, left, right);
    }

}
