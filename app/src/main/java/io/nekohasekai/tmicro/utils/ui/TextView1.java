package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.Theme;
import org.bouncycastle.util.Strings;

public class TextView1 extends Container {

    public TextView1() {
        super(new BoxLayout(BoxLayout.Y_AXIS));
    }

    public TextView1(String text) {
        this();
        setText(text);
    }

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        removeAll();
        this.text = text;
        String[] lines = Strings.split(text, '\n');
        for (int i = 0; i < lines.length; ) {
            String line = lines[i];
            if (line.length() > 25) {
                String[] newLines = new String[lines.length + 1];
                if (i > 0) {
                    System.arraycopy(lines, 0, newLines, 0, i);
                }
                newLines[i] = line.substring(0, 25);
                newLines[i + 1] = line.substring(25);
                if (lines.length > i + 1) {
                    System.arraycopy(lines, i + 1, newLines, i + 2, lines.length - i - 1);
                }
                lines = newLines;
                continue;
            }
            i++;
        }
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Label label = new Label(line) {{
                getStyle().setFgColor(Theme.getCurrent().textColor);
                getStyle().setFont(Theme.fontSmall);
            }};
            addComponent(label);
        }
        if (getComponentForm() != null) {
            getComponentForm().repaint();
        }
    }

    public void center() {
        int n = getComponentCount();
        for (int i = 0; i < n; i++) {
            Label label = (Label) getComponentAt(i);
            label.getStyle().setAlignment(Container.CENTER);
        }
    }

}
