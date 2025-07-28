package com.neosburritos.ui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.neosburritos.util.DatabaseConnectionManager;

/**
 * System Statistics Dialog for Admin Panel
 * Displays total counts of users, products, and orders
 */
public class SystemStatisticsDialog extends JDialog {

    private final JFrame parentFrame;

    // UI Components
    private JLabel totalUsersLabel;
    private JLabel activeUsersLabel;
    private JLabel totalProductsLabel;
    private JLabel activeProductsLabel;
    private JLabel totalOrdersLabel;
    private JLabel pendingOrdersLabel;
    private JLabel completedOrdersLabel;
    private JLabel cancelledOrdersLabel;
    private JButton refreshButton;
    private JButton closeButton;

    public SystemStatisticsDialog(JFrame parent) {
        super(parent, "System Statistics", true);
        this.parentFrame = parent;

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadStatistics();

        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        totalUsersLabel = SwingUIConstants.createSecondaryLabel("Total Users: Loading...");
        activeUsersLabel = SwingUIConstants.createSecondaryLabel("Active Users: Loading...");
        totalProductsLabel = SwingUIConstants.createSecondaryLabel("Total Products: Loading...");
        activeProductsLabel = SwingUIConstants.createSecondaryLabel("Active Products: Loading...");
        totalOrdersLabel = SwingUIConstants.createSecondaryLabel("Total Orders: Loading...");
        pendingOrdersLabel = SwingUIConstants.createSecondaryLabel("Pending Orders: Loading...");
        completedOrdersLabel = SwingUIConstants.createSecondaryLabel("Completed Orders: Loading...");
        cancelledOrdersLabel = SwingUIConstants.createSecondaryLabel("Cancelled Orders: Loading...");

        refreshButton = SwingUIConstants.createSecondaryButton("Refresh");
        closeButton = SwingUIConstants.createPrimaryButton("Close");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM,
            SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM
        ));

        JLabel titleLabel = SwingUIConstants.createTitleLabel("System Statistics");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel instructionLabel = SwingUIConstants.createSecondaryLabel(
            "Real-time metrics of system usage and performance"
        );
        headerPanel.add(instructionLabel, BorderLayout.CENTER);

        headerPanel.add(refreshButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(8, 1, 10, 10));
        statsPanel.setBackground(SwingUIConstants.SURFACE_COLOR);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
            "Statistics Overview",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            SwingUIConstants.HEADER_FONT,
            SwingUIConstants.TEXT_PRIMARY
        ));

        statsPanel.add(totalUsersLabel);
        statsPanel.add(activeUsersLabel);
        statsPanel.add(totalProductsLabel);
        statsPanel.add(activeProductsLabel);
        statsPanel.add(totalOrdersLabel);
        statsPanel.add(pendingOrdersLabel);
        statsPanel.add(completedOrdersLabel);
        statsPanel.add(cancelledOrdersLabel);

        add(statsPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        footerPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        footerPanel.add(closeButton);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> loadStatistics());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadStatistics() {
        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int totalUsers, activeUsers, totalProducts, activeProducts, totalOrders, pendingOrders, completedOrders, cancelledOrders;

            @Override
            protected Void doInBackground() throws Exception {
                String userSQL = "SELECT COUNT(*) AS total, SUM(CASE WHEN is_active THEN 1 ELSE 0 END) AS active FROM users";
                String productSQL = "SELECT COUNT(*) AS total, SUM(CASE WHEN is_active THEN 1 ELSE 0 END) AS active FROM products";
                String orderSQL = "SELECT COUNT(*) AS total,\n                                SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending,\n                                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,\n                                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled FROM orders";

                try (Connection conn = DatabaseConnectionManager.getConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement(userSQL);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            totalUsers = rs.getInt("total");
                            activeUsers = rs.getInt("active");
                        }
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(productSQL);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            totalProducts = rs.getInt("total");
                            activeProducts = rs.getInt("active");
                        }
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(orderSQL);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            totalOrders = rs.getInt("total");
                            pendingOrders = rs.getInt("pending");
                            completedOrders = rs.getInt("completed");
                            cancelledOrders = rs.getInt("cancelled");
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                totalUsersLabel.setText("Total Users: " + totalUsers);
                activeUsersLabel.setText("Active Users: " + activeUsers);
                totalProductsLabel.setText("Total Products: " + totalProducts);
                activeProductsLabel.setText("Active Products: " + activeProducts);
                totalOrdersLabel.setText("Total Orders: " + totalOrders);
                pendingOrdersLabel.setText("Pending Orders: " + pendingOrders);
                completedOrdersLabel.setText("Completed Orders: " + completedOrders);
                cancelledOrdersLabel.setText("Cancelled Orders: " + cancelledOrders);

                refreshButton.setEnabled(true);
                refreshButton.setText("Refresh");
            }
        };

        worker.execute();
    }
}