package com.neosburritos.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Test class to demonstrate the improved database connection management.
 * This replaces the erratic singleton pattern with proper resource management.
 */
public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Database Connection Management...");
        
        // Test 1: Basic connection test
        testBasicConnection();
        
        // Test 2: Multiple connections (simulating concurrent DAO usage)
        testMultipleConnections();
        
        // Test 3: Connection lifecycle management
        testConnectionLifecycle();
        
        System.out.println("Database connection tests completed.");
    }
    
    private static void testBasicConnection() {
        System.out.println("\n=== Test 1: Basic Connection ===");
        
        if (DatabaseConnectionManager.testConnection()) {
            System.out.println("✓ Database connection test passed");
        } else {
            System.out.println("✗ Database connection test failed");
        }
    }
    
    private static void testMultipleConnections() {
        System.out.println("\n=== Test 2: Multiple Connections ===");
        
        try {
            // Simulate multiple DAOs getting connections simultaneously
            Connection conn1 = DatabaseConnectionManager.getConnection();
            Connection conn2 = DatabaseConnectionManager.getConnection();
            Connection conn3 = DatabaseConnectionManager.getConnection();
            
            System.out.println("✓ Successfully created 3 concurrent connections");
            System.out.println("  Connection 1: " + conn1.hashCode());
            System.out.println("  Connection 2: " + conn2.hashCode());
            System.out.println("  Connection 3: " + conn3.hashCode());
            
            // Verify connections are independent
            if (conn1 != conn2 && conn2 != conn3 && conn1 != conn3) {
                System.out.println("✓ All connections are independent instances");
            } else {
                System.out.println("✗ Connections are not independent");
            }
            
            // Clean up
            conn1.close();
            conn2.close();
            conn3.close();
            System.out.println("✓ All connections closed successfully");
            
        } catch (SQLException e) {
            System.out.println("✗ Multiple connection test failed: " + e.getMessage());
        }
    }
    
    private static void testConnectionLifecycle() {
        System.out.println("\n=== Test 3: Connection Lifecycle ===");
        
        try {
            // Test proper try-with-resources pattern
            try (Connection conn = DatabaseConnectionManager.getConnection()) {
                System.out.println("✓ Connection created with try-with-resources");
                System.out.println("  Connection valid: " + conn.isValid(5));
                System.out.println("  Connection closed: " + conn.isClosed());
            } // Connection automatically closed here
            
            System.out.println("✓ Connection automatically closed by try-with-resources");
            
            // Test manual connection management
            Connection manualConn = DatabaseConnectionManager.getConnection();
            System.out.println("✓ Manual connection created");
            
            DatabaseConnectionManager.closeQuietly(manualConn);
            System.out.println("✓ Manual connection closed quietly");
            
        } catch (SQLException e) {
            System.out.println("✗ Connection lifecycle test failed: " + e.getMessage());
        }
    }
}