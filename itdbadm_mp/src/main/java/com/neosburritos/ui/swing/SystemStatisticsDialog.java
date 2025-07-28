package com.neosburritos.ui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import com.neosburritos.dao.OrderDAO;
import com.neosburritos.dao.ProductDAO;
import com.neosburritos.dao.UserDAO;

/**
 * System Statistics Dialog for Admin Panel
 * Displays total counts of users, products, and orders
 */
public class SystemStatisticsDialog extends JDialog {

    private final JFrame parentFrame;
    private final UserDAO userDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;

    private JTable statsTable;
    private StatsTableModel tableModel;
    private JButton refreshButton;
    private JButton closeButton;

    private List<StatMetric> stats;

    public SystemStatisticsDialog(JFrame parent, UserDAO userDAO, ProductDAO productDAO, OrderDAO orderDAO) {
        super(parent, "System Statistics", true);
        this.parentFrame = parent;
        this.userDAO = userDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadStats();

        setSize(900, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        tableModel = new StatsTableModel();
        statsTable = new JTable(tableModel);
        statsTable.setFont(SwingUIConstants.BODY_FONT);
        statsTable.setRowHeight(30);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        statsTable.setBackground(SwingUIConstants.SURFACE_COLOR);

        statsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        statsTable.getColumnModel().getColumn(1).setPreferredWidth(100);

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
                "Overview of system usage metrics"
        );
        headerPanel.add(instructionLabel, BorderLayout.CENTER);

        headerPanel.add(refreshButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SwingUIConstants.BORDER_COLOR),
                "Metrics Overview",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                SwingUIConstants.HEADER_FONT,
                SwingUIConstants.TEXT_PRIMARY
        ));
        add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SwingUIConstants.PADDING_MEDIUM, SwingUIConstants.PADDING_MEDIUM));
        footerPanel.setBackground(SwingUIConstants.BACKGROUND_COLOR);
        footerPanel.add(closeButton);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> loadStats());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadStats() {
        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");

        SwingWorker<List<StatMetric>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StatMetric> doInBackground() throws Exception {
                List<StatMetric> data = new ArrayList<>();
                data.add(new StatMetric("Total Users", userDAO.countUsers()));
                data.add(new StatMetric("Active Users", userDAO.countActiveUsers()));
                data.add(new StatMetric("Total Products", productDAO.countProducts()));
                data.add(new StatMetric("Active Products", productDAO.countActiveProducts()));
                data.add(new StatMetric("Total Orders", orderDAO.countOrders()));
                data.add(new StatMetric("Pending Orders", orderDAO.countOrdersByStatus("PENDING")));
                data.add(new StatMetric("Completed Orders", orderDAO.countOrdersByStatus("COMPLETED")));
                data.add(new StatMetric("Cancelled Orders", orderDAO.countOrdersByStatus("CANCELLED")));
                return data;
            }

            @Override
            protected void done() {
                try {
                    stats = get();
                    tableModel.setStats(stats);
                } catch (Exception e) {
                    SwingUIConstants.showErrorDialog(SystemStatisticsDialog.this,
                            "Failed to load system statistics: " + e.getMessage(),
                            "Load Error");
                } finally {
                    refreshButton.setEnabled(true);
                    refreshButton.setText("Refresh");
                }
            }
        };

        worker.execute();
    }

    private static class StatsTableModel extends AbstractTableModel {
        private final String[] columnNames = { "Metric", "Value" };
        private List<StatMetric> stats = new ArrayList<>();

        public void setStats(List<StatMetric> stats) {
            this.stats = stats;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return stats.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StatMetric stat = stats.get(rowIndex);
            return columnIndex == 0 ? stat.name() : stat.value();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 1 ? Integer.class : String.class;
        }
    }

    private record StatMetric(String name, int value) { }
} 