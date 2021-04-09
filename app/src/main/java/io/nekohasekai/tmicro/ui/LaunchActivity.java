package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.*;
import com.sun.lwuit.layouts.BoxLayout;

import java.io.IOException;

public class LaunchActivity extends Form {

    public LaunchActivity() {
        super();

        setLayout(new BoxLayout(BoxLayout.Y_AXIS));

        Container content = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        addComponent(content);

        try {
            content.addComponent(new Label(Image.createImage("/icon.png")) {{
                getStyle().setMargin(Container.TOP, 16);
                getStyle().setMargin(Container.BOTTOM, 8);
                getStyle().setAlignment(Container.CENTER);
            }});
        } catch (IOException e) {
            e.printStackTrace();
        }

        content.addComponent(new Label("Telegram") {{
            getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
            getStyle().setMargin(Container.BOTTOM, 8);
            getStyle().setAlignment(Container.CENTER);
        }});

        content.addComponent(new TextArea("The world's fastest messaging app.\nIt is free and secure.") {{
            getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            getStyle().setAlignment(Container.CENTER);
            setFocusable(false);
            setEditable(false);
            getStyle().setBorder(null);
            getStyle().setMargin(Container.BOTTOM, 32);
        }});

        Container buttonLayout = new Container(new BoxLayout(BoxLayout.Y_AXIS) {{
            getStyle().setPadding(0, 0, 32, 32);
        }});
        content.addComponent(buttonLayout);

        Button loginButton = new Button("Start Messaging") {{
            setAlignment(Container.CENTER);
            getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            getSelectedStyle().setFgColor(0xffffff);
            getSelectedStyle().setBgColor(0x448aff);
            getSelectedStyle().setBorder(null);
        }};

        buttonLayout.addComponent(loginButton);

    }

}
