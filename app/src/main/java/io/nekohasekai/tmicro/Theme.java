package io.nekohasekai.tmicro;

import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;

public class Theme {

    public int primary;
    public int primaryDark;
    public int accent;
    public int accentDark;
    public int accentLight;
    public int textColor;
    public int onSurface;

    private static final Theme defaultTheme = new Theme() {{
        primary = 0x2196f3;
        primaryDark = 0x0069c0;
        accent = 0x448aff;
        accentDark = 0x005ecb;
        accentLight = 0x83b9ff;
        textColor = 0x000000;
        onSurface = 0xffffff;
    }};

    public static Theme getCurrent() {
        return defaultTheme;
    }

    public static void applyTitleBar(Form form) {
        applyTitleBar(form, Locale.getCurrent().appName);
    }
        public static void applyTitleBar(Form form, String title) {
        form.setTitle(title);
        form.getTitleStyle().setFgColor(0xffffff);
        form.getTitleStyle().setBgColor(0x448aff);
        form.getTitleStyle().setPadding(Container.LEFT, 8);
        form.getTitleStyle().setAlignment(Container.LEFT);

        Style def =  UIManager.getInstance().getComponentStyle("SoftButton");
        def.setBgColor(Theme.defaultTheme.primaryDark);
        UIManager.getInstance().setComponentStyle("SoftButton",def);
    }

}