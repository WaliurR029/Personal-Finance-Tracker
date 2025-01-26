import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class DashboardPanel extends JFrame {
    private int userId;
    private JTable expensesTable;
    private JComboBox<String> filterComboBox;
    private JTextField customDaysField;
    private JLabel totalExpensesLabel;

    public DashboardPanel(int userId) {
        this.userId = userId;

        setTitle("Dashboard");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // Top Panel for Filter Controls
        JPanel topPanel = new JPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JLabel filterLabel = new JLabel("View Expenses:");
        topPanel.add(filterLabel);

        filterComboBox = new JComboBox<>(new String[]{"Last 7 Days", "Last 30 Days", "Last 90 Days", "Custom Days"});
        filterComboBox.addActionListener(e -> onFilterChange());
        topPanel.add(filterComboBox);

        customDaysField = new JTextField(5);
        customDaysField.setVisible(false); // Only show if "Custom Days" is selected
        topPanel.add(customDaysField);

        JButton applyFilterButton = new JButton("Apply");
        applyFilterButton.addActionListener(e -> onFilterChange());
        topPanel.add(applyFilterButton);

        // Add Expense Button
        JButton addExpenseButton = new JButton("Add Expense");
        addExpenseButton.addActionListener(e -> new AddExpenseDialog(userId, this).setVisible(true));
        topPanel.add(addExpenseButton);

        // Table to Display Expenses
        expensesTable = new JTable();
        mainPanel.add(new JScrollPane(expensesTable), BorderLayout.CENTER);

        // Bottom Panel for Total Expenses and Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        JLabel totalLabel = new JLabel("Total Expenses:");
        bottomPanel.add(totalLabel);

        totalExpensesLabel = new JLabel("0.00");
        bottomPanel.add(totalExpensesLabel);

        JButton editExpenseButton = new JButton("Edit Expense");
        editExpenseButton.addActionListener(e -> editSelectedExpense());
        bottomPanel.add(editExpenseButton);

        JButton deleteExpenseButton = new JButton("Delete Expense");
        deleteExpenseButton.addActionListener(e -> deleteSelectedExpense());
        bottomPanel.add(deleteExpenseButton);

        // Load default expenses (Last 30 Days)
        loadExpenses(30);
    }

    // Method called when the filter is changed
    private void onFilterChange() {
        String selectedFilter = (String) filterComboBox.getSelectedItem();
        int days = 30; // Default to 30 days

        if ("Last 7 Days".equals(selectedFilter)) {
            days = 7;
        } else if ("Last 30 Days".equals(selectedFilter)) {
            days = 30;
        } else if ("Last 90 Days".equals(selectedFilter)) {
            days = 90;
        } else if ("Custom Days".equals(selectedFilter)) {
            try {
                days = Integer.parseInt(customDaysField.getText());
                if (days <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number of days.");
                return;
            }
        }

        // Load expenses based on the selected number of days
        loadExpenses(days);
    }

    // Load expenses and calculate the total for the selected time range
    public void loadExpenses(int days) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT id, category, amount, date FROM expenses WHERE user_id = ? AND date >= CURDATE() - INTERVAL ? DAY";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, days);
            ResultSet resultSet = statement.executeQuery();

            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columns = new Vector<>();
            columns.add("ID");
            columns.add("Category");
            columns.add("Amount");
            columns.add("Date");

            double totalExpenses = 0;

            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getInt("id")); // ID for identification
                row.add(resultSet.getString("category"));
                row.add(resultSet.getDouble("amount"));
                row.add(resultSet.getDate("date"));
                data.add(row);

                totalExpenses += resultSet.getDouble("amount");
            }

            // Update the table model
            expensesTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make the table read-only
                }
            });

            // Update the total expenses label
            totalExpensesLabel.setText(String.format("%.2f", totalExpenses));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Unable to load expenses.");
        }
    }

    // Edit the selected expense
    private void editSelectedExpense() {
        int selectedRow = expensesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to edit.");
            return;
        }

        int expenseId = (int) expensesTable.getValueAt(selectedRow, 0);
        String category = (String) expensesTable.getValueAt(selectedRow, 1);
        double amount = (double) expensesTable.getValueAt(selectedRow, 2);
        String date = expensesTable.getValueAt(selectedRow, 3).toString();

        // Show edit dialog
        EditExpenseDialog dialog = new EditExpenseDialog(userId, expenseId, category, amount, date, this);
        dialog.setVisible(true);
    }

    // Delete the selected expense
    private void deleteSelectedExpense() {
        int selectedRow = expensesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
            return;
        }

        int expenseId = (int) expensesTable.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this expense?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM expenses WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, expenseId);
                statement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Expense deleted successfully.");
                loadExpenses(30); // Reload default view
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: Unable to delete expense.");
            }
        }
    }
}
