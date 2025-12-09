
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDeductionPanel extends JPanel {

    // Holds the active DB connection passed from the main dashboard
    private Connection connection;

    // Table displaying all deductions
    private JTable table;

    public AdminDeductionPanel(Connection connection) {
        super(new BorderLayout(12, 12));

        // Store reference to database connection
        this.connection = connection;

        // Set panel background and padding
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // BUTTON BAR SETUP
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        buttonPanel.setOpaque(false);  // blend with dark theme

        // Create action buttons with consistent styling
        JButton addBtn = createModernButton("Add Deduction");
        JButton editBtn = createModernButton("Edit Deduction");
        JButton deleteBtn = createModernButton("Delete Deduction");
        JButton refreshBtn = createModernButton("Refresh");

        // Bind button actions to their respective handlers
        refreshBtn.addActionListener(e -> loadDeductionData());
        addBtn.addActionListener(e -> showAddDeductionDialog());
        editBtn.addActionListener(e -> showEditDeductionDialog());
        deleteBtn.addActionListener(e -> deleteSelectedDeduction());

        // Add buttons to the top bar
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        // TABLE CONFIGURATION
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);   // auto fit columns
        table.setFillsViewportHeight(true);                        // fill empty space
        table.setRowHeight(26);                                    // uniform row height
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Apply dark theme colors
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(60, 63, 65));
        table.getTableHeader().setForeground(Color.WHITE);

        // Load data immediately on panel creation
        loadDeductionData();

        // Add table into a scroll pane for smooth viewing
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setPreferredSize(new Dimension(600, 250));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add UI components to layout
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadDeductionData() {
        // Loads all deductions from the DB and updates the table model
        try {
            String query = "SELECT DeductionID, Description, Default_Amount FROM DEDUCTION ORDER BY DeductionID";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Setup column names for the table
            Vector<String> columnNames = new Vector<>();
            columnNames.add("DeductionID");
            columnNames.add("Description");
            columnNames.add("Default_Amount");

            // Store each database row into a vector
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("DeductionID"));
                row.add(rs.getString("Description"));
                row.add(rs.getBigDecimal("Default_Amount"));
                data.add(row);
            }

            // Create a non-editable table model
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;   // prevent cell editing
                }
            };

            // Apply the model to the table
            table.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading deduction data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDeductionDialog() {
        // Input fields for adding a new deduction
        JTextField descriptionField = new JTextField();
        JTextField amountField = new JTextField();

        // Build dialog panel containing form inputs
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(new Color(30, 30, 30));
        panel.add(createLabel("Description:"));
        panel.add(descriptionField);
        panel.add(createLabel("Default Amount:"));
        panel.add(amountField);

        // Show dialog and capture user choice
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Deduction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If user confirmed
        if (result == JOptionPane.OK_OPTION) {

            // Read field values
            String description = descriptionField.getText().trim();
            String amountStr = amountField.getText().trim();

            // Validate required fields
            if (description.isEmpty() || amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            // Validate numeric amount
            double defaultAmount;
            try {
                defaultAmount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Default Amount must be a number.");
                return;
            }

            // Insert new deduction into the database
            try {
                String insert = "INSERT INTO DEDUCTION (Description, Default_Amount) VALUES (?, ?)";
                PreparedStatement stmt = connection.prepareStatement(insert);
                stmt.setString(1, description);
                stmt.setDouble(2, defaultAmount);
                stmt.executeUpdate();

                // Reload table to show new entry
                loadDeductionData();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error adding deduction: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDeductionDialog() {
        // Ensures a row is selected before editing
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a deduction to edit.");
            return;
        }

        // Retrieve selected row values
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object id = model.getValueAt(selectedRow, 0);
        String currentDescription = model.getValueAt(selectedRow, 1).toString();
        Object currentAmount = model.getValueAt(selectedRow, 2);

        // Build input fields pre-filled with current values
        JTextField descriptionField = new JTextField(currentDescription);
        JTextField amountField = new JTextField(currentAmount.toString());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(new Color(30, 30, 30));
        panel.add(createLabel("Description:"));
        panel.add(descriptionField);
        panel.add(createLabel("Default Amount:"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Deduction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If user confirmed edit
        if (result == JOptionPane.OK_OPTION) {

            // Read and validate new values
            String newDescription = descriptionField.getText().trim();
            String amountStr = amountField.getText().trim();

            if (newDescription.isEmpty() || amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            double defaultAmount;
            try {
                defaultAmount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Default Amount must be a number.");
                return;
            }

            // Update row in the database
            try {
                String update = "UPDATE DEDUCTION SET Description = ?, Default_Amount = ? WHERE DeductionID = ?";
                PreparedStatement stmt = connection.prepareStatement(update);
                stmt.setString(1, newDescription);
                stmt.setDouble(2, defaultAmount);
                stmt.setObject(3, id);
                stmt.executeUpdate();

                // Refresh table
                loadDeductionData();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error editing deduction: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedDeduction() {
        // Check if a row is selected for deletion
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a deduction to delete.");
            return;
        }

        // Confirm with user
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected deduction?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Get deduction ID from table
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object id = model.getValueAt(selectedRow, 0);

        // Run delete query
        try {
            String delete = "DELETE FROM DEDUCTION WHERE DeductionID = ?";
            PreparedStatement stmt = connection.prepareStatement(delete);
            stmt.setObject(1, id);
            stmt.executeUpdate();

            // Refresh and notify user
            loadDeductionData();
            JOptionPane.showMessageDialog(this, "Deduction deleted.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error deleting deduction: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Utility: Creates white label for dark mode
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // Utility: Creates a modern dark themed button
    private JButton createModernButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(60, 63, 65));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));

        // Add padding and border
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Hover color effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(85, 90, 92));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(60, 63, 65));
            }
        });

        return b;
    }
}
