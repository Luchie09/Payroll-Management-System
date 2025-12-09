
import java.sql.*;

public class User {

    private int userID;
    private int employeeID;
    private String username;
    private String role;

    // Database connection info
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/payrolldb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    // Constructors
    public User() {
        // Default constructor
    }

    public User(int userID, int employeeID, String username, String role) {
        this.userID = userID;
        this.employeeID = employeeID;
        this.username = username;
        this.role = role;
    }

    // Getters
    public int getUserID() {
        return userID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // String representation
    @Override
    public String toString() {
        return "User{userID=" + userID
                + ", employeeID=" + employeeID
                + ", username='" + username + '\''
                + ", role='" + role + '\'' + '}';
    }

    // Authenticate user by username and password
    // Returns User object if valid, null if invalid
    public static User authenticate(String username, String password) {
        User user = null;
        String query = "SELECT UserID, EmployeeID, Username, Role FROM USER WHERE Username=? AND Password=?";

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username); // Bind username
            ps.setString(2, password); // Bind password

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // If match found
                    user = new User(
                            rs.getInt("UserID"),
                            rs.getInt("EmployeeID"),
                            rs.getString("Username"),
                            rs.getString("Role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log DB errors
        }

        return user; // Null if authentication failed
    }

    // Check if username exists
    // Returns true if exists, false otherwise
    private static boolean usernameExists(String username) {
        String query = "SELECT 1 FROM USER WHERE Username=?";

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username); // Bind username
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // True if any row exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Fail-safe: assume exists on error
        }
    }

    // Check if password exists
    // Returns true if exists, false otherwise
    private static boolean passwordExists(String password) {
        String query = "SELECT 1 FROM USER WHERE Password=?";

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, password); // Bind password
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // True if any row exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Fail-safe: assume exists on error
        }
    }

    // Register a new user
    // Returns a String message indicating result
    public static String register(int employeeID, String username, String password, String role) {

        // Prevent duplicate usernames
        if (usernameExists(username)) {
            return "Username already exists";
        }

        // Prevent duplicate passwords (optional security policy)
        if (passwordExists(password)) {
            return "Password already exists";
        }

        String query = "INSERT INTO USER (EmployeeID, Username, Password, Role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, employeeID); // Bind employee ID
            ps.setString(2, username); // Bind username
            ps.setString(3, password); // Bind password
            ps.setString(4, role);     // Bind role

            int rows = ps.executeUpdate(); // Execute insert
            return (rows > 0) ? "Success" : "Registration failed";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage(); // Return DB error
        }
    }
}
