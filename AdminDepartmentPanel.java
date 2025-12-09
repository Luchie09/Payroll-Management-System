
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDepartmentPanel extends JPanel {

    // Holds shared DB connection from the main dashboard
    private Connection connection;

    // Table used to display department records
    private JTable table;

    public AdminDepartmentPanel(Connection connection) {
        super(new BorderLayout(12, 12));

        // Store DB connection for all operations
        this.connection = connection;

        // Panel styling
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // BUTTON BAR SETUP
        // Holds Add, Edit, Delete, and Refresh buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        buttonPanel.setOpaque(false);

        // Create styled action buttons
        JButton addBtn = createModernButton("Add Department");
        JButton editBtn = createModernButton("Edit Department");
        JButton deleteBtn = createModernButton("Delete Department");
        JButton refreshBtn = createModernButton("Refresh");

        // Connect button actions to methods
        refreshBtn.addActionListener(e -> loadDepartmentData());
        addBtn.addActionListener(e -> showAddDepartmentDialog());
        editBtn.addActionListener(e -> showEditDepartmentDialog());
        deleteBtn.addActionListener(e -> deleteSelectedDepartment());

        // Add buttons to the layout
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        // TABLE SETUP
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Apply dark theme styling to table
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(60, 63, 65));
        table.getTableHeader().setForeground(Color.WHITE);

        // Load data from the database into the table model
        loadDepartmentData();

        // Scroll container for the table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setPreferredSize(new Dimension(400, 250));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add components to panel layout
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Loads department data from DB and updates table model
    private void loadDepartmentData() {
        try {
            // Simple select query for department list
            String query = "SELECT DepartmentID, DepartmentName FROM DEPARTMENT";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Define table column headers
            Vector<String> columnNames = new Vector<>();
            columnNames.add("DepartmentID");
            columnNames.add("DepartmentName");

            // Collect table data row by row
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("DepartmentID"));
                row.add(rs.getString("DepartmentName"));
                data.add(row);
            }

            // Create non-editable table model
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Prevents direct table editing
                }
            };

            // Apply model to the table
            table.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading department data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Shows a form dialog for adding a new department
    private void showAddDepartmentDialog() {
        JTextField nameField = new JTextField();

        // Layout for input form
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(new Color(30, 30, 30));
        panel.add(createLabel("Department Name:"));
        panel.add(nameField);

        // Show dialog with OK and Cancel buttons
        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add Department",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        // If user selects OK
        if (result == JOptionPane.OK_OPTION) {

            String name = nameField.getText().trim();

            // Validate input
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Department Name cannot be empty.");
                return;
            }

            // Insert the new department into DB
            try {
                String insert = "INSERT INTO DEPARTMENT (DepartmentName) VALUES (?)";
                PreparedStatement stmt = connection.prepareStatement(insert);
                stmt.setString(1, name);
                stmt.executeUpdate();

                // Reload table data
                loadDepartmentData();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error adding department: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Shows a form dialog for editing the selected department
    private void showEditDepartmentDialog() {

        // Check if a row is selected
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a department to edit.");
            return;
        }

        // Retrieve selected row values
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object id = model.getValueAt(selectedRow, 0);
        String currentName = (String) model.getValueAt(selectedRow, 1);

        JTextField nameField = new JTextField(currentName);

        // Create form panel
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(new Color(30, 30, 30));
        panel.add(createLabel("Department Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Edit Department",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        // If user selects OK
        if (result == JOptionPane.OK_OPTION) {

            String newName = nameField.getText().trim();

            // Validate new name
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Department Name cannot be empty.");
                return;
            }

            // Perform update query
            try {
                String update = "UPDATE DEPARTMENT SET DepartmentName = ? WHERE DepartmentID = ?";
                PreparedStatement stmt = connection.prepareStatement(update);
                stmt.setString(1, newName);
                stmt.setObject(2, id);
                stmt.executeUpdate();

                // Reload updated table data
                loadDepartmentData();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error editing department: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Deletes the department selected in the table
    private void deleteSelectedDepartment() {

        // Ensure a row is selected
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a department to delete.");
            return;
        }

        // Confirmation dialog to prevent accidental deletion
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the selected department?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Get ID of selected department
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object id = model.getValueAt(selectedRow, 0);

        // Execute delete operation
        try {
            String delete = "DELETE FROM DEPARTMENT WHERE DepartmentID = ?";
            PreparedStatement stmt = connection.prepareStatement(delete);
            stmt.setObject(1, id);
            stmt.executeUpdate();

            loadDepartmentData(); // Refresh table
            JOptionPane.showMessageDialog(this, "Department deleted.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error deleting department: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Creates a styled label for dark-themed dialogs
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // Creates a modern dark-themed button with hover effects
    private JButton createModernButton(String text) {
        JButton b = new JButton(text);

        // Basic button styling
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(new Color(60, 63, 65));
        b.setForeground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Font and padding styling
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Hover color behavior
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
