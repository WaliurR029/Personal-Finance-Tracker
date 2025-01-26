import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddExpenseDialog extends JDialog {
    private JTextField categoryField;
    private JTextField amountField;
    private DashboardPanel dashboardPanel;
    private int userId;

    public AddExpenseDialog(int userId, DashboardPanel dashboardPanel) {
        this.userId = userId;
        this.dashboardPanel = dashboardPanel;

        setTitle("Add Expense");
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));

        JLabel categoryLabel = new JLabel("Category:");
        categoryField = new JTextField();
        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addExpense());

        add(categoryLabel);
        add(categoryField);
        add(amountLabel);
        add(amountField);
        add(new JLabel()); // Empty label for spacing
        add(addButton);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void addExpense() {
        String category = categoryField.getText();
        String amountText = amountField.getText();

        if (category.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO expenses (user_id, category, amount, date) VALUES (?, ?, ?, CURDATE())";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, userId);
                statement.setString(2, category);
                statement.setDouble(3, amount);
                statement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Expense added successfully!");

                // Refresh the dashboard panel's table
                dashboardPanel.loadExpenses(30);

                dispose(); // Close the dialog
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a numeric value.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Unable to add expense.");
        }
    }
}
