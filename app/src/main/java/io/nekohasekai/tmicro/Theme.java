package io.nekohasekai.tmicro;

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

}