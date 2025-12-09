
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminEmployeePanel extends JPanel {

    private final Connection connection;      // Active database connection
    private final JTable table;               // Main employee table
    private JTextField searchField;           // Search input field

    // Constructor: Initializes panel layout, components, and loads employee data
    public AdminEmployeePanel(Connection connection) {
        super(new BorderLayout(12, 12));
        this.connection = connection;

        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(createSearchPanel(), BorderLayout.NORTH);     // Add search bar
        add(createTopButtonBar(), BorderLayout.BEFORE_FIRST_LINE); // Add action buttons

        table = createEmployeeTable();                    // Initialize employee table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setPreferredSize(new Dimension(950, 340));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        loadEmployeeData("");                             // Load all employees initially
    }

    // --- SEARCH PANEL ---
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        searchPanel.setOpaque(false);

        searchPanel.add(new JLabel("Search by ID or Name:"));
        searchField = new JTextField(22);
        JButton searchBtn = createModernButton("Go");
        searchBtn.addActionListener(e -> loadEmployeeData(searchField.getText().trim()));

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        return searchPanel;
    }

    // --- TOP BUTTON BAR ---
    private JPanel createTopButtonBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        topBar.setOpaque(false);

        JButton addBtn = createModernButton("Add Employee");
        JButton editBtn = createModernButton("Edit Employee");
        JButton deleteBtn = createModernButton("Delete Employee");
        JButton refreshBtn = createModernButton("Refresh");

        addBtn.addActionListener(e -> showEmployeeDialog("Add", null));
        editBtn.addActionListener(e -> editSelectedEmployee());
        deleteBtn.addActionListener(e -> deleteEmployee());
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadEmployeeData("");
        });

        topBar.add(addBtn);
        topBar.add(editBtn);
        topBar.add(deleteBtn);
        topBar.add(refreshBtn);

        return topBar;
    }

    // --- EMPLOYEE TABLE SETUP ---
    private JTable createEmployeeTable() {
        JTable table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableModel emptyModel = new DefaultTableModel(
                new String[]{"EmployeeID", "LastName", "FirstName", "Email", "Gender", "Age",
                    "DOB", "Address", "PhoneNumber", "DateOfHire", "Department", "Position", "MonthlySalary"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(emptyModel);
        return table;
    }

    // --- LOAD EMPLOYEE DATA ---
    private void loadEmployeeData(String search) {
        String baseQuery = "SELECT e.EmployeeID, e.LastName, e.FirstName, e.Email, e.Gender, e.Age, e.DOB, e.Address, "
                + "e.PhoneNumber, e.DateOfHire, d.DepartmentName, p.PositionName, e.MonthlySalary "
                + "FROM EMPLOYEE e "
                + "LEFT JOIN DEPARTMENT d ON e.DepartmentID = d.DepartmentID "
                + "LEFT JOIN POSITION p ON e.PositionID = p.PositionID ";
        String where = "";
        Vector<Object> params = new Vector<>();

        if (search != null && !search.isEmpty()) {
            where = "WHERE e.EmployeeID LIKE ? OR e.FirstName LIKE ? OR e.LastName LIKE ? ";
            String like = "%" + search + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        String query = baseQuery + where + "ORDER BY e.EmployeeID";

        Vector<String> columnNames = new Vector<>(java.util.Arrays.asList(
                "EmployeeID", "LastName", "FirstName", "Email", "Gender", "Age",
                "DOB", "Address", "PhoneNumber", "DateOfHire", "Department", "Position", "MonthlySalary"));
        Vector<Vector<Object>> data = new Vector<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("EmployeeID"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("Email"));
                row.add(rs.getString("Gender"));
                row.add(rs.getObject("Age") != null ? rs.getInt("Age") : null);
                row.add(rs.getDate("DOB") != null ? rs.getDate("DOB").toString() : null);
                row.add(rs.getString("Address"));
                row.add(rs.getString("PhoneNumber"));
                row.add(rs.getDate("DateOfHire") != null ? rs.getDate("DateOfHire").toString() : null);
                row.add(rs.getString("DepartmentName") != null ? rs.getString("DepartmentName") : "");
                row.add(rs.getString("PositionName") != null ? rs.getString("PositionName") : "");
                row.add(rs.getBigDecimal("MonthlySalary") != null ? rs.getBigDecimal("MonthlySalary").toString() : "");
                data.add(row);
            }

            table.setModel(new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
            table.setFillsViewportHeight(true);
            table.getTableHeader().setBackground(new Color(60, 63, 65));
            table.getTableHeader().setForeground(Color.WHITE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- SHOW ADD/EDIT EMPLOYEE DIALOG ---
    private void showEmployeeDialog(String action, Integer employeeID) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                action + " Employee", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(30, 30, 30));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(30, 30, 30));
        inputPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JTextField lastNameField = new JTextField(18);
        JTextField firstNameField = new JTextField(18);
        JTextField emailField = new JTextField(20);
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female"});
        JTextField ageField = new JTextField(6);
        JTextField dobField = new JTextField(10);
        JTextField addressField = new JTextField(20);
        JTextField phoneField = new JTextField(12);
        JTextField hireDateField = new JTextField(10);
        JTextField salaryField = new JTextField(10);

        JComboBox<String> deptCombo = new JComboBox<>();
        JComboBox<String> posCombo = new JComboBox<>();
        deptCombo.addItem("0 - <None>");
        posCombo.addItem("0 - <None>");
        loadDepartmentsAndPositions(deptCombo, posCombo);

        String[] labels = {"Last Name", "First Name", "Email", "Gender", "Age", "DOB (YYYY-MM-DD)",
            "Address", "Phone", "Hire Date (YYYY-MM-DD)", "Department", "Position", "Monthly Salary"};
        Component[] fields = {lastNameField, firstNameField, emailField, genderCombo, ageField, dobField,
            addressField, phoneField, hireDateField, deptCombo, posCombo, salaryField};

        // Add labels and fields
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.25;
            JLabel lbl = new JLabel(labels[i] + ":");
            lbl.setForeground(Color.WHITE);
            inputPanel.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.75;
            inputPanel.add(fields[i], gbc);
        }

        // Load existing employee for editing
        if ("Edit".equals(action) && employeeID != null) {
            loadEmployeeIntoFields(employeeID, lastNameField, firstNameField, emailField,
                    genderCombo, ageField, dobField, addressField, phoneField,
                    hireDateField, salaryField, deptCombo, posCombo);
        }

        // Dialog buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setOpaque(false);
        JButton okBtn = createAccentButton(action.equals("Add") ? "Add" : "Save");
        JButton cancelBtn = createModernButton("Cancel");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        okBtn.addActionListener(ev -> saveEmployee(action, employeeID, lastNameField, firstNameField,
                emailField, genderCombo, ageField, dobField, addressField, phoneField,
                hireDateField, salaryField, deptCombo, posCombo, dialog));
        cancelBtn.addActionListener(ev -> dialog.dispose());

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // --- LOAD DEPARTMENTS AND POSITIONS INTO COMBOBOXES ---
    private void loadDepartmentsAndPositions(JComboBox<String> deptCombo, JComboBox<String> posCombo) {
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT DepartmentID, DepartmentName FROM DEPARTMENT")) {
                while (rs.next()) {
                    deptCombo.addItem(rs.getInt("DepartmentID") + " - " + rs.getString("DepartmentName"));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT PositionID, PositionName FROM POSITION")) {
                while (rs.next()) {
                    posCombo.addItem(rs.getInt("PositionID") + " - " + rs.getString("PositionName"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading departments/positions: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- EDIT SELECTED EMPLOYEE ---
    private void editSelectedEmployee() {
        int r = table.getSelectedRow();
        if (r >= 0) {
            Object idObj = table.getValueAt(r, 0);
            if (idObj instanceof Number) {
                showEmployeeDialog("Edit", ((Number) idObj).intValue()); 
            }else {
                JOptionPane.showMessageDialog(this, "Selected row doesn't have a valid EmployeeID.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit.");
        }
    }

    // --- DELETE EMPLOYEE ---
    private void deleteEmployee() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        Object idObj = table.getValueAt(sel, 0);
        if (!(idObj instanceof Number)) {
            JOptionPane.showMessageDialog(this, "Selected row invalid.");
            return;
        }

        int employeeID = ((Number) idObj).intValue();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected employee?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM EMPLOYEE WHERE EmployeeID=?")) {
            ps.setInt(1, employeeID);
            if (ps.executeUpdate() > 0) {
                loadEmployeeData(""); // Refresh table
                JOptionPane.showMessageDialog(this, "Employee deleted.");
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found or already deleted.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting employee: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- LOAD EMPLOYEE DATA INTO FORM FOR EDITING ---
    private void loadEmployeeIntoFields(Integer employeeID, JTextField lastNameField, JTextField firstNameField,
            JTextField emailField, JComboBox<String> genderCombo, JTextField ageField,
            JTextField dobField, JTextField addressField, JTextField phoneField,
            JTextField hireDateField, JTextField salaryField, JComboBox<String> deptCombo,
            JComboBox<String> posCombo) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM EMPLOYEE WHERE EmployeeID=?")) {
            ps.setInt(1, employeeID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lastNameField.setText(rs.getString("LastName"));
                firstNameField.setText(rs.getString("FirstName"));
                emailField.setText(rs.getString("Email"));
                genderCombo.setSelectedItem(rs.getString("Gender"));
                ageField.setText(rs.getObject("Age") != null ? rs.getInt("Age") + "" : "");
                dobField.setText(rs.getDate("DOB") != null ? rs.getDate("DOB").toString() : "");
                addressField.setText(rs.getString("Address"));
                phoneField.setText(rs.getString("PhoneNumber"));
                hireDateField.setText(rs.getDate("DateOfHire") != null ? rs.getDate("DateOfHire").toString() : "");
                salaryField.setText(rs.getBigDecimal("MonthlySalary") != null ? rs.getBigDecimal("MonthlySalary").toString() : "");

                String deptItem = findComboItemStartingWith(deptCombo, rs.getInt("DepartmentID") + "");
                if (deptItem != null) {
                    deptCombo.setSelectedItem(deptItem);
                }

                String posItem = findComboItemStartingWith(posCombo, rs.getInt("PositionID") + "");
                if (posItem != null) {
                    posCombo.setSelectedItem(posItem);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- SAVE EMPLOYEE (ADD OR UPDATE) ---
    private void saveEmployee(String action, Integer employeeID, JTextField lastNameField, JTextField firstNameField,
            JTextField emailField, JComboBox<String> genderCombo, JTextField ageField,
            JTextField dobField, JTextField addressField, JTextField phoneField,
            JTextField hireDateField, JTextField salaryField, JComboBox<String> deptCombo,
            JComboBox<String> posCombo, JDialog dialog) {
        try {
            Integer age = parseIntegerOrNull(ageField.getText().trim());
            String dob = dobField.getText().trim();
            String hireDate = hireDateField.getText().trim();
            BigDecimal salary = salaryField.getText().trim().isEmpty() ? null
                    : new BigDecimal(salaryField.getText().trim());

            if (!dob.isEmpty() && !isValidDateFormat(dob)) {
                JOptionPane.showMessageDialog(dialog, "DOB must be YYYY-MM-DD");
                return;
            }
            if (!hireDate.isEmpty() && !isValidDateFormat(hireDate)) {
                JOptionPane.showMessageDialog(dialog, "Hire Date must be YYYY-MM-DD");
                return;
            }

            int deptId = parseComboId((String) deptCombo.getSelectedItem());
            int posId = parseComboId((String) posCombo.getSelectedItem());

            if ("Add".equals(action)) {
                String insert = "INSERT INTO EMPLOYEE (LastName, FirstName, Email, Gender, Age, DOB, Address, PhoneNumber, DateOfHire, DepartmentID, PositionID, MonthlySalary) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = connection.prepareStatement(insert)) {
                    ps.setString(1, lastNameField.getText().trim());
                    ps.setString(2, firstNameField.getText().trim());
                    ps.setString(3, emailField.getText().trim());
                    ps.setString(4, (String) genderCombo.getSelectedItem());
                    if (age != null) {
                        ps.setInt(5, age);
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    ps.setString(6, dob.isEmpty() ? null : dob);
                    ps.setString(7, addressField.getText().trim());
                    ps.setString(8, phoneField.getText().trim());
                    ps.setString(9, hireDate.isEmpty() ? null : hireDate);
                    ps.setInt(10, deptId);
                    ps.setInt(11, posId);
                    ps.setBigDecimal(12, salary);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(dialog, "Employee added successfully.");
            } else { // Edit
                String update = "UPDATE EMPLOYEE SET LastName=?, FirstName=?, Email=?, Gender=?, Age=?, DOB=?, "
                        + "Address=?, PhoneNumber=?, DateOfHire=?, DepartmentID=?, PositionID=?, MonthlySalary=? "
                        + "WHERE EmployeeID=?";
                try (PreparedStatement ps = connection.prepareStatement(update)) {
                    ps.setString(1, lastNameField.getText().trim());
                    ps.setString(2, firstNameField.getText().trim());
                    ps.setString(3, emailField.getText().trim());
                    ps.setString(4, (String) genderCombo.getSelectedItem());
                    if (age != null) {
                        ps.setInt(5, age);
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    ps.setString(6, dob.isEmpty() ? null : dob);
                    ps.setString(7, addressField.getText().trim());
                    ps.setString(8, phoneField.getText().trim());
                    ps.setString(9, hireDate.isEmpty() ? null : hireDate);
                    ps.setInt(10, deptId);
                    ps.setInt(11, posId);
                    ps.setBigDecimal(12, salary);
                    ps.setInt(13, employeeID);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(dialog, "Employee updated successfully.");
            }

            loadEmployeeData(""); // Refresh table
            dialog.dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Invalid number format: " + ex.getMessage());
        }
    }

    // --- HELPER METHODS ---
    private static Integer parseIntegerOrNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean isValidDateFormat(String s) {
        return s.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private static int parseComboId(String comboItem) {
        if (comboItem == null) {
            return 0;
        }
        try {
            return Integer.parseInt(comboItem.split(" - ", 2)[0].trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private static String findComboItemStartingWith(JComboBox<String> combo, String prefix) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String it = combo.getItemAt(i);
            if (it != null && it.startsWith(prefix)) {
                return it;
            }
        }
        return null;
    }

    // --- BUTTON STYLES ---
    private JButton createModernButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(new Color(60, 63, 65));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(85, 90, 92));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(60, 63, 65));
            }
        });
        return b;
    }

    private JButton createAccentButton(String text) {
        JButton b = createModernButton(text);
        b.setBackground(new Color(200, 120, 0));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(220, 140, 20));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(200, 120, 0));
            }
        });
        return b;
    }
}
