
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminTimesheetPanel extends JPanel {

    private final Connection connection; // Database connection
    private final JTable table;           // Table to display timesheets

    public AdminTimesheetPanel(Connection connection) {
        super(new BorderLayout(12, 12));
        this.connection = connection;
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        buttonPanel.setOpaque(false);

        JButton addBtn = createAccentButton("Add Timesheet");
        JButton editBtn = createAccentButton("Edit Timesheet");
        JButton deleteBtn = createAccentButton("Delete Timesheet");
        JButton refreshBtn = createModernButton("Refresh");

        addBtn.addActionListener(e -> showTimesheetDialog("Add", null));
        editBtn.addActionListener(e -> editSelectedTimesheet());
        deleteBtn.addActionListener(e -> deleteSelectedTimesheet());
        refreshBtn.addActionListener(e -> loadTimesheetData());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        // TABLE SETUP
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));
        scrollPane.setPreferredSize(new Dimension(800, 350));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load initial timesheet data
        loadTimesheetData();
    }

    // Load timesheet data from DB
    private void loadTimesheetData() {
        try {
            String query = "SELECT t.TimesheetID, t.EmployeeID, e.FirstName, e.LastName, t.WorkDate, "
                    + "t.StandardHours, t.RenderedHours, t.OvertimeHours "
                    + "FROM TIMESHEET t JOIN EMPLOYEE e ON t.EmployeeID = e.EmployeeID "
                    + "ORDER BY t.TimesheetID";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            Vector<String> columnNames = new Vector<>();
            columnNames.add("TimesheetID");
            columnNames.add("EmployeeID");
            columnNames.add("FirstName");
            columnNames.add("LastName");
            columnNames.add("WorkDate");
            columnNames.add("StandardHours");
            columnNames.add("RenderedHours");
            columnNames.add("OvertimeHours");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("TimesheetID"));
                row.add(rs.getInt("EmployeeID"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getDate("WorkDate"));
                row.add(rs.getBigDecimal("StandardHours"));
                row.add(rs.getBigDecimal("RenderedHours"));
                row.add(rs.getBigDecimal("OvertimeHours"));
                data.add(row);
            }

            table.setModel(new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table non-editable
                }
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading timesheet data: " + e.getMessage());
        }
    }

    // Show Add/Edit Timesheet Dialog
    private void showTimesheetDialog(String action, Integer timesheetId) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField employeeIdField = new JTextField(10);
        JTextField workDateField = new JTextField("YYYY-MM-DD");
        JTextField renderedHoursField = new JTextField();

        // Pre-fill fields if editing
        if ("Edit".equals(action) && timesheetId != null) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                employeeIdField.setText(String.valueOf(table.getValueAt(selectedRow, 1)));
                workDateField.setText(table.getValueAt(selectedRow, 4).toString());
                renderedHoursField.setText(table.getValueAt(selectedRow, 6).toString());
            }
        }

        // Add labels and fields to panel
        String[] labels = {"Employee ID", "Work Date (YYYY-MM-DD)", "Rendered Hours"};
        Component[] fields = {employeeIdField, workDateField, renderedHoursField};
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

        // Buttons for dialog
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setOpaque(false);
        JButton okBtn = createAccentButton(action.equals("Add") ? "Add" : "Save");
        JButton cancelBtn = createModernButton("Cancel");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        okBtn.addActionListener(e -> {
            String empIdStr = employeeIdField.getText().trim();
            String workDate = workDateField.getText().trim();
            String renderedHoursStr = renderedHoursField.getText().trim();

            if (empIdStr.isEmpty() || workDate.isEmpty() || renderedHoursStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            int empId;
            double renderedHours;

            try {
                empId = Integer.parseInt(empIdStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Employee ID must be a number.");
                return;
            }

            try {
                renderedHours = Double.parseDouble(renderedHoursStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Rendered Hours must be a number.");
                return;
            }

            try {
                // Validate EmployeeID exists
                PreparedStatement stmtCheckEmp = connection.prepareStatement(
                        "SELECT 1 FROM EMPLOYEE WHERE EmployeeID = ?");
                stmtCheckEmp.setInt(1, empId);
                ResultSet rsCheckEmp = stmtCheckEmp.executeQuery();
                if (!rsCheckEmp.next()) {
                    JOptionPane.showMessageDialog(this, "Employee ID does not exist.");
                    return;
                }

                // Prevent duplicate timesheets
                PreparedStatement stmtCheckDup;
                if ("Edit".equals(action) && timesheetId != null) {
                    stmtCheckDup = connection.prepareStatement(
                            "SELECT 1 FROM TIMESHEET WHERE EmployeeID = ? AND WorkDate = ? AND TimesheetID <> ?");
                    stmtCheckDup.setInt(1, empId);
                    stmtCheckDup.setString(2, workDate);
                    stmtCheckDup.setInt(3, timesheetId);
                } else {
                    stmtCheckDup = connection.prepareStatement(
                            "SELECT 1 FROM TIMESHEET WHERE EmployeeID = ? AND WorkDate = ?");
                    stmtCheckDup.setInt(1, empId);
                    stmtCheckDup.setString(2, workDate);
                }

                ResultSet rsDup = stmtCheckDup.executeQuery();
                if (rsDup.next()) {
                    JOptionPane.showMessageDialog(this,
                            "A timesheet for this employee on this date already exists.");
                    return;
                }

                if ("Add".equals(action)) {
                    PreparedStatement stmt = connection.prepareStatement(
                            "INSERT INTO TIMESHEET (EmployeeID, WorkDate, RenderedHours) VALUES (?, ?, ?)");
                    stmt.setInt(1, empId);
                    stmt.setString(2, workDate);
                    stmt.setDouble(3, renderedHours);
                    stmt.executeUpdate();
                } else {
                    int tsId = timesheetId != null ? timesheetId : (int) table.getValueAt(table.getSelectedRow(), 0);
                    PreparedStatement stmt = connection.prepareStatement(
                            "UPDATE TIMESHEET SET EmployeeID = ?, WorkDate = ?, RenderedHours = ? WHERE TimesheetID = ?");
                    stmt.setInt(1, empId);
                    stmt.setString(2, workDate);
                    stmt.setDouble(3, renderedHours);
                    stmt.setInt(4, tsId);
                    stmt.executeUpdate();
                }

                loadTimesheetData();  // Refresh table
                SwingUtilities.getWindowAncestor(okBtn).dispose();  // CLOSE DIALOG

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saving timesheet: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> SwingUtilities.getWindowAncestor(panel).dispose());

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), action + " Timesheet", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(30, 30, 30));
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Edit selected timesheet
    private void editSelectedTimesheet() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a timesheet to edit.");
            return;
        }
        int timesheetId = (int) table.getValueAt(selectedRow, 0);
        showTimesheetDialog("Edit", timesheetId);
    }

    // Delete selected timesheet
    private void deleteSelectedTimesheet() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a timesheet to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected timesheet?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int tsId = (int) table.getValueAt(selectedRow, 0);
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM TIMESHEET WHERE TimesheetID = ?");
            stmt.setInt(1, tsId);
            stmt.executeUpdate();
            loadTimesheetData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting timesheet: " + e.getMessage());
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
