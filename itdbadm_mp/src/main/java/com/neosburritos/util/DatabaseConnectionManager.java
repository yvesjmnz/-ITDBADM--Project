package com.neosburritos.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection manager that provides clean connection lifecycle management.
 * Eliminates singleton anti-pattern and provides proper resource management.
 */
public class DatabaseConnectionManager {
    
    private static final String URL = "jdbc:mysql://localhost:3306/neos_burritos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "@Sponeoiscool1";
    
    private static final Properties CONNECTION_PROPS;
    
    static {
        // Load MySQL JDBC driver once
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
        
        // Initialize connection properties
        CONNECTION_PROPS = new Properties();
        CONNECTION_PROPS.setProperty("user", USERNAME);
        CONNECTION_PROPS.setProperty("password", PASSWORD);
        CONNECTION_PROPS.setProperty("useSSL", "false");
        CONNECTION_PROPS.setProperty("allowPublicKeyRetrieval", "true");
        CONNECTION_PROPS.setProperty("serverTimezone", "UTC");
        CONNECTION_PROPS.setProperty("autoReconnect", "true");
        CONNECTION_PROPS.setProperty("maxReconnects", "3");
    }
    
    /**
     * Creates a new database connection.
     * Caller is responsible for closing the connection.
     * 
     * @return A new database connection
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(URL, CONNECTION_PROPS);
            connection.setAutoCommit(true); // Explicit auto-commit for clarity
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Tests database connectivity by creating and immediately closing a connection.
     * 
     * @return true if connection can be established, false otherwise
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Safely closes a connection, ignoring any exceptions.
     * 
     * @param connection the connection to close (can be null)
     */
    public static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}