package com.finance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FinanceAppUI extends JFrame {
    private LedgerService currentEngine = new LinearLedgerServiceImpl();
    
    private List<Expense> filteredList = new ArrayList<>();
    private int currentPage = 1;
    private final int PAGE_SIZE = 50;
    
    // UI Elements - Tab 1 (Form & Totals)
    private final JTextField amountField = new JTextField(10);
    private final JTextField dateField = new JTextField(10);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"});
    private final JTextArea summaryArea = new JTextArea(12, 22);

    // UI Elements - Tab 1 (Filters & Table)
    private final JComboBox<String> filterCategoryBox = new JComboBox<>(new String[]{"ALL", "GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"});
    private final JTextField filterStartField = new JTextField("2020-01-01", 7);
    private final JTextField filterEndField = new JTextField("2026-12-31", 7);
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Date", "Category", "Amount ($)"}, 0);
    private final JTable expenseTable = new JTable(tableModel);
    private final JLabel pageIndicatorLabel = new JLabel("Page 1 of 1");
    private final JButton prevPageBtn = new JButton("<- Previous");
    private final JButton nextPageBtn = new JButton("Next ->");

    // UI Elements - Tab 2 (Testing Controls)
    private final JTextField seedField = new JTextField("42", 6);
    private final JTextArea logArea = new JTextArea(12, 45);
    private final JLabel engineLabel = new JLabel("Active Engine: ArrayList + HashMap (Solution 1)");

    public FinanceAppUI() {
        setTitle("Finance Tracker Assignment Project");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        dateField.setText(LocalDate.now().toString());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📱 Main App Dashboard", createNormalAppPanel());
        tabbedPane.addTab("⚡ Testing & Speed Check", createDevPanel());
        add(tabbedPane);
        
        updateSummaryAndPredictions();
        applyFiltersAndRefreshTable();
    }

    private JPanel createNormalAppPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Add New Expense"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; leftPanel.add(new JLabel("Amount ($):"), gbc);
        gbc.gridx = 1; leftPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; leftPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; leftPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; leftPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; leftPanel.add(categoryBox, gbc);

        JButton addBtn = new JButton("Add Expense");
        addBtn.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        leftPanel.add(addBtn, gbc);
        
        summaryArea.setEditable(false);
        summaryArea.setBackground(new Color(245, 247, 250));
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridy = 4; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(new JScrollPane(summaryArea), gbc);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("History Table"));

        JPanel searchFilterBar = new JPanel(new GridBagLayout());
        GridBagConstraints sfGbc = new GridBagConstraints();
        sfGbc.insets = new Insets(5, 4, 5, 4);
        sfGbc.fill = GridBagConstraints.HORIZONTAL;

        sfGbc.gridx = 0; searchFilterBar.add(new JLabel("Category:"), sfGbc);
        sfGbc.gridx = 1; searchFilterBar.add(filterCategoryBox, sfGbc);
        sfGbc.gridx = 2; searchFilterBar.add(new JLabel(" From:"), sfGbc);
        sfGbc.gridx = 3; searchFilterBar.add(filterStartField, sfGbc);
        sfGbc.gridx = 4; searchFilterBar.add(new JLabel(" To:"), sfGbc);
        sfGbc.gridx = 5; searchFilterBar.add(filterEndField, sfGbc);
        
        JButton applyFilterBtn = new JButton("🔍 Search / Filter");
        applyFilterBtn.setForeground(Color.BLACK);
        sfGbc.gridx = 6; sfGbc.weightx = 1.0; searchFilterBar.add(applyFilterBtn, sfGbc);
        
        rightPanel.add(searchFilterBar, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(expenseTable), BorderLayout.CENTER);

        JPanel paginationBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        paginationBar.add(prevPageBtn);
        paginationBar.add(pageIndicatorLabel);
        paginationBar.add(nextPageBtn);
        rightPanel.add(paginationBar, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        addBtn.addActionListener(e -> handleManualInput());
        applyFilterBtn.addActionListener(e -> {
            currentPage = 1;
            applyFiltersAndRefreshTable();
        });
        prevPageBtn.addActionListener(e -> { if (currentPage > 1) { currentPage--; renderTablePage(); } });
        nextPageBtn.addActionListener(e -> { if (currentPage < getTotalPages()) { currentPage++; renderTablePage(); } });

        return mainPanel;
    }

    private JPanel createDevPanel() {
        JPanel devPanel = new JPanel(new BorderLayout(10, 10));
        devPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        engineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(engineLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton toggleBtn = new JButton("Switch Data Structure");
        
        buttonPanel.add(new JLabel("Seed Number:"));
        buttonPanel.add(seedField);
        
        JButton seedBtn = new JButton("Generate 200k Test Data");
        JButton clearBtn = new JButton("Delete All Data");
        JButton profileBtn = new JButton("Check Method Speeds");
        profileBtn.setForeground(Color.BLACK);
        
        buttonPanel.add(toggleBtn);
        buttonPanel.add(seedBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(profileBtn);
        controlPanel.add(buttonPanel);

        devPanel.add(controlPanel, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        devPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        toggleBtn.addActionListener(e -> {
            List<Expense> temporaryDataHold = currentEngine.getAllExpenses();
            if (currentEngine instanceof LinearLedgerServiceImpl) {
                currentEngine = new TreeLedgerServiceImpl();
                engineLabel.setText("Active Engine: TreeSet + TreeMap (Solution 2)");
            } else {
                currentEngine = new LinearLedgerServiceImpl();
                engineLabel.setText("Active Engine: ArrayList + HashMap (Solution 1)");
            }
            for (Expense exp : temporaryDataHold) {
                currentEngine.addExpense(exp);
            }
            logArea.append("🔄 Data structure swapped. Copied " + temporaryDataHold.size() + " items over to the new engine.\n");
            applyFiltersAndRefreshTable();
            updateSummaryAndPredictions();
        });

        seedBtn.addActionListener(e -> {
            long seedValue;
            try {
                seedValue = Long.parseLong(seedField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please type a valid integer number for the seed.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            logArea.append("⚙️ Generating and inserting 200,000 random expenses...\n");
            
            Timer timer = new Timer(50, event -> {
                long start = System.currentTimeMillis();
                LocalDate startDate = LocalDate.of(2020, 1, 1);
                String[] categories = {"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"};
                Random seededRand = new Random(seedValue);

                for (int i = 0; i < 200_000; i++) {
                    long randomDays = (long) (seededRand.nextDouble() * (365 * 5));
                    double amount = 5.0 + (seededRand.nextDouble() * 495.0);
                    String cat = categories[seededRand.nextInt(categories.length)];

                    currentEngine.addExpense(new Expense(
                            UUID.nameUUIDFromBytes(("id_" + seedValue + "_" + i).getBytes()).toString().substring(0, 8),
                            Math.round(amount * 100.0) / 100.0,
                            startDate.plusDays(randomDays),
                            cat
                    ));
                }
                long end = System.currentTimeMillis();
                logArea.append("➔ [addExpense()] Finished adding 200,000 items. Time taken: " + (end - start) + " ms.\n\n");

                applyFiltersAndRefreshTable();
                updateSummaryAndPredictions();
            });
            timer.setRepeats(false);
            timer.start();
        });

        clearBtn.addActionListener(e -> {
            currentEngine.clear();
            filteredList.clear();
            currentPage = 1;
            applyFiltersAndRefreshTable();
            updateSummaryAndPredictions();
            logArea.append("🗑️ Cleaned memory. All items have been deleted.\n");
        });

        profileBtn.addActionListener(e -> {
            logArea.append("📋 TESTING METHOD RUNTIMES...\n");
            String[] categories = {"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"};
            
            // 1. category total retrieval
            long startCatNano = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                for (String cat : categories) {
                    currentEngine.getTotalByCategory(cat);
                }
            }
            long endCatNano = System.nanoTime();
            double avgCatTime = ((endCatNano - startCatNano) / 1000.0) / 1_000_000.0;
            logArea.append(String.format("➔ [getTotalByCategory()] Time to fetch category sums: %.4f ms\n", avgCatTime));

            // 2. data range slicing
            LocalDate searchStart = LocalDate.of(2022, 1, 1);
            LocalDate searchEnd = LocalDate.of(2022, 3, 1);
            
            long startRangeNano = System.nanoTime();
            List<Expense> rangeResults = currentEngine.getExpensesInDateRange(searchStart, searchEnd);
            long endRangeNano = System.nanoTime();
            double rangeTime = (endRangeNano - startRangeNano) / 1_000_000.0;
            logArea.append(String.format("➔ [getExpensesInDateRange()] Found %d items between dates in: %.4f ms\n", rangeResults.size(), rangeTime));

            // 3. entire memory extraction
            long startAllNano = System.nanoTime();
            List<Expense> allResults = currentEngine.getAllExpenses();
            long endAllNano = System.nanoTime();
            double allTime = (endAllNano - startAllNano) / 1_000_000.0;
            logArea.append(String.format("➔ [getAllExpenses()] Retrieved all %d list entries in: %.4f ms\n\n", allResults.size(), allTime));
        });

        return devPanel;
    }

    private void updateSummaryAndPredictions() {
        String[] categories = {"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"};
        List<Expense> allExpenses = currentEngine.getAllExpenses();
        
        StringBuilder sb = new StringBuilder("=== TOTAL SPENT ===\n");
        for (String cat : categories) {
            double total = currentEngine.getTotalByCategory(cat);
            sb.append(String.format("%-14s: $%.2f\n", cat, total));
        }

        sb.append("\n=== PREDICTION ===\n");
        
        if (allExpenses.isEmpty()) {
            sb.append("Awaiting data inputs to calculate trends...");
            summaryArea.setText(sb.toString());
            return;
        }

        LocalDate minDate = LocalDate.MAX;
        for (Expense e : allExpenses) {
            if (e.getDate().isBefore(minDate)) minDate = e.getDate();
        }
        LocalDate baseMonth = minDate.withDayOfMonth(1);
        
        Map<String, Map<Long, Double>> historicalGrid = new HashMap<>();
        for (String cat : categories) historicalGrid.put(cat, new HashMap<>());

        long maxMonthIndex = 0;
        for (Expense e : allExpenses) {
            long monthIndex = ChronoUnit.MONTHS.between(baseMonth, e.getDate().withDayOfMonth(1));
            if (monthIndex > maxMonthIndex) maxMonthIndex = monthIndex;
            
            Map<Long, Double> monthMap = historicalGrid.get(e.getCategory());
            monthMap.put(monthIndex, monthMap.getOrDefault(monthIndex, 0.0) + e.getAmount());
        }

        long targetNextMonth = maxMonthIndex + 1;
        
        for (String cat : categories) {
            Map<Long, Double> points = historicalGrid.get(cat);
            
            if (points.size() <= 1) {
                double fallbackSum = 0;
                for (double val : points.values()) fallbackSum += val;
                double predictedValue = points.isEmpty() ? 0.0 : fallbackSum / points.size();
                sb.append(String.format("%-14s: $%.2f\n", cat, predictedValue));
                continue;
            }

            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            int n = points.size();
            
            for (Map.Entry<Long, Double> entry : points.entrySet()) {
                long x = entry.getKey();
                double y = entry.getValue();
                sumX += x;
                sumY += y;
                sumXY += (x * y);
                sumX2 += (x * x);
            }

            double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            double intercept = (sumY - slope * sumX) / n;

            double predictedExpenditure = (slope * targetNextMonth) + intercept;
            if (predictedExpenditure < 0) predictedExpenditure = 0.0;

            sb.append(String.format("%-14s: $%.2f\n", cat, predictedExpenditure));
        }
        
        summaryArea.setText(sb.toString());
    }

    private void applyFiltersAndRefreshTable() {
        try {
            LocalDate start = LocalDate.parse(filterStartField.getText().trim());
            LocalDate end = LocalDate.parse(filterEndField.getText().trim());
            String selectedCategory = (String) filterCategoryBox.getSelectedItem();

            List<Expense> rangeResults = currentEngine.getExpensesInDateRange(start, end);
            filteredList = new ArrayList<>();
            for (Expense e : rangeResults) {
                if (selectedCategory.equals("ALL") || e.getCategory().equalsIgnoreCase(selectedCategory)) {
                    filteredList.add(e);
                }
            }
            renderTablePage();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Filter fields must use proper date format YYYY-MM-DD.", "Filter Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderTablePage() {
        tableModel.setRowCount(0);
        if (filteredList.isEmpty()) {
            pageIndicatorLabel.setText("Page 1 of 1");
            prevPageBtn.setEnabled(false); nextPageBtn.setEnabled(false);
            return;
        }
        int totalPages = getTotalPages();
        if (currentPage > totalPages) currentPage = totalPages;

        pageIndicatorLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < totalPages);

        int startIndex = (currentPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, filteredList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Expense e = filteredList.get(i);
            tableModel.addRow(new Object[]{e.getId(), e.getDate(), e.getCategory(), String.format("%.2f", e.getAmount())});
        }
    }

    private int getTotalPages() {
        if (filteredList.isEmpty()) return 1;
        return (int) Math.ceil((double) filteredList.size() / PAGE_SIZE);
    }

    private void handleManualInput() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            String category = (String) categoryBox.getSelectedItem();
            String id = UUID.randomUUID().toString().substring(0, 8);

            currentEngine.addExpense(new Expense(id, amount, date, category));
            applyFiltersAndRefreshTable();
            updateSummaryAndPredictions();
            amountField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please input a valid numeric amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please use correct date format YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceAppUI().setVisible(true));
    }
}