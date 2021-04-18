package io.nekohasekai.tmicro.utils.ui;

import com.sun.lwuit.Form;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.Theme;

public class BaseForm extends Form {

    public BaseForm() {
        Theme.applyTitleBar(this);
    }

    public BaseForm(String title) {
        Theme.applyTitleBar(this, title);
    }

    {
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    }

}
