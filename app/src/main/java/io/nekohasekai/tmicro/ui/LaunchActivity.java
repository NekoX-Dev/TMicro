package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.Button;
import com.sun.lwuit.Form;
import com.sun.lwuit.layouts.BorderLayout;

public class LaunchActivity extends Form {

    public LaunchActivity() {
        super();

        setLayout(new BorderLayout());

//        Container rootLayout = new Container(new BoxLayout(BoxLayout.Y_AXIS));
//
//        rootLayout.addComponent(new Label("The world's fastest messaging app."));
//        rootLayout.addComponent(new Label("It is free and secure."));
//
//        rootLayout.addComponent( {{
//            getStyle().setMargin(20, 0, 0, 0);
//        }});

        addComponent(BorderLayout.CENTER, new Button("Start Messaging"));

    }

}
