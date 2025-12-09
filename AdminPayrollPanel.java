
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminPayrollPanel extends JPanel {

    // Shared DB connection (provided by caller)
    private final Connection connection;

    // Main table showing payroll calculation summaries
    private final JTable table;

    // Constructor: create UI, wire actions, and load initial data
    public AdminPayrollPanel(Connection connection) {
        super(new BorderLayout(12, 12));

        // Keep reference to DB connection for all DB operations
        this.connection = connection;

        // Visual styling for the panel
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Build top button bar (Add, View Deductions, Delete, Refresh)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton addBtn = createModernButton("Add Payroll");
        JButton viewBtn = createModernButton("View Deductions");
        JButton deleteBtn = createModernButton("Delete Payroll");
        JButton refreshBtn = createModernButton("Refresh");

        // Wire button actions to handler methods
        addBtn.addActionListener(e -> showAddPayrollDialog());
        viewBtn.addActionListener(e -> showViewPayrollDeductionsDialog());
        deleteBtn.addActionListener(e -> deleteSelectedPayroll());
        refreshBtn.addActionListener(e -> loadPayrollData());

        // Add buttons to the top bar
        buttonPanel.add(addBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        // Configure the main table used for payroll listings
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Apply consistent dark theme colors
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(60, 63, 65));
        table.getTableHeader().setForeground(Color.WHITE);

        // Wrap table in scroll pane sized to fit typical content
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setPreferredSize(new Dimension(940, 320));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add components to main layout
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load data right away so UI shows current payrolls
        loadPayrollData();
    }

    // Loads payroll summary records into the table
    // Queries PAYROLL_CALC joined to EMPLOYEE and fills a non-editable table model.
    private void loadPayrollData() {
        // SQL to fetch payroll summary with employee names
        String query = "SELECT pc.PayrollID, pc.EmployeeID, e.FirstName, e.LastName, pc.ReferenceNo, "
                + "pc.Start_Cut_Off, pc.End_Cut_Off, pc.TotalGrossPay, pc.TotalDeduction, pc.NetPay "
                + "FROM PAYROLL_CALC pc "
                + "JOIN EMPLOYEE e ON pc.EmployeeID = e.EmployeeID "
                + "ORDER BY pc.PayrollID";

        // Use try-with-resources to ensure resources are closed
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            // Define visible column names (keeps UI stable)
            Vector<String> columnNames = new Vector<>();
            columnNames.add("PayrollID");
            columnNames.add("EmployeeID");
            columnNames.add("FirstName");
            columnNames.add("LastName");
            columnNames.add("ReferenceNo");
            columnNames.add("Start Cut-Off");
            columnNames.add("End Cut-Off");
            columnNames.add("TotalGrossPay");
            columnNames.add("TotalDeduction");
            columnNames.add("NetPay");

            // Collect rows from ResultSet into Vector<Vector<Object>>
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("PayrollID"));
                row.add(rs.getInt("EmployeeID"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("ReferenceNo"));
                row.add(rs.getDate("Start_Cut_Off"));
                row.add(rs.getDate("End_Cut_Off"));
                row.add(rs.getBigDecimal("TotalGrossPay"));
                row.add(rs.getBigDecimal("TotalDeduction"));
                row.add(rs.getBigDecimal("NetPay"));
                data.add(row);
            }

            // Apply a non-editable table model so cells cannot be edited directly
            table.setModel(new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

        } catch (SQLException e) {
            // Show helpful error to user if DB read fails
            JOptionPane.showMessageDialog(this, "Error loading payroll data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dialog to add a payroll for an employee between two dates
    // Inserts into PAYROLL, copies default deductions, creates gross pay entries for timesheets
    private void showAddPayrollDialog() {
        // Input fields for simple form
        JTextField employeeIdField = new JTextField();
        JTextField startCutOffField = new JTextField("YYYY-MM-DD");
        JTextField endCutOffField = new JTextField("YYYY-MM-DD");

        // Build a compact form panel (labels + fields)
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(new Color(30, 30, 30));
        panel.add(createLabel("Employee ID:"));
        panel.add(employeeIdField);
        panel.add(createLabel("Start Cut-Off (YYYY-MM-DD):"));
        panel.add(startCutOffField);
        panel.add(createLabel("End Cut-Off (YYYY-MM-DD):"));
        panel.add(endCutOffField);

        // Show the dialog and stop if user cancels
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Payroll",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Read and validate inputs
        String empIdStr = employeeIdField.getText().trim();
        String startCutOff = startCutOffField.getText().trim();
        String endCutOff = endCutOffField.getText().trim();

        if (empIdStr.isEmpty() || startCutOff.isEmpty() || endCutOff.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        int empId;
        try {
            // Parse employee ID to integer; show message on failure
            empId = Integer.parseInt(empIdStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Employee ID must be a number.");
            return;
        }

        // Database operations: check employee -> insert payroll -> copy deductions -> compute gross pay
        try {
            // 1) Verify employee exists
            try (PreparedStatement checkEmp = connection.prepareStatement(
                    "SELECT 1 FROM EMPLOYEE WHERE EmployeeID = ?")) {
                checkEmp.setInt(1, empId);
                try (ResultSet rsEmp = checkEmp.executeQuery()) {
                    if (!rsEmp.next()) {
                        JOptionPane.showMessageDialog(this, "Employee does not exist.");
                        return;
                    }
                }
            }

            // 2) Insert into PAYROLL and retrieve generated key (PayrollID)
            String referenceNo = "PR-" + empId + "-" + System.currentTimeMillis();
            int payrollId;
            try (PreparedStatement insertPayroll = connection.prepareStatement(
                    "INSERT INTO PAYROLL (EmployeeID, ReferenceNo, Start_Cut_Off, End_Cut_Off) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                insertPayroll.setInt(1, empId);
                insertPayroll.setString(2, referenceNo);

                // Convert string to SQL Date; Date.valueOf() throws IllegalArgumentException if format wrong
                insertPayroll.setDate(3, Date.valueOf(startCutOff));
                insertPayroll.setDate(4, Date.valueOf(endCutOff));

                insertPayroll.executeUpdate();

                // Retrieve the generated payroll id
                try (ResultSet genKeys = insertPayroll.getGeneratedKeys()) {
                    if (!genKeys.next()) {
                        JOptionPane.showMessageDialog(this, "Failed to insert payroll.");
                        return;
                    }
                    payrollId = genKeys.getInt(1);
                }
            }

            // 3) Copy all default deductions from DEDUCTION into PAYROLL_DEDUCTION for this payroll
            try (PreparedStatement getDeductions = connection.prepareStatement(
                    "SELECT DeductionID, Default_Amount FROM DEDUCTION"); ResultSet dedRs = getDeductions.executeQuery(); PreparedStatement insertDed = connection.prepareStatement(
                    "INSERT INTO PAYROLL_DEDUCTION (PayrollID, DeductionID, Amount) VALUES (?, ?, ?)")) {

                while (dedRs.next()) {
                    insertDed.setInt(1, payrollId);
                    insertDed.setInt(2, dedRs.getInt("DeductionID"));
                    insertDed.setBigDecimal(3, dedRs.getBigDecimal("Default_Amount"));
                    insertDed.executeUpdate();
                }
            }

            // 4) For each timesheet in the payroll range, insert GROSS_PAY if missing and compute rates
            try (PreparedStatement getTimesheets = connection.prepareStatement(
                    "SELECT TimesheetID, EmployeeID FROM TIMESHEET WHERE EmployeeID = ? AND WorkDate BETWEEN ? AND ?")) {

                getTimesheets.setInt(1, empId);
                getTimesheets.setDate(2, Date.valueOf(startCutOff));
                getTimesheets.setDate(3, Date.valueOf(endCutOff));

                try (ResultSet tsRs = getTimesheets.executeQuery(); PreparedStatement insertGross = connection.prepareStatement(
                        "INSERT INTO GROSS_PAY (TimesheetID, StdHourlyRate, OTHourlyRate) VALUES (?, ?, ?)")) {

                    while (tsRs.next()) {
                        int timesheetId = tsRs.getInt("TimesheetID");

                        // Check if gross pay already exists for this timesheet
                        boolean grossExists;
                        try (PreparedStatement checkGross = connection.prepareStatement(
                                "SELECT 1 FROM GROSS_PAY WHERE TimesheetID = ?")) {
                            checkGross.setInt(1, timesheetId);
                            try (ResultSet checkRs = checkGross.executeQuery()) {
                                grossExists = checkRs.next();
                            }
                        }

                        if (!grossExists) {
                            // Get monthly salary for employee to calculate rates
                            BigDecimal stdRate = BigDecimal.ZERO;
                            BigDecimal otRate = BigDecimal.ZERO;
                            try (PreparedStatement getSalary = connection.prepareStatement(
                                    "SELECT MonthlySalary FROM EMPLOYEE WHERE EmployeeID = ?")) {
                                getSalary.setInt(1, empId);
                                try (ResultSet salaryRs = getSalary.executeQuery()) {
                                    if (salaryRs.next()) {
                                        BigDecimal monthly = salaryRs.getBigDecimal("MonthlySalary");
                                        if (monthly != null) {
                                            // Standard hourly = monthly / (22 days * 8 hours), rounded to 2 decimals
                                            BigDecimal divisor = new BigDecimal(22 * 8);
                                            stdRate = monthly.divide(divisor, 2, BigDecimal.ROUND_HALF_UP);
                                            // OT rate is 125% of standard rate
                                            otRate = stdRate.multiply(new BigDecimal("1.25"));
                                        }
                                    }
                                }
                            }

                            // Insert the computed gross pay rates for this timesheet
                            insertGross.setInt(1, timesheetId);
                            insertGross.setBigDecimal(2, stdRate);
                            insertGross.setBigDecimal(3, otRate);
                            insertGross.executeUpdate();
                        }
                    }
                }
            }

            // Inform user and refresh UI so totals appear after background DB updates
            JOptionPane.showMessageDialog(this, "Payroll added and totals computed automatically.");
            loadPayrollData();

        } catch (SQLException e) {
            // Capture any SQL error and show descriptive message
            JOptionPane.showMessageDialog(this, "Error adding payroll: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException iae) {
            // Handle invalid date format passed to Date.valueOf()
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Shows a modal dialog displaying deductions for the selected payroll
    private void showViewPayrollDeductionsDialog() {
        // Ensure user selected a payroll row first
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a payroll record first.");
            return;
        }

        // PayrollID is stored in first column of the table model
        int payrollId = (int) table.getValueAt(selectedRow, 0);

        // Build modal dialog with consistent styling
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "Payroll Deductions", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setSize(500, 400);
        dialog.getContentPane().setBackground(new Color(30, 30, 30));

        JTable deductionTable = new JTable();
        deductionTable.setBackground(new Color(45, 45, 45));
        deductionTable.setForeground(Color.WHITE);
        deductionTable.getTableHeader().setBackground(new Color(60, 63, 65));
        deductionTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(deductionTable);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Prepare table columns
        Vector<String> columns = new Vector<>();
        columns.add("PayrollDeductionID");
        columns.add("DeductionID");
        columns.add("Description");
        columns.add("Amount");

        // Load deduction rows for this payroll
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT pd.PayrollDeductionID, pd.DeductionID, d.Description, pd.Amount "
                + "FROM PAYROLL_DEDUCTION pd JOIN DEDUCTION d ON pd.DeductionID = d.DeductionID "
                + "WHERE pd.PayrollID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, payrollId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("PayrollDeductionID"));
                    row.add(rs.getInt("DeductionID"));
                    row.add(rs.getString("Description"));
                    row.add(rs.getBigDecimal("Amount"));
                    data.add(row);
                }
            }

            // Apply model and disable editing
            deductionTable.setModel(new DefaultTableModel(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

        } catch (SQLException e) {
            // Show error and close dialog on failure to load deductions
            JOptionPane.showMessageDialog(dialog, "Error loading deductions: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            dialog.dispose();
            return;
        }

        // Center and show dialog
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Deletes the selected payroll and its associated deductions
    private void deleteSelectedPayroll() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a payroll to delete.");
            return;
        }

        // Get payroll id from table
        int payrollId = (int) table.getValueAt(selectedRow, 0);

        // Confirm deletion with the user to prevent accidental removal
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete payroll and associated deductions?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Execute delete operations in proper order: PAYROLL_DEDUCTION then PAYROLL
        try (PreparedStatement deleteDeds = connection.prepareStatement(
                "DELETE FROM PAYROLL_DEDUCTION WHERE PayrollID = ?"); PreparedStatement deletePayroll = connection.prepareStatement(
                        "DELETE FROM PAYROLL WHERE PayrollID = ?")) {

            deleteDeds.setInt(1, payrollId);
            deleteDeds.executeUpdate();

            deletePayroll.setInt(1, payrollId);
            deletePayroll.executeUpdate();

            // Refresh UI after successful deletion
            loadPayrollData();
            JOptionPane.showMessageDialog(this, "Payroll deleted.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting payroll: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // UI helper: creates a white label for dark panels
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // UI helper: creates a modern dark-themed button with hover effect
    private JButton createModernButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setBackground(new Color(60, 63, 65));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Hover effect: brighten background on mouse enter
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
