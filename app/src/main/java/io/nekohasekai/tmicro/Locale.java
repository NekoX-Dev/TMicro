package io.nekohasekai.tmicro;

public class Locale {

    public String appName;
    public String exit;

    public static class English extends Locale {
        {
            appName = "TMicro";
            exit = "Exit";
        }
    }

    private static Locale instance;
    public static Locale getCurrent() {
        if (instance == null) {
            instance = new English();
        }
        return instance;
    }

}
