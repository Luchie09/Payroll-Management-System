
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class EmployeeDashboard extends JFrame {

    private final Connection connection;          // Active database connection
    private final int currentEmployeeID;          // ID of logged-in employee

    // UI Colors for consistent dark theme
    private final Color BLACK_COLOR = new Color(20, 20, 20);
    private final Color DARK_GRAY = new Color(35, 35, 35);
    private final Color MID_GRAY = new Color(55, 55, 55);
    private final Color LIGHT_GRAY = new Color(180, 180, 180);
    private final Color WHITE_COLOR = Color.WHITE;
    private final Color RED_ACCENT = new Color(228, 54, 54); // Highlight color

    public EmployeeDashboard(int employeeID) {
        this.currentEmployeeID = employeeID;
        this.connection = initializeDBConnection(); // Establish DB connection
        initializeUI();                             // Build UI components
    }

    // Connects to MySQL database, exits app if connection fails
    private Connection initializeDBConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/payrolldb", "root", "1234");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    // Initialize main UI frame and components
    private void initializeUI() {
        setTitle("Employee Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BLACK_COLOR);

        // Create menu bar with File -> Logout / Exit options
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(DARK_GRAY);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(WHITE_COLOR);

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout()); // Return to login screen

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0)); // Close app

        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Create tabbed interface for Profile, Timesheets, Payroll
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BLACK_COLOR);
        tabbedPane.setForeground(WHITE_COLOR);
        tabbedPane.setOpaque(true);

        tabbedPane.addTab("My Profile", createProfilePanel());
        tabbedPane.addTab("My Timesheets", createTimesheetPanel());
        tabbedPane.addTab("My Payroll", createPayrollPanel());

        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    // Builds profile panel with rounded card UI and employee info
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BLACK_COLOR);

        RoundedPanel card = new RoundedPanel(30);       // Rounded card container
        card.setBackground(DARK_GRAY);
        card.setPreferredSize(new Dimension(900, 500));
        card.setLayout(new BorderLayout(25, 25));       // Padding between sections
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header: avatar and name/email labels
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        header.setOpaque(false);

        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setOpaque(true);
        avatar.setBackground(RED_ACCENT);
        avatar.setHorizontalAlignment(JLabel.CENTER);

        try { // Attempt to load user image
            ImageIcon icon = new ImageIcon(getClass().getResource("/user.png"));
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));
        } catch (Exception e) { // Fallback to emoji if image fails
            avatar.setText("👤");
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 28));
            avatar.setForeground(WHITE_COLOR);
        }

        // Name and email labels stacked vertically
        JPanel nameEmailPanel = new JPanel();
        nameEmailPanel.setLayout(new BoxLayout(nameEmailPanel, BoxLayout.Y_AXIS));
        nameEmailPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("Your Name");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(WHITE_COLOR);

        JLabel emailLabel = new JLabel("your@email.com");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(LIGHT_GRAY);

        nameEmailPanel.add(nameLabel);
        nameEmailPanel.add(Box.createVerticalStrut(5));
        nameEmailPanel.add(emailLabel);

        header.add(avatar);
        header.add(nameEmailPanel);
        card.add(header, BorderLayout.NORTH);

        // Information panel: detailed employee data
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15); // Space between rows
        gbc.anchor = GridBagConstraints.WEST;

        try {
            String query = """
                    SELECT e.EmployeeID, e.FirstName, e.LastName, e.Age, e.DOB,
                           e.Address, e.PhoneNumber, e.DateOfHire, e.Gender,
                           e.Email, d.DepartmentName, p.PositionName, e.MonthlySalary
                    FROM EMPLOYEE e
                    LEFT JOIN DEPARTMENT d ON e.DepartmentID = d.DepartmentID
                    LEFT JOIN POSITION p ON e.PositionID = p.PositionID
                    WHERE e.EmployeeID = ?
            """;

            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, currentEmployeeID); // Bind employee ID
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) { // Populate labels with database info
                nameLabel.setText(rs.getString("FirstName") + " " + rs.getString("LastName"));
                emailLabel.setText(rs.getString("Email"));

                int row = 0;
                addStyledRow(infoPanel, gbc, row++, "Employee ID:", rs.getString("EmployeeID"));
                addStyledRow(infoPanel, gbc, row++, "Gender:", rs.getString("Gender"));
                addStyledRow(infoPanel, gbc, row++, "Age:", rs.getString("Age"));
                addStyledRow(infoPanel, gbc, row++, "Birthday:", rs.getString("DOB"));
                addStyledRow(infoPanel, gbc, row++, "Phone:", rs.getString("PhoneNumber"));
                addStyledRow(infoPanel, gbc, row++, "Address:", rs.getString("Address"));
                addStyledRow(infoPanel, gbc, row++, "Department:", rs.getString("DepartmentName"));
                addStyledRow(infoPanel, gbc, row++, "Position:", rs.getString("PositionName"));
                addStyledRow(infoPanel, gbc, row++, "Date Hired:", rs.getString("DateOfHire"));

                BigDecimal salary = rs.getBigDecimal("MonthlySalary");
                if (salary != null) { // Format salary to 2 decimals
                    addStyledRow(infoPanel, gbc, row++, "Monthly Salary:",
                            "₱" + salary.setScale(2, RoundingMode.HALF_UP));
                }
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
        }

        card.add(infoPanel, BorderLayout.CENTER);
        panel.add(card);
        return panel;
    }

    // Helper: add label-value row in profile panel
    private void addStyledRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(LIGHT_GRAY);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel val = new JLabel(value);
        val.setForeground(WHITE_COLOR);
        val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(val, gbc);
    }

    // Create timesheet panel with table and refresh button
    private JPanel createTimesheetPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BLACK_COLOR);

        JTable table = new JTable();
        styleTable(table); // Apply UI styling

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(DARK_GRAY);

        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadTimesheetData(table)); // Reload data

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(BLACK_COLOR);
        buttonPanel.add(refreshBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadTimesheetData(table); // Initial load
        return panel;
    }

    // Query and populate timesheet table from database
    private void loadTimesheetData(JTable table) {
        try {
            String query = """
                    SELECT TimesheetID, WorkDate, StandardHours, RenderedHours, OvertimeHours 
                    FROM TIMESHEET 
                    WHERE EmployeeID = ?
            """;

            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, currentEmployeeID); // Bind employee ID
            ResultSet rs = pstmt.executeQuery();

            Vector<String> columns = new Vector<>();
            columns.add("ID");
            columns.add("Date");
            columns.add("Standard Hrs");
            columns.add("Rendered Hrs");
            columns.add("Overtime");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("TimesheetID"));
                row.add(rs.getDate("WorkDate"));
                row.add(rs.getBigDecimal("StandardHours"));
                row.add(rs.getBigDecimal("RenderedHours"));
                row.add(rs.getBigDecimal("OvertimeHours"));
                data.add(row);
            }

            table.setModel(new DefaultTableModel(data, columns));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Create payroll panel similar to timesheet panel
    private JPanel createPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BLACK_COLOR);

        JTable table = new JTable();
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(DARK_GRAY);

        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadPayrollData(table));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(BLACK_COLOR);
        top.add(refreshBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadPayrollData(table);
        return panel;
    }

    // Query and populate payroll data
    private void loadPayrollData(JTable table) {
        try {
            String query = """
                    SELECT PayrollID, ReferenceNo, Start_Cut_Off, End_Cut_Off,
                           TotalGrossPay, TotalDeduction, NetPay
                    FROM PAYROLL_CALC 
                    WHERE EmployeeID = ?
            """;

            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, currentEmployeeID);
            ResultSet rs = pstmt.executeQuery();

            Vector<String> columns = new Vector<>();
            columns.add("ID");
            columns.add("Reference");
            columns.add("Start");
            columns.add("End");
            columns.add("Gross");
            columns.add("Deduction");
            columns.add("Net");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("PayrollID"));
                row.add(rs.getString("ReferenceNo"));
                row.add(rs.getDate("Start_Cut_Off"));
                row.add(rs.getDate("End_Cut_Off"));
                row.add(rs.getBigDecimal("TotalGrossPay"));
                row.add(rs.getBigDecimal("TotalDeduction"));
                row.add(rs.getBigDecimal("NetPay"));
                data.add(row);
            }

            table.setModel(new DefaultTableModel(data, columns));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Apply consistent styling to tables
    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setBackground(DARK_GRAY);
        table.setForeground(WHITE_COLOR);
        table.getTableHeader().setBackground(RED_ACCENT);
        table.getTableHeader().setForeground(WHITE_COLOR);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(RED_ACCENT);
    }

    // Apply consistent styling to buttons
    private void styleButton(JButton button) {
        button.setBackground(RED_ACCENT);
        button.setForeground(WHITE_COLOR);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    // Logout action: close current dashboard and open login screen
    private void logout() {
        JOptionPane.showMessageDialog(this, "Logged out successfully");
        dispose();
        new Login();
    }

}

// Panel with rounded corners
class RoundedPanel extends JPanel {

    private final int radius;

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false); // Allow custom painting
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius); // Draw rounded rect
        g2.dispose();
        super.paintComponent(g);
    }
}
