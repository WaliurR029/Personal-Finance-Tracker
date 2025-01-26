import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditExpenseDialog extends JDialog {
    private int userId;
    private int expenseId;
    private JTextField categoryField;
    private JTextField amountField;
    private JTextField dateField;
    private DashboardPanel dashboard;

    public EditExpenseDialog(int userId, int expenseId, String category, double amount, String date, DashboardPanel dashboard) {
        this.userId = userId;
        this.expenseId = expenseId;
        this.dashboard = dashboard;

        setTitle("Edit Expense");
        setSize(300, 200);
        setLayout(new GridLayout(5, 2));

        add(new JLabel("Category:"));
        categoryField = new JTextField(category);
        add(categoryField);

        add(new JLabel("Amount:"));
        amountField = new JTextField(String.valueOf(amount));
        add(amountField);

        add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField(date);
        add(dateField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateExpense();
            }
        });
        add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(null);
    }

    private void updateExpense() {
        String category = categoryField.getText();
        String amountText = amountField.getText();
        String date = dateField.getText();

        // Validate inputs
        if (category.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE expenses SET category = ?, amount = ?, date = ? WHERE id = ? AND user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, category);
            statement.setDouble(2, amount);
            statement.setString(3, date);
            statement.setInt(4, expenseId);
            statement.setInt(5, userId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Expense updated successfully.");
                dashboard.loadExpenses(30); // Reload the expenses
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update expense.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Unable to update expense.");
        }
    }
}
