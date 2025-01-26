import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegistrationPanel extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public RegistrationPanel() {
        setTitle("Register");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        usernameField = new JTextField(20);
        usernameField.setBounds(100, 20, 165, 25);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(100, 50, 165, 25);
        panel.add(passwordField);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(10, 80, 100, 25);
        panel.add(registerButton);

        JButton loginButton = new JButton("Go to Login");
        loginButton.setBounds(120, 80, 120, 25);
        panel.add(loginButton);

        // Register Button Action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        // Redirect to Login Panel
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginPanel().setVisible(true);
            }
        });
    }

    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration successful! Please log in.");
            dispose();
            new LoginPanel().setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Unable to register user.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationPanel().setVisible(true));
    }
}
