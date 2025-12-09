
import javax.swing.JOptionPane;

public class InputValidation {

    // Helper method: Shows an error dialog for invalid inputs
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // Validates username if not empty, amd its length limit
    public static boolean validateUsername(String username) {
        if (isEmpty(username)) {      // Checks if null or blank
            showError("Username cannot be empty.");
            return false;
        }

        if (username.length() > 50) { // Ensures max allowed length
            showError("Username cannot exceed 50 characters.");
            return false;
        }

        return true;
    }

    // Validates username if not empty, amd its length limit
    public static boolean validatePassword(String password) {
        if (isEmpty(password)) {      // Checks if null or blank
            showError("Password cannot be empty.");
            return false;
        }

        if (password.length() > 20) { // Ensures max allowed length
            showError("Password cannot exceed 20 characters.");
            return false;
        }

        return true;
    }

    // Validates name if letters & spaces only, and its length limit
    public static boolean validatename(String firstname) {
        if (isEmpty(firstname)) {     // Required input
            showError("First name cannot be empty.");
            return false;
        }

        if (firstname.length() > 50) { // Ensures max allowed length
            showError("First name cannot exceed 50 characters.");
            return false;
        }

        if (!firstname.matches("[A-Za-z ]+")) { // Rejects numbers & symbols
            showError("First name can only contain letters and spaces.");
            return false;
        }

        return true;
    }

    // Validates Employee ID which must be numeric only, and has max 8 digits
    public static boolean validateEmployeeID(String employeeID) {
        if (isEmpty(employeeID)) {      // Required input
            showError("Employee ID cannot be empty.");
            return false;
        }

        if (!employeeID.matches("\\d+")) { // Must be numbers only
            showError("Employee ID must contain numbers only.");
            return false;
        }

        if (employeeID.length() > 8) { // Prevents very long IDs
            showError("Employee ID cannot exceed 8 digits.");
            return false;
        }

        return true;
    }

    // Validates email format using simplified regex
    public static boolean validateEmail(String email) {
        if (isEmpty(email)) {          // Required input
            showError("Email cannot be empty.");
            return false;
        }

        // Basic email pattern: local@domain
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showError("Invalid email format.");
            return false;
        }

        return true;
    }

    // Checks if a string is null or empty
    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
