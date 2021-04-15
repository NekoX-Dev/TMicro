package io.nekohasekai.tmicro.ui;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.DefaultListCellRenderer;
import com.sun.lwuit.plaf.Border;
import io.nekohasekai.tmicro.TMicro;
import io.nekohasekai.tmicro.Theme;
import io.nekohasekai.tmicro.utils.IoUtil;
import io.nekohasekai.tmicro.utils.ui.ColoredButton;
import io.nekohasekai.tmicro.utils.ui.EditText;
import io.nekohasekai.tmicro.utils.ui.TextView;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

import java.io.IOException;

public class LoginActivity extends BaseActivity {

    public void onCreate() {
        super.onCreate();
        setContentForm(new InputPhoneForm());
    }

    public class InputPhoneForm extends Form {

        public InputPhoneForm() {
            super();

            Theme.applyTitleBar(this);
            setLayout(new BoxLayout(BoxLayout.Y_AXIS));

            addCommand(new Command("Exit", 0));
            addCommandListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getCommand().getId() == 0) {
                        TMicro.application.notifyDestroyed();
                    }
                }
            });

            addComponent(new Container(new BoxLayout(BoxLayout.Y_AXIS)) {

                public ComboBox country;
                public EditText phoneNumber;
                public ColoredButton continueButton;

                {
                    getStyle().setPadding(8, 8, 8, 8);

                    addComponent(new TextView("Input your phone to continue.") {{
                        getStyle().setMargin(Container.BOTTOM, 4);
                    }});

                    addComponent(country = new ComboBox() {

                        public final Command select = new Command("Select", 11);
                        public final ActionListener listener = new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if (evt.getCommand().equals(select)) {
                                    fireClicked();
                                }
                            }
                        };

                        {
                            addFocusListener(new FocusListener() {
                                public void focusGained(Component cmp) {
                                    Form form = cmp.getComponentForm();
                                    if (form == null) return;
                                    MenuBar menu = form.getMenuBar();
                                    if (menu == null) return;
                                    menu.addCommand(select);
                                    form.addCommandListener(listener);
                                }

                                public void focusLost(Component cmp) {
                                    Form form = cmp.getComponentForm();
                                    if (form == null) return;
                                    MenuBar menu = form.getMenuBar();
                                    if (menu == null) return;
                                    menu.removeCommand(select);
                                    form.removeCommandListener(listener);
                                }
                            });

                            setHintIcon(null);
                            addItem("Choose Country");
                            try {
                                String[] items = Strings.split(IoUtil.readResUTF8("countries.txt"), '\n');
                                items = Arrays.reverse(items);
                                for (int i = 0; i < items.length; i++) {
                                    String[] item = Strings.split(items[i], ';');
                                    String name = "+ " + item[0] + " (" + item[2] + ")";
                                    addItem(name);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ((DefaultListCellRenderer) getRenderer()).getListFocusComponent(this).getSelectedStyle().setBorder(Border.createCompoundBorder(null, null, null, Border.createLineBorder(2, Theme.getCurrent().accent)));
                        }
                    });

                    addComponent(phoneNumber = new EditText(TextArea.PHONENUMBER) {{
                        setHint("Phone number");
                        setInputMode("123");
                    }});

                    addComponent(continueButton = new ColoredButton("Continue", new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (country.getSelectedIndex() == 0) {
                                country.fireClicked();
                                return;
                            }
                            if (phoneNumber.getText().length() == 0) {
                                phoneNumber.requestFocus();
                                return;
                            }
                        }
                    }, true) {{
                        getStyle().setMargin(Container.TOP, 8);
                    }});

                }
            });


        }
    }

}
