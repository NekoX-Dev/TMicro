package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.Container;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import io.nekohasekai.tmicro.messenger.ConnectionsManager;
import io.nekohasekai.tmicro.utils.ui.*;

import java.io.IOException;

public class RelayActivity extends BaseActivity {

    private final BaseActivity showBack;

    public RelayActivity(BaseActivity showBack) {
        this.showBack = showBack;
    }

    public void onCreate() {
        setContentForm(new RelayForm());
    }

    public class RelayForm extends BaseForm {

        public RelayForm() {
            super("Relay Settings");

            addComponent(new Container(new BoxLayout(BoxLayout.Y_AXIS)) {{
                getStyle().setPadding(8, 8, 8, 8);

                addComponent(new TextView(
                        "Input relay address and port:\n" +
                                "(ignore port if is 80)\n" +
                                "(empty to disable)\n" +
                                "e.g. 127.0.0.1:8080"
                ));

                final EditText address;

                addComponent(address = new EditText(1, TextArea.ANY) {{
                    setHint("Relay address");
                    setMargin(8, 8, 0, 0);
                    setText(getConnectionsManager().relayAddress);
                }});

                addComponent(new ColoredButton("Save and reconnect", new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        String relay = address.getText();

                        int portIndex = relay.lastIndexOf(':');
                        if (portIndex != -1) {
                            String port = relay.substring(portIndex + 1);
                            try {
                                Integer.parseInt(port);
                            } catch (NumberFormatException invalidNumber) {
                                new AlertDialog("Error", "Invalid port: " + port).show();
                                address.requestFocus();
                                return;
                            }
                        }

                        ConnectionsManager.getInstance().relayAddress = address.getText();

                        try {
                            ConnectionsManager.getInstance().writeConfig();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        performActivity(showBack);

                    }
                }));

                addComponent(new ColoredButton("Cancel", new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        performActivity(showBack);
                    }
                }));

            }});


        }
    }


}
