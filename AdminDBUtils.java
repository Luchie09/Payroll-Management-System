
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;

public class AdminDBUtils {

    // Database URL for connecting to the payroll DB
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/payrolldb";

    // MySQL credentials
    private static final String USER = "root";
    private static final String PASS = "1234";

    // Returns a live database connection using the preset credentials
    // Shows an error dialog and stops the program if the connection fails
    public static Connection getConnection() {
        try {
            // Attempts to establish a connection to MySQL
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            // Displays the failure message to the user
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());

            // Stops program execution due to critical DB failure
            System.exit(1);

            // Unreachable fallback return (kept for compilation completeness)
            return null;
        }
    }
}
