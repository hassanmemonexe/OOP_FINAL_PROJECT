package supermarket.functionality; // Moved to functionality package

/**
 * Represents a user of the system (Admin or Seller).
 * Updated for new package structure.
 */
public class User {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String toCSVString() {
        return username + "," + password + "," + role;
    }

    public static User fromCSVString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvString.split(",");
        if (parts.length == 3) {
            return new User(parts[0].trim(), parts[1].trim(), parts[2].trim());
        }
        System.err.println("Invalid CSV string for User (expected 3 parts): " + csvString);
        return null;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
