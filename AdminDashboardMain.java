
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboardMain extends JFrame {

    // Stores currently logged-in user role and ID
    private String currentUserRole;
    private int currentUserID;

    // Shared database connection for all panels
    private Connection connection;

    // Components used by the global search dialog
    private JTable searchTable;
    private JDialog searchDialog;

    public AdminDashboardMain(String role, int userID) {
        // Save session user details
        this.currentUserRole = role;
        this.currentUserID = userID;

        // Establish DB connection using helper class
        this.connection = AdminDBUtils.getConnection();

        // Build the dashboard UI
        initializeUI();
    }

    private void initializeUI() {
        // Window setup
        setTitle("Payroll Management System Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(30, 30, 30));

        // MENU BAR
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(30, 30, 30));
        menuBar.setForeground(Color.WHITE);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setBackground(new Color(80, 80, 80));
        fileMenu.setForeground(Color.WHITE);

        // Logout button
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setBackground(new Color(200, 120, 0));
        logoutItem.setForeground(Color.BLACK);
        logoutItem.addActionListener(e -> logout());

        // Exit program
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setBackground(new Color(200, 120, 0));
        exitItem.setForeground(Color.BLACK);
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // TOP SEARCH PANEL
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(new Color(30, 30, 30));

        // Textbox for search input
        JTextField searchField = new JTextField(20);
        searchField.setBackground(new Color(60, 60, 60));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.ORANGE);

        // Search button triggers global search
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(228, 54, 54));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setFont(searchButton.getFont().deriveFont(Font.BOLD));
        searchButton.addActionListener(e -> performGlobalSearch(searchField.getText().trim()));

        topPanel.add(searchField);
        topPanel.add(searchButton);

        // TABBED PANELS
        JTabbedPane tabbedPane = new JTabbedPane();

        // Create each admin panel and pass DB connection
        tabbedPane.addTab("Employees", new AdminEmployeePanel(connection));
        tabbedPane.addTab("Departments", new AdminDepartmentPanel(connection));
        tabbedPane.addTab("Positions", new AdminPositionPanel(connection));
        tabbedPane.addTab("Timesheets", new AdminTimesheetPanel(connection));
        tabbedPane.addTab("Payroll", new AdminPayrollPanel(connection));
        tabbedPane.addTab("Deductions", new AdminDeductionPanel(connection));
        tabbedPane.addTab("Users", new UserAccountsPanel(connection));
        tabbedPane.addTab("Admin", new AdminUserAccountsPanel(connection));

        // Disable restricted tabs for non-admin users
        if ("Employee".equals(currentUserRole)) {
            tabbedPane.setEnabledAt(1, false);
            tabbedPane.setEnabledAt(2, false);
            tabbedPane.setEnabledAt(5, false);
        }

        // ADD LAYOUT
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    // Performs a global search across all key database tables
    private void performGlobalSearch(String searchText) {
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.");
            return;
        }

        // Stores all row results found across tables
        Vector<Object[]> allResults = new Vector<>();

        // Stores column names (only assigned by Employee section)
        Vector<String> allColumns = new Vector<>();

        try {
            // EMPLOYEE SEARCH 
            // Searches employee details matching basic info
            String eSql = "SELECT EmployeeID, LastName, FirstName, Age, PhoneNumber, Address "
                    + "FROM EMPLOYEE WHERE EmployeeID LIKE ? OR LastName LIKE ? OR FirstName LIKE ? "
                    + "OR PhoneNumber LIKE ? OR Address LIKE ?";

            try (PreparedStatement stmt = connection.prepareStatement(eSql)) {
                for (int i = 1; i <= 5; i++) {
                    stmt.setString(i, "%" + searchText + "%");
                }

                try (ResultSet rs = stmt.executeQuery()) {

                    // Define columns only once
                    allColumns.clear();
                    allColumns.add("Type");
                    allColumns.add("ID");
                    allColumns.add("LastName");
                    allColumns.add("FirstName");
                    allColumns.add("Age");
                    allColumns.add("PhoneNumber");
                    allColumns.add("Address");

                    // Add each result row to list
                    while (rs.next()) {
                        allResults.add(new Object[]{
                            "Employee",
                            rs.getInt("EmployeeID"),
                            rs.getString("LastName"),
                            rs.getString("FirstName"),
                            rs.getObject("Age"),
                            rs.getString("PhoneNumber"),
                            rs.getString("Address")
                        });
                    }
                }
            }

            // DEPARTMENT SEARCH
            String dSql = "SELECT DepartmentID, DepartmentName FROM DEPARTMENT "
                    + "WHERE DepartmentID LIKE ? OR DepartmentName LIKE ?";

            try (PreparedStatement stmt = connection.prepareStatement(dSql)) {
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        allResults.add(new Object[]{
                            "Department",
                            rs.getInt("DepartmentID"),
                            rs.getString("DepartmentName"),
                            null, null, null, null
                        });
                    }
                }
            }

            // POSITION SEARCH
            String pSql = "SELECT PositionID, PositionName, DepartmentID, BaseSalary FROM POSITION "
                    + "WHERE PositionID LIKE ? OR PositionName LIKE ?";

            try (PreparedStatement stmt = connection.prepareStatement(pSql)) {
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        allResults.add(new Object[]{
                            "Position",
                            rs.getInt("PositionID"),
                            rs.getString("PositionName"),
                            rs.getString("DepartmentID"),
                            null,
                            rs.getBigDecimal("BaseSalary"),
                            null
                        });
                    }
                }
            }

            // TIMESHEET SEARCH
            String tSql = "SELECT TimesheetID, EmployeeID, WorkDate, RenderedHours, OvertimeHours "
                    + "FROM TIMESHEET WHERE TimesheetID LIKE ? OR EmployeeID LIKE ?";

            try (PreparedStatement stmt = connection.prepareStatement(tSql)) {
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        allResults.add(new Object[]{
                            "Timesheet",
                            rs.getInt("TimesheetID"),
                            rs.getInt("EmployeeID"),
                            rs.getDate("WorkDate").toString(),
                            rs.getBigDecimal("RenderedHours"),
                            rs.getBigDecimal("OvertimeHours"),
                            null
                        });
                    }
                }
            }

            // PAYROLL SEARCH
            String paySql = "SELECT PayrollID, EmployeeID, ReferenceNo, Start_Cut_Off, End_Cut_Off "
                    + "FROM PAYROLL WHERE PayrollID LIKE ? OR EmployeeID LIKE ? OR ReferenceNo LIKE ?";

            try (PreparedStatement stmt = connection.prepareStatement(paySql)) {
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");
                stmt.setString(3, "%" + searchText + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        allResults.add(new Object[]{
                            "Payroll",
                            rs.getInt("PayrollID"),
                            rs.getInt("EmployeeID"),
                            rs.getString("ReferenceNo"),
                            rs.getDate("Start_Cut_Off").toString(),
                            rs.getDate("End_Cut_Off").toString(),
                            null
                        });
                    }
                }
            }

            // DEDUCTION SEARCH
            String d2Sql = "SELECT DeductionID, Description, Default_Amount FROM DEDUCTION "
                    + "WHERE DeductionID LIKE ? OR Description LIKE ?";

            try (PreparedStatement stmt = connection.prepareStatement(d2Sql)) {
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        allResults.add(new Object[]{
                            "Deduction",
                            rs.getInt("DeductionID"),
                            rs.getString("Description"),
                            null, null,
                            rs.getBigDecimal("Default_Amount"),
                            null
                        });
                    }
                }
            }

            // USER SEARCH
            String userSql = "SELECT UserID, Username, Role FROM USER "
                    + "WHERE UserID LIKE ? OR Username LIKE ?";

            try (PreparedStatement stmt = connection.prepareStatement(userSql)) {
                stmt.setString(1, "%" + searchText + "%");
                stmt.setString(2, "%" + searchText + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        allResults.add(new Object[]{
                            "User",
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            rs.getString("Role"),
                            null, null, null
                        });
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching: " + ex.getMessage());
        }

        // No match found
        if (allResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for '" + searchText + "'");
            return;
        }

        // Display results in table dialog
        showSearchDialog(allColumns, allResults, searchText);
    }

    // Builds and displays the search results popup window
    private void showSearchDialog(Vector<String> columns, Vector<Object[]> results, String searchText) {
        // Close old search window if already open
        if (searchDialog != null && searchDialog.isVisible()) {
            searchDialog.dispose();
        }

        searchDialog = new JDialog(this, "Search results for: " + searchText, true);
        searchDialog.setLayout(new BorderLayout());

        // Create table model containing all result rows
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Object[] row : results) {
            model.addRow(row);
        }

        // Table setup
        searchTable = new JTable(model);
        searchTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        searchTable.setFillsViewportHeight(true);

        // Scroll pane for results
        JScrollPane pane = new JScrollPane(searchTable);
        pane.setPreferredSize(new Dimension(900, 350));
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        searchDialog.add(pane, BorderLayout.CENTER);

        // Close button at bottom
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> searchDialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(closeBtn);
        searchDialog.add(btnPanel, BorderLayout.SOUTH);

        searchDialog.setSize(950, 420);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setVisible(true);
    }

    // Placeholder for future audit log feature
    private void showAuditLog() {
        JOptionPane.showMessageDialog(this, "Audit log feature not implemented yet.");
    }

    // Logs out user and returns to login screen
    private void logout() {
        JOptionPane.showMessageDialog(this, "You have been logged out.");
        dispose();
        new Login().setVisible(true);
    }

}
