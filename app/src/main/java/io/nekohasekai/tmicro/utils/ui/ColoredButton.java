package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Border;
import io.nekohasekai.tmicro.Theme;

public class ColoredButton extends Container {

    public Container view;
    public Button button;

    public ColoredButton() {
        super(new BoxLayout(BoxLayout.Y_AXIS));
        addComponent(button = new Button() {{
            setAlignment(Component.CENTER);
            getUnselectedStyle().setFgColor(Theme.getCurrent().onSurface);
            getUnselectedStyle().setBgColor(Theme.getCurrent().accent);
            getUnselectedStyle().setBorder(null);
            getSelectedStyle().setFgColor(Theme.getCurrent().onSurface);
            getSelectedStyle().setBgColor(Theme.getCurrent().accent);
            getSelectedStyle().setBorder(Border.createCompoundBorder(null, null, Border.createLineBorder(5, Theme.getCurrent().accentDark), null));
            getPressedStyle().setFgColor(Theme.getCurrent().onSurface);
            getPressedStyle().setBgColor(Theme.getCurrent().accentLight);
            getPressedStyle().setBorder(null);

            addFocusListener(new FocusListener() {
                public void focusGained(Component cmp) {
                    getPressedStyle().setBorder(Border.createCompoundBorder(null, null, Border.createLineBorder(5, Theme.getCurrent().accentDark), null));
                }

                public void focusLost(Component cmp) {
                    getPressedStyle().setBorder(null);
                }
            });
        }});
    }

    public ColoredButton(String text, ActionListener listener) {
        this();
        button.setText(text);
        button.addActionListener(listener);
    }


}
