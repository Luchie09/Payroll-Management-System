
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Login extends JFrame {

    // UI Components
    private JPanel leftPanel, rightPanel;
    private CardLayout cardLayout;

    private JTextField tfUsername, tfRegUsername, tfEmployeeID;
    private JPasswordField pfPassword, pfRegPassword;

    // Constructor: Initialize UI
    public Login() {
        initializeUI();
    }

    // Setup Main UI
    private void initializeUI() {
        setTitle("Authentication Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupLeftPanel();   // Navigation panel
        setupRightPanel();  // Forms panel

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // Setup Navigation Panel
    private void setupLeftPanel() {
        leftPanel = new JPanel();
        leftPanel.setBackground(new Color(30, 30, 30));
        leftPanel.setLayout(new GridLayout(10, 1, 0, 10));
        leftPanel.setPreferredSize(new Dimension(250, 600));

        // Title label
        JLabel lblWelcome = new JLabel("Welcome", SwingConstants.CENTER);
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));

        // Navigation buttons
        JButton btnLoginNav = new JButton("Log In");
        JButton btnRegisterNav = new JButton("Register");
        styleNavButton(btnLoginNav);
        styleNavButton(btnRegisterNav);

        // Switch forms using CardLayout
        btnLoginNav.addActionListener(e -> cardLayout.show(rightPanel, "LoginForm"));
        btnRegisterNav.addActionListener(e -> cardLayout.show(rightPanel, "RegisterForm"));

        leftPanel.add(lblWelcome);
        leftPanel.add(btnLoginNav);
        leftPanel.add(btnRegisterNav);
    }

    // Setup Forms Panel
    private void setupRightPanel() {
        cardLayout = new CardLayout();
        rightPanel = new JPanel(cardLayout);

        // Add login and register forms
        rightPanel.add(createLoginForm(), "LoginForm");
        rightPanel.add(createRegisterForm(), "RegisterForm");

        cardLayout.show(rightPanel, "LoginForm"); // Default form
    }

    // Style Navigation Buttons
    private void styleNavButton(JButton btn) {
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
    }

    // Create Login Form
    private JPanel createLoginForm() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(40, 40, 40));

        // Title
        JLabel title = new JLabel("Log In", SwingConstants.CENTER);
        title.setBounds(250, 40, 300, 40);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        // Username field
        JLabel lblUser = new JLabel("Username");
        lblUser.setBounds(250, 120, 200, 20);
        lblUser.setForeground(Color.WHITE);
        tfUsername = new JTextField();
        tfUsername.setBounds(250, 145, 300, 30);

        // Password field
        JLabel lblPass = new JLabel("Password");
        lblPass.setBounds(250, 190, 200, 20);
        lblPass.setForeground(Color.WHITE);
        pfPassword = new JPasswordField();
        pfPassword.setBounds(250, 215, 300, 30);
        pfPassword.setEchoChar('•');

        // Show password checkbox
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setBounds(250, 250, 200, 20);
        showPass.setForeground(Color.WHITE);
        showPass.setBackground(new Color(40, 40, 40));
        showPass.addActionListener(e -> pfPassword.setEchoChar(showPass.isSelected() ? (char) 0 : '•'));

        // Login button
        JButton btnLogin = new JButton("Log In");
        btnLogin.setBounds(250, 300, 300, 35);
        btnLogin.setBackground(new Color(0xE4, 0x36, 0x36));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.addActionListener(e -> performLogin()); // Perform login on click

        // Add components to panel
        panel.add(title);
        panel.add(lblUser);
        panel.add(tfUsername);
        panel.add(lblPass);
        panel.add(pfPassword);
        panel.add(showPass);
        panel.add(btnLogin);

        return panel;
    }

    // Create Registration Form
    private JPanel createRegisterForm() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(40, 40, 40));

        // Title
        JLabel title = new JLabel("Register Account", SwingConstants.CENTER);
        title.setBounds(250, 40, 300, 40);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        // Username field
        JLabel lblUser = new JLabel("New Username");
        lblUser.setBounds(250, 120, 200, 20);
        lblUser.setForeground(Color.WHITE);
        tfRegUsername = new JTextField();
        tfRegUsername.setBounds(250, 145, 300, 30);

        // Employee ID field
        JLabel lblEmployee = new JLabel("Employee ID");
        lblEmployee.setBounds(250, 190, 200, 20);
        lblEmployee.setForeground(Color.WHITE);
        tfEmployeeID = new JTextField();
        tfEmployeeID.setBounds(250, 215, 300, 30);

        // Password field
        JLabel lblPass = new JLabel("New Password");
        lblPass.setBounds(250, 260, 200, 20);
        lblPass.setForeground(Color.WHITE);
        pfRegPassword = new JPasswordField();
        pfRegPassword.setBounds(250, 285, 300, 30);

        // Show password checkbox
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setBounds(250, 320, 200, 20);
        showPass.setForeground(Color.WHITE);
        showPass.setBackground(new Color(40, 40, 40));
        showPass.addActionListener(e -> pfRegPassword.setEchoChar(showPass.isSelected() ? (char) 0 : '•'));

        // Register button
        JButton btnRegister = new JButton("Register");
        btnRegister.setBounds(250, 380, 300, 35);
        btnRegister.setBackground(new Color(0xE4, 0x36, 0x36));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegister.addActionListener(e -> performRegister()); // Perform registration on click

        // Add components to panel
        panel.add(title);
        panel.add(lblUser);
        panel.add(tfRegUsername);
        panel.add(lblEmployee);
        panel.add(tfEmployeeID);
        panel.add(lblPass);
        panel.add(pfRegPassword);
        panel.add(showPass);
        panel.add(btnRegister);

        return panel;
    }

    // Database connection helper
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/payrolldb"; // DB URL
        String user = "root";                                  // DB username
        String pass = "1234";                                  // DB password
        return DriverManager.getConnection(url, user, pass);
    }

    // Handle user registration
    private void performRegister() {
        String username = tfRegUsername.getText().trim();
        String password = String.valueOf(pfRegPassword.getPassword()).trim();
        String empText = tfEmployeeID.getText().trim();

        // Validate inputs
        if (!InputValidation.validateUsername(username)) {
            return;
        }
        if (!InputValidation.validatePassword(password)) {
            return;
        }
        if (!InputValidation.validateEmployeeID(empText)) {
            return;
        }

        int employeeID = Integer.parseInt(empText);

        try (Connection conn = getConnection()) {
            // Check if Employee already has an account
            String checkSql = "SELECT COUNT(*) FROM USER WHERE EmployeeID=? AND Role='Employee'";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, employeeID);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This employee already has an account.");
                return;
            }

            // Check if username exists
            String checkUserSql = "SELECT COUNT(*) FROM USER WHERE Username=?";
            PreparedStatement psUser = conn.prepareStatement(checkUserSql);
            psUser.setString(1, username);
            ResultSet rsUser = psUser.executeQuery();
            if (rsUser.next() && rsUser.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists. Choose another.");
                return;
            }

            // Insert new user
            String insertSql = "INSERT INTO USER (EmployeeID, Username, Password, Role) VALUES (?, ?, ?, 'Employee')";
            PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setInt(1, employeeID);
            psInsert.setString(2, username);
            psInsert.setString(3, password);
            psInsert.executeUpdate();

            // Success message and reset fields
            JOptionPane.showMessageDialog(this, "Registration Successful!");
            tfRegUsername.setText("");
            tfEmployeeID.setText("");
            pfRegPassword.setText("");

            cardLayout.show(rightPanel, "LoginForm"); // Return to login

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error registering user: " + ex.getMessage(),
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Handle login with role check
    private void performLogin() {
        String username = tfUsername.getText().trim();
        String password = String.valueOf(pfPassword.getPassword()).trim();

        // Validate inputs
        if (!InputValidation.validateUsername(username)) {
            return;
        }
        if (!InputValidation.validatePassword(password)) {
            return;
        }

        // Authenticate user
        User user = User.authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password!",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Open dashboard based on role
        switch (user.getRole()) {
            case "Admin":
                new AdminDashboardMain(user.getRole(), user.getEmployeeID());
                dispose();
                break;
            case "Employee":
                new EmployeeDashboard(user.getEmployeeID());
                dispose();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role. Cannot proceed!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    // Main method: Launch UI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new); // Thread-safe UI creation
    }
}
