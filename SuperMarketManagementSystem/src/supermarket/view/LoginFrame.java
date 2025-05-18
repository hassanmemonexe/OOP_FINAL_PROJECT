package supermarket.view; // Moved to view package

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import supermarket.functionality.User; // Import User from functionality package
// AdminFrame and SellerFrame are in the same 'view' package, so direct import not strictly needed
// but can be explicit:
// import supermarket.view.AdminFrame;
// import supermarket.view.SellerFrame;


/**
 * The login window for the Supermarket Management System.
 * Handles user authentication by reading from users.txt.
 * Updated for new package structure.
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;

    private static final String USERS_FILE_PATH = "users.txt"; // Path relative to execution directory

    public LoginFrame() {
        setTitle("Supermarket Login");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        createDefaultAdminIfNotExists();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        add(userLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(loginButton);

        exitButton = new JButton("Exit");
        exitButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(exitButton);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(buttonPanel, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                authenticateAndProceed(username, password);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void createDefaultAdminIfNotExists() {
        File usersFile = new File(USERS_FILE_PATH);
        if (!usersFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(usersFile))) {
                User defaultAdmin = new User("admin", "password123", "admin");
                writer.println(defaultAdmin.toCSVString());
                System.out.println(USERS_FILE_PATH + " not found. Created with default admin (admin/password123).");
            } catch (IOException ex) {
                System.err.println("Error creating default " + USERS_FILE_PATH + ": " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Critical Error: Could not create user data file.",
                        "File Creation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void authenticateAndProceed(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User authenticatedUser = null;
        File usersFile = new File(USERS_FILE_PATH);

        if (!usersFile.exists()) {
            JOptionPane.showMessageDialog(this, "User data file not found. Please restart or contact support.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromCSVString(line); // User class is now in functionality package
                if (user != null && user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    authenticatedUser = user;
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "User data file not found unexpectedly.", "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading user data: " + ex.getMessage(), "File Read Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (authenticatedUser != null) {
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + authenticatedUser.getUsername() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            if ("admin".equalsIgnoreCase(authenticatedUser.getRole())) {
                new AdminFrame(authenticatedUser).setVisible(true); // AdminFrame is in the same view package
            } else if ("seller".equalsIgnoreCase(authenticatedUser.getRole())) {
                new SellerFrame(authenticatedUser).setVisible(true); // SellerFrame is in the same view package
            } else {
                JOptionPane.showMessageDialog(this, "Unknown user role: " + authenticatedUser.getRole(), "Role Error", JOptionPane.ERROR_MESSAGE);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
