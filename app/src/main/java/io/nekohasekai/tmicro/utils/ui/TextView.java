package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.sun.lwuit.html.HTMLParser;
import io.nekohasekai.tmicro.Theme;
import io.nekohasekai.tmicro.utils.HtmlUtil;
import org.bouncycastle.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class TextView extends TextArea {

    public TextView() {

        setFocusable(false);
        setEditable(false);

        getStyle().setFgColor(Theme.getCurrent().textColor);
        getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));

        getStyle().setBorder(null);
    }

    public TextView(String text) {
        this();
        setText(text);
    }

    public void center() {
        getStyle().setAlignment(Container.CENTER);
    }

}
