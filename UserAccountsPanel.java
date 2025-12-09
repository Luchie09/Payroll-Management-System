
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class UserAccountsPanel extends JPanel {

    private final Connection connection; // Database connection
    private final JTable table;          // Table to display employee users
    private JTextField searchField;      // Search field for filtering users

    public UserAccountsPanel(Connection connection) {
        super(new BorderLayout(12, 12));
        this.connection = connection;
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // SEARCH BAR
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search by Username or Employee Name:"));
        searchField = new JTextField(22);
        JButton searchBtn = createModernButton("Go");
        searchBtn.addActionListener(e -> loadUserAccounts(searchField.getText().trim()));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        add(searchPanel, BorderLayout.NORTH);

        // TOP BUTTON PANEL
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        topBar.setOpaque(false);
        JButton addBtn = createModernButton("Add User");
        JButton editBtn = createModernButton("Edit User");
        JButton deleteBtn = createModernButton("Delete User");
        JButton refreshBtn = createModernButton("Refresh");
        topBar.add(addBtn);
        topBar.add(editBtn);
        topBar.add(deleteBtn);
        topBar.add(refreshBtn);
        add(topBar, BorderLayout.BEFORE_FIRST_LINE);

        // TABLE SETUP
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);

        // Initialize empty table
        table.setModel(new DefaultTableModel(
                new String[]{"UserID", "EmployeeID", "Employee Name", "Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing in table cells
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setPreferredSize(new Dimension(800, 350));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // BUTTON ACTIONS
        addBtn.addActionListener(e -> showUserDialog("Add", null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                Object idObj = table.getValueAt(r, 0);
                if (idObj instanceof Number) {
                    showUserDialog("Edit", ((Number) idObj).intValue());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a user to edit.");
            }
        });
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadUserAccounts("");
        });

        // Load initial user data
        loadUserAccounts("");
    }

    // Load Employee Users from DB
    private void loadUserAccounts(String search) {
        String query = "SELECT u.UserID, u.EmployeeID, CONCAT(e.FirstName,' ',e.LastName) AS EmployeeName, u.Username, u.Role "
                + "FROM USER u JOIN EMPLOYEE e ON u.EmployeeID = e.EmployeeID "
                + "WHERE u.Role='Employee' ";
        if (search != null && !search.isEmpty()) {
            query += "AND (u.Username LIKE ? OR e.FirstName LIKE ? OR e.LastName LIKE ?) ";
        }
        query += "ORDER BY u.UserID";

        Vector<String> columnNames = new Vector<>();
        columnNames.add("UserID");
        columnNames.add("EmployeeID");
        columnNames.add("Employee Name");
        columnNames.add("Username");
        columnNames.add("Role");

        Vector<Vector<Object>> data = new Vector<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            // Bind search values if provided
            if (search != null && !search.isEmpty()) {
                String s = "%" + search + "%";
                ps.setString(1, s);
                ps.setString(2, s);
                ps.setString(3, s);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("UserID"));
                row.add(rs.getInt("EmployeeID"));
                row.add(rs.getString("EmployeeName"));
                row.add(rs.getString("Username"));
                row.add(rs.getString("Role"));
                data.add(row);
            }

            // Update table with results
            table.setModel(new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Table cells remain non-editable
                }
            });

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage());
        }
    }

    // Add/Edit User Dialog
    private void showUserDialog(String action, Integer userID) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                action + " User", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(30, 30, 30));

        // INPUT PANEL
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField employeeIdField = new JTextField(10);
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        // Load existing data if editing
        if ("Edit".equals(action) && userID != null) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM USER WHERE UserID=? AND Role='Employee'")) {
                ps.setInt(1, userID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    employeeIdField.setText(String.valueOf(rs.getInt("EmployeeID")));
                    usernameField.setText(rs.getString("Username"));
                } else {
                    JOptionPane.showMessageDialog(this, "User not found or not an Employee.");
                    dialog.dispose();
                    return;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading user: " + ex.getMessage());
                dialog.dispose();
                return;
            }
        }

        // Add labels and fields to panel
        String[] labels = {"Employee ID", "Username", "Password"};
        Component[] fields = {employeeIdField, usernameField, passwordField};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.25;
            JLabel lbl = new JLabel(labels[i] + ":");
            lbl.setForeground(Color.WHITE);
            panel.add(lbl, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.75;
            panel.add(fields[i], gbc);
        }

        // BUTTON PANEL
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setOpaque(false);
        JButton okBtn = createAccentButton(action.equals("Add") ? "Add" : "Save");
        JButton cancelBtn = createModernButton("Cancel");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        // OK BUTTON LOGIC
        okBtn.addActionListener(e -> {
            String uname = usernameField.getText().trim();
            String pwd = new String(passwordField.getPassword()).trim();
            int empID;

            if (uname.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username is required");
                return;
            }

            try {
                empID = Integer.parseInt(employeeIdField.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Employee ID must be a number");
                return;
            }

            try {
                if ("Add".equals(action)) {
                    // Check duplicate EmployeeID
                    String checkSql = "SELECT COUNT(*) FROM USER WHERE EmployeeID=? AND Role='Employee'";
                    PreparedStatement checkPs = connection.prepareStatement(checkSql);
                    checkPs.setInt(1, empID);
                    ResultSet rsCheck = checkPs.executeQuery();
                    if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(dialog, "This employee already has a user account.");
                        return;
                    }

                    if (pwd.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Password is required for new user.");
                        return;
                    }

                    // Insert new employee user
                    String sql = "INSERT INTO USER (EmployeeID, Username, Password, Role) VALUES (?, ?, ?, 'Employee')";
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setInt(1, empID);
                    ps.setString(2, uname);
                    ps.setString(3, pwd);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "User added");

                } else { // Edit
                    if (pwd.isEmpty()) {
                        String sql = "UPDATE USER SET EmployeeID=?, Username=? WHERE UserID=? AND Role='Employee'";
                        PreparedStatement ps = connection.prepareStatement(sql);
                        ps.setInt(1, empID);
                        ps.setString(2, uname);
                        ps.setInt(3, userID);
                        ps.executeUpdate();
                    } else {
                        String sql = "UPDATE USER SET EmployeeID=?, Username=?, Password=? WHERE UserID=? AND Role='Employee'";
                        PreparedStatement ps = connection.prepareStatement(sql);
                        ps.setInt(1, empID);
                        ps.setString(2, uname);
                        ps.setString(3, pwd);
                        ps.setInt(4, userID);
                        ps.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "User updated");
                }
                dialog.dispose();
                loadUserAccounts("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving user: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Delete Employee User
    private void deleteUser() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first");
            return;
        }
        int userID = (int) table.getValueAt(sel, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM USER WHERE UserID=? AND Role='Employee'")) {
            ps.setInt(1, userID);
            ps.executeUpdate();
            loadUserAccounts(""); // Refresh table
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage());
        }
    }

    // Create modern styled button
    private JButton createModernButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setBackground(new Color(60, 63, 65));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(85, 90, 92));
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(60, 63, 65));
            }
        });
        return b;
    }

    // Create accent button for important actions
    private JButton createAccentButton(String text) {
        JButton b = createModernButton(text);
        b.setBackground(new Color(200, 120, 0));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(220, 140, 20));
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(200, 120, 0));
            }
        });
        return b;
    }
}
