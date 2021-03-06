package main.com.allan.amca.gui.register;

import main.com.allan.amca.gui.Frameable;
import main.com.allan.amca.gui.Screen;
import main.com.allan.amca.gui.menu.MainMenuView;
import main.com.allan.amca.user.Client;
import main.com.allan.amca.register.RegisterViewModel;


import javax.swing.*;
import java.awt.*;

public class RegisterView extends Screen implements Frameable {
    private JLabel              registerHeaderLabel;
    private JLabel              firstNameLabel;
    private JLabel              lastNameLabel;
    private JLabel              pinLabel;
    private JTextField          firstNameTextField;
    private JTextField          lastNameTextField;
    private JPasswordField      pinField;
    private JButton             confirmRegBtn;
    private JButton             backBtn;
    private JPanel              registerPanel;
    private GridBagConstraints  constraints;
    private final JPanel        parentPane;
    private final CardLayout    parentCardLayout;
    private final RegisterResources r = new RegisterResources();

    {
        r.getPropertyValues();
    }

    public RegisterView(final CardLayout layout, final JPanel pane) {
        this.parentCardLayout   = layout;
        this.parentPane         = pane;
    }

    @Override
    protected void updateUI() {
        // RegisterViewModel label
        constraints.gridx = 1;
        constraints.gridy = 0;
        registerPanel.add(registerHeaderLabel, constraints);

        // First name label
        constraints.gridx = 0;
        constraints.gridy = 1;
        registerPanel.add(firstNameLabel, constraints);

        // First name text field
        constraints.gridx = 1;
        constraints.gridy = 1;
        registerPanel.add(firstNameTextField, constraints);

        // Last name label
        constraints.gridx = 0;
        constraints.gridy = 2;
        registerPanel.add(lastNameLabel, constraints);

        // Last name text field
        constraints.gridx = 1;
        constraints.gridy = 2;
        registerPanel.add(lastNameTextField, constraints);

        // PIN label
        constraints.gridx = 0;
        constraints.gridy = 3;
        registerPanel.add(pinLabel, constraints);

        // PIN field
        constraints.gridx = 1;
        constraints.gridy = 3;
        registerPanel.add(pinField, constraints);

        // Confirm Reg Btn
        constraints.gridy = 4;
        registerPanel.add(confirmRegBtn, constraints);

        constraints.gridy = 5;
        registerPanel.add(backBtn, constraints);

        parentPane.add(registerPanel, r.REGISTER_PANEL());
        parentCardLayout.show(parentPane, r.REGISTER_PANEL());
        frame.repaint();
    }
    
    @Override
    public void addListeners() {
        confirmRegBtn.addActionListener( evt -> {
            final String firstName = firstNameTextField.getText();
            final String lastName  = lastNameTextField.getText();
            final String pin       = String.valueOf(pinField.getPassword());
            final RegisterViewModel registerViewModel = RegisterViewModel.newInstance();
            final boolean registerSuccessful;

            try {

                registerSuccessful = registerViewModel.register(firstName, lastName, pin);

                if (registerSuccessful) {
                    final Client newClient = Client.getClient(1);
                    JOptionPane.showMessageDialog(frame,
                            String.format(r.REGISTER_SUCCESS_MSG(), newClient.getClientID()),
                            r.REGISTER_SUCCESS_TITLE(), JOptionPane.INFORMATION_MESSAGE);
                    final MainMenuView menuUI = new MainMenuView(parentCardLayout, parentPane);
                    menuUI.createUI();
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame,
                        r.REGISTER_ERROR_MSG(),
                        r.REGISTER_ERROR_TITLE(), JOptionPane.WARNING_MESSAGE);
            }
        });

        backBtn.addActionListener(evt -> parentCardLayout.show(parentPane, resource.LOGIN_PANEL()));
    }

    @Override
    public void initComponents() {
        constraints         = new GridBagConstraints();
        registerHeaderLabel = new JLabel(r.REGISTER_HEADER_TXT());
        firstNameLabel      = new JLabel(r.REGISTER_FIRSTNAME_LABEL());
        lastNameLabel       = new JLabel(r.REGISTER_LASTNAME_LABEL());
        pinLabel            = new JLabel(r.REGISTER_PIN_LABEL());
        firstNameTextField  = new JTextField(10);
        lastNameTextField   = new JTextField(10);
        pinField            = new JPasswordField(10);
        confirmRegBtn       = new JButton(r.REGISTER_REG_BTN());
        backBtn             = new JButton(r.REGISTER_BACK_BTN());
        registerPanel       = new JPanel(new GridBagLayout());
    }
}