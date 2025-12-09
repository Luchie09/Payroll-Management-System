
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminPositionPanel extends JPanel {

    // Holds the shared database connection passed from outside
    private final Connection connection;

    // Displays all position records
    private JTable table;

    public AdminPositionPanel(Connection connection) {
        // Uses BorderLayout with spacing for a cleaner layout
        super(new BorderLayout(12, 12));
        this.connection = connection;

        // Panel styling for dark theme
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Builds the top bar of buttons
        JPanel buttonPanel = buildButtonBar();

        // Creates and styles the table
        buildTable();

        // Scroll container for table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(500, 250));

        // Add components to main layout
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Loads initial data from database
        loadPositionData();
    }

    // Builds the top bar containing CRUD buttons
    private JPanel buildButtonBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        panel.setOpaque(false);

        JButton addBtn = createModernButton("Add Position");
        JButton editBtn = createModernButton("Edit Position");
        JButton deleteBtn = createModernButton("Delete Position");
        JButton refreshBtn = createModernButton("Refresh");

        // Button actions call the respective handler methods
        refreshBtn.addActionListener(e -> loadPositionData());
        addBtn.addActionListener(e -> showAddPositionDialog());
        editBtn.addActionListener(e -> showEditPositionDialog());
        deleteBtn.addActionListener(e -> deleteSelectedPosition());

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        panel.add(refreshBtn);

        return panel;
    }

    // Configures the JTable appearance and behavior
    private void buildTable() {
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);

        // Dark theme colors
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(60, 63, 65));
        table.getTableHeader().setForeground(Color.WHITE);
    }

    // Loads all position data and updates the table model
    private void loadPositionData() {
        String query
                = "SELECT p.PositionID, p.PositionName, p.BaseSalary, d.DepartmentName "
                + "FROM POSITION p LEFT JOIN DEPARTMENT d ON p.DepartmentID = d.DepartmentID";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            // Extract column names dynamically
            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();
            Vector<String> colNames = new Vector<>();
            for (int i = 1; i <= columns; i++) {
                colNames.add(meta.getColumnName(i));
            }

            // Store result rows
            Vector<Vector<Object>> rows = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columns; i++) {
                    row.add(rs.getObject(i));
                }
                rows.add(row);
            }

            // Table model that prevents editing
            table.setModel(new DefaultTableModel(rows, colNames) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading position data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Shows dialog for creating a new position
    private void showAddPositionDialog() {
        JTextField nameField = new JTextField();
        JTextField salaryField = new JTextField();

        // Dropdown of departments
        JComboBox<String> deptCombo = loadDepartmentList();
        if (deptCombo == null) {
            return; // Stops if DB error occurred
        }
        // Build dialog layout
        JPanel panel = buildFormPanel(
                "Position Name:", nameField,
                "Base Salary:", salaryField,
                "Department:", deptCombo
        );

        // Show confirmation dialog
        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add Position", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Validate input fields
        String name = nameField.getText().trim();
        String salaryText = salaryField.getText().trim();
        if (name.isEmpty() || salaryText.isEmpty() || deptCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Base Salary must be numeric.");
            return;
        }

        // Extract department ID from combo entry
        int deptId = Integer.parseInt(deptCombo.getSelectedItem().toString().split(" - ")[0]);

        // Insert new record
        try (PreparedStatement stmt
                = connection.prepareStatement("INSERT INTO POSITION (PositionName, BaseSalary, DepartmentID) VALUES (?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setDouble(2, salary);
            stmt.setInt(3, deptId);
            stmt.executeUpdate();

            loadPositionData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error adding position: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Shows dialog for editing the selected position
    private void showEditPositionDialog() {
        int row = table.getSelectedRow();

        // Ensure a row is selected
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a position to edit.");
            return;
        }

        // Current values from table
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object id = model.getValueAt(row, 0);
        String currentName = model.getValueAt(row, 1).toString();
        String currentSalary = model.getValueAt(row, 2).toString();
        String currentDept = model.getValueAt(row, 3).toString();

        JTextField nameField = new JTextField(currentName);
        JTextField salaryField = new JTextField(currentSalary);

        // Load department list into combo box
        JComboBox<String> deptCombo = loadDepartmentList();
        if (deptCombo == null) {
            return;
        }

        // Set current department as selected
        for (int i = 0; i < deptCombo.getItemCount(); i++) {
            if (deptCombo.getItemAt(i).contains(currentDept)) {
                deptCombo.setSelectedIndex(i);
                break;
            }
        }

        // Form panel
        JPanel panel = buildFormPanel(
                "Position Name:", nameField,
                "Base Salary:", salaryField,
                "Department:", deptCombo
        );

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Edit Position", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Validate and parse values
        String newName = nameField.getText().trim();
        String salaryText = salaryField.getText().trim();
        if (newName.isEmpty() || salaryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Base Salary must be numeric.");
            return;
        }

        int deptId = Integer.parseInt(deptCombo.getSelectedItem().toString().split(" - ")[0]);

        // Run update SQL
        try (PreparedStatement stmt
                = connection.prepareStatement(
                        "UPDATE POSITION SET PositionName=?, BaseSalary=?, DepartmentID=? WHERE PositionID=?")) {

            stmt.setString(1, newName);
            stmt.setDouble(2, salary);
            stmt.setInt(3, deptId);
            stmt.setObject(4, id);
            stmt.executeUpdate();

            loadPositionData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error editing position: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Deletes the currently selected position
    private void deleteSelectedPosition() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a position to delete.");
            return;
        }

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to delete the selected position?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Object id = table.getModel().getValueAt(row, 0);

        // Execute delete query
        try (PreparedStatement stmt
                = connection.prepareStatement("DELETE FROM POSITION WHERE PositionID=?")) {

            stmt.setObject(1, id);
            stmt.executeUpdate();

            loadPositionData();
            JOptionPane.showMessageDialog(this, "Position deleted.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error deleting position: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Loads department entries and returns a combo box filled with them
    private JComboBox<String> loadDepartmentList() {
        JComboBox<String> combo = new JComboBox<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT DepartmentID, DepartmentName FROM DEPARTMENT")) {

            while (rs.next()) {
                combo.addItem(rs.getInt("DepartmentID") + " - " + rs.getString("DepartmentName"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading departments: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return combo;
    }

    // Utility to create consistent form fields layout
    private JPanel buildFormPanel(Object... components) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(new Color(30, 30, 30));

        for (Object comp : components) {
            if (comp instanceof String) {
                panel.add(createLabel((String) comp)); 
            }else if (comp instanceof JComponent) {
                panel.add((JComponent) comp);
            }
        }
        return panel;
    }

    // Creates a styled JLabel for forms
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // Creates a modern dark-themed button with hover effects
    private JButton createModernButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(60, 63, 65));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(85, 90, 92));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(60, 63, 65));
            }
        });

        return b;
    }

    // Accent button variant (unused but kept for flexibility)
    private JButton createAccentButton(String text) {
        JButton b = createModernButton(text);
        b.setBackground(new Color(200, 120, 0));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(220, 140, 20));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(200, 120, 0));
            }
        });

        return b;
    }
}
