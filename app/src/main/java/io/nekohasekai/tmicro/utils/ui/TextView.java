package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Container;
import com.sun.lwuit.TextArea;
import io.nekohasekai.tmicro.Theme;

public class TextView extends TextArea {

    public TextView() {
        super();

        setFocusable(false);
        setEditable(false);

        getStyle().setFgColor(Theme.getCurrent().textColor);
        getStyle().setFont(Theme.fontSmall);
        getStyle().setBorder(null);
    }

    public TextView(String text) {
        this();

        setText(text);
    }

    public void center() {
        getStyle().setAlignment(Container.CENTER);
    }

    public void setText(String text) {
       /* String[] lines = Strings.split(text, '\n');

        }*/
        super.setText(text);
    }
}
