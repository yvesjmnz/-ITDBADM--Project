package com.neosburritos.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.neosburritos.model.User;
import com.neosburritos.util.DatabaseConnectionManager;

/**
 * Data Access Object for User operations
 * Demonstrates stored procedure calls for authentication and user management
 */
public class UserDAO {

    public UserDAO() {
        // No instance connection - each method manages its own connection lifecycle
    }

    /**
     * Authenticate user using sp_authenticate_user stored procedure
     */
    public AuthResult authenticate(String email, String password) {
        String sql = "{CALL sp_authenticate_user(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {
            // Set input parameters
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            // Register output parameters
            stmt.registerOutParameter(3, Types.INTEGER); // user_id
            stmt.registerOutParameter(4, Types.VARCHAR); // role
            stmt.registerOutParameter(5, Types.VARCHAR); // name
            stmt.registerOutParameter(6, Types.BOOLEAN); // success
            stmt.registerOutParameter(7, Types.VARCHAR); // message
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(6);
            String message = stmt.getString(7);
            
            if (success) {
                int userId = stmt.getInt(3);
                String roleStr = stmt.getString(4);
                String name = stmt.getString(5);
                
                User user = new User();
                user.setUserId(userId);
                user.setName(name);
                user.setEmail(email);
                user.setRole(User.Role.valueOf(roleStr));
                
                System.out.println("User authenticated successfully: " + email);
                return new AuthResult(true, message, user);
            } else {
                System.out.println("Authentication failed for user: " + email);
                return new AuthResult(false, message, null);
            }
            
        } catch (SQLException e) {
            System.err.println("Error during authentication for user: " + email + " - " + e.getMessage());
            return new AuthResult(false, "Database error during authentication", null);
        }
    }

    /**
     * Register new user using sp_register_user stored procedure
     */
    public RegisterResult register(String name, String email, String password, 
                                 User.Role role, String phone, String address) {
        String sql = "{CALL sp_register_user(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {
            // Set input parameters
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role.name());
            stmt.setString(5, phone);
            stmt.setString(6, address);
            
            // Register output parameters
            stmt.registerOutParameter(7, Types.INTEGER); // user_id
            stmt.registerOutParameter(8, Types.BOOLEAN); // success
            stmt.registerOutParameter(9, Types.VARCHAR); // message
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(8);
            String message = stmt.getString(9);
            Integer userId = success ? stmt.getInt(7) : null;
            
            if (success) {
                System.out.println("User registered successfully: " + email);
            } else {
                System.out.println("User registration failed: " + message);
            }
            
            return new RegisterResult(success, message, userId);
            
        } catch (SQLException e) {
            System.err.println("Error during user registration for email: " + email + " - " + e.getMessage());
            return new RegisterResult(false, "Database error during registration", null);
        }
    }

    /**
     * Update user profile using sp_update_user_profile stored procedure
     */
    public UpdateResult updateProfile(int userId, String name, String phone, String address) {
        String sql = "{CALL sp_update_user_profile(?, ?, ?, ?, ?, ?)}";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {
            // Set input parameters
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            
            // Register output parameters
            stmt.registerOutParameter(5, Types.BOOLEAN); // success
            stmt.registerOutParameter(6, Types.VARCHAR); // message
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(5);
            String message = stmt.getString(6);
            
            if (success) {
                System.out.println("Profile updated successfully for user ID: " + userId);
            } else {
                System.out.println("Profile update failed for user ID: " + userId);
            }
            
            return new UpdateResult(success, message);
            
        } catch (SQLException e) {
            System.err.println("Error updating profile for user ID: " + userId + " - " + e.getMessage());
            return new UpdateResult(false, "Database error during profile update");
        }
    }

    /**
     * Get all users for admin management
     */
    public java.util.List<User> getAllUsers() {
        String sql = "SELECT user_id, name, email, role, phone, address, is_active, created_at FROM users ORDER BY created_at DESC";
        java.util.List<User> users = new java.util.ArrayList<>();
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                users.add(user);
            }
            
            System.out.println("Retrieved " + users.size() + " users");
            return users;
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all users: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Update user status (active/inactive)
     */
    public UpdateResult updateUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                String message = "User status updated to " + (isActive ? "active" : "inactive");
                System.out.println(message + " for user ID: " + userId);
                return new UpdateResult(true, message);
            } else {
                String message = "User not found or no changes made";
                System.out.println(message + " for user ID: " + userId);
                return new UpdateResult(false, message);
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating user status for user ID: " + userId + " - " + e.getMessage());
            return new UpdateResult(false, "Database error during status update");
        }
    }

    /**
     * Update user role
     */
    public UpdateResult updateUserRole(int userId, User.Role newRole) {
        String sql = "UPDATE users SET role = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, newRole.name());
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                String message = "User role updated to " + newRole;
                System.out.println(message + " for user ID: " + userId);
                return new UpdateResult(true, message);
            } else {
                String message = "User not found or no changes made";
                System.out.println(message + " for user ID: " + userId);
                return new UpdateResult(false, message);
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating user role for user ID: " + userId + " - " + e.getMessage());
            return new UpdateResult(false, "Database error during role update");
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT user_id, name, email, role, phone, address, is_active, created_at FROM users WHERE user_id = ?";
        
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(User.Role.valueOf(rs.getString("role")));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setActive(rs.getBoolean("is_active"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return user;
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error retrieving user by ID: " + userId + " - " + e.getMessage());
            return null;
        }
    }

    // Result classes for clean return types
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    public static class RegisterResult {
        private final boolean success;
        private final String message;
        private final Integer userId;

        public RegisterResult(boolean success, String message, Integer userId) {
            this.success = success;
            this.message = message;
            this.userId = userId;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getUserId() { return userId; }
    }

    public static class UpdateResult {
        private final boolean success;
        private final String message;

        public UpdateResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = TRUE";
        try (Connection conn = DatabaseConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}