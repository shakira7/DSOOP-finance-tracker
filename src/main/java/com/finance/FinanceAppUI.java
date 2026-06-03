package com.finance;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
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
    private double currentBalance = 5000.00; 
    
    private List<Expense> filteredList = new ArrayList<>();
    private int currentPage = 1;
    private final int PAGE_SIZE = 50;
    
    private static final Color BG_COLOR = new Color(251, 248, 243);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color TABLE_STRIPE_COLOR = new Color(248, 249, 250);

    private static final Color ACCENT_BLUE = new Color(52, 152, 219);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color ACCENT_PURPLE = new Color(155, 89, 182);
    private static final Color ACCENT_RED = new Color(231, 76, 60);

    private static final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BALANCE_FONT = new Font("SansSerif", Font.BOLD, 18);

    private final JTextField amountField = createStyledTextField(10);
    private final JTextField dateField = createStyledTextField(10);
    private final JComboBox<String> categoryBox = createStyledComboBox(new String[]{"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"});
    private final JTextArea summaryArea = new JTextArea(10, 22);
    private final JLabel balanceLabel = new JLabel("Wallet Balance: $5000.00");
    
    private final PieChartPanel pieChart = new PieChartPanel();
    private final BillSplitPanel billSplitPanel = new BillSplitPanel();

    private final JComboBox<String> filterCategoryBox = createStyledComboBox(new String[]{"ALL", "GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC", "DEPOSIT"});
    private final JTextField filterStartField = createStyledTextField(7);
    private final JTextField filterEndField = createStyledTextField(7);
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Date", "Category", "Amount ($)"}, 0);
    private final JTable expenseTable = new JTable(tableModel);
    
    private final JButton firstPageBtn = createStyledButton("First", ACCENT_BLUE);
    private final JButton prevPageBtn = createStyledButton("<- Previous", ACCENT_BLUE);
    private final JLabel pageIndicatorLabel = new JLabel("Page 1 of 1");
    private final JButton nextPageBtn = createStyledButton("Next ->", ACCENT_BLUE);
    private final JButton lastPageBtn = createStyledButton("Last", ACCENT_BLUE);
    private final JTextField jumpToField = createStyledTextField(4);
    private final JButton jumpToBtn = createStyledButton("Go", ACCENT_BLUE);

    private final JButton applyFilterBtn = createStyledButton("Search", ACCENT_BLUE);
    private final JButton deleteSelectedBtn = createStyledButton("Delete Selected", ACCENT_RED);
    private final JButton deleteRangeBtn = createStyledButton("Delete Range", ACCENT_RED);

    private final JTextField seedField = createStyledTextField(6);
    private final JTextArea logArea = new JTextArea(12, 45);
    private final JLabel engineLabel = new JLabel("Active Engine: ArrayList + HashMap (Solution 1)");

    public FinanceAppUI() {
        setTitle("Finance Tracker & System Performance Profiler");
        
        UIManager.put("Label.font", MAIN_FONT);
        UIManager.put("Button.font", MAIN_FONT);
        UIManager.put("Table.font", MAIN_FONT);
        UIManager.put("TextField.font", MAIN_FONT);
        UIManager.put("ComboBox.font", MAIN_FONT);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        filterStartField.setText("2020-01-01");
        filterEndField.setText("2026-12-31");
        seedField.setText("42");
        dateField.setText(LocalDate.now().toString());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(MAIN_FONT);
        tabbedPane.addTab("Dashboard", createNormalAppPanel());
        tabbedPane.addTab("System Profiler", createDevPanel());
        add(tabbedPane);
        
        applyFiltersAndRefreshTable();
    }

    private JPanel createNormalAppPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel leftPanel = createCardPanel();
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(320, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        balanceLabel.setFont(BALANCE_FONT);
        balanceLabel.setForeground(ACCENT_BLUE);
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        leftPanel.add(balanceLabel, gbc);

        JButton depositBtn = createStyledButton("Deposit Cash", ACCENT_BLUE);
        gbc.gridy = 1; leftPanel.add(depositBtn, gbc);

        gbc.gridy = 2; leftPanel.add(new JSeparator(), gbc);

        gbc.gridwidth = 1; 
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0.3;
        leftPanel.add(createStyledLabel("Amount ($):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        leftPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        leftPanel.add(createStyledLabel("Date:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        leftPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        leftPanel.add(createStyledLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        leftPanel.add(categoryBox, gbc);

        JButton addBtn = createStyledButton("Add Expense", ACCENT_GREEN);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.weightx = 1.0;
        leftPanel.add(addBtn, gbc);
        
        summaryArea.setEditable(false);
        summaryArea.setBackground(new Color(248, 249, 250));
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        gbc.gridy = 7; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(new JScrollPane(summaryArea), gbc);

        JPanel centerContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        centerContainer.setOpaque(false);
        centerContainer.setPreferredSize(new Dimension(380, 600));

        JPanel centerChartPanel = createCardPanel();
        centerChartPanel.setLayout(new BorderLayout());
        JLabel chartTitle = createHeaderLabel("Expense Distribution");
        chartTitle.setHorizontalAlignment(SwingConstants.CENTER);
        centerChartPanel.add(chartTitle, BorderLayout.NORTH);
        
        pieChart.setOpaque(true);
        pieChart.setBackground(CARD_COLOR);
        centerChartPanel.add(pieChart, BorderLayout.CENTER);

        billSplitPanel.setOpaque(true);
        billSplitPanel.setBackground(CARD_COLOR);

        centerContainer.add(centerChartPanel);
        centerContainer.add(billSplitPanel); 

        JPanel rightPanel = createCardPanel();
        rightPanel.setLayout(new BorderLayout(5, 5));

        JPanel operationalHeaderPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        operationalHeaderPanel.setOpaque(false);

        JPanel searchFilterBar = new JPanel(new GridBagLayout());
        searchFilterBar.setOpaque(false);
        GridBagConstraints sfGbc = new GridBagConstraints();
        sfGbc.insets = new Insets(2, 4, 2, 4);
        sfGbc.fill = GridBagConstraints.HORIZONTAL;

        sfGbc.gridx = 0; searchFilterBar.add(createStyledLabel("Category:"), sfGbc);
        sfGbc.gridx = 1; searchFilterBar.add(filterCategoryBox, sfGbc);
        sfGbc.gridx = 2; searchFilterBar.add(createStyledLabel(" From:"), sfGbc);
        sfGbc.gridx = 3; searchFilterBar.add(filterStartField, sfGbc);
        sfGbc.gridx = 4; searchFilterBar.add(createStyledLabel(" To:"), sfGbc);
        sfGbc.gridx = 5; searchFilterBar.add(filterEndField, sfGbc);
        
        applyFilterBtn.setPreferredSize(new Dimension(90, 25));
        sfGbc.gridx = 6; sfGbc.weightx = 1.0; searchFilterBar.add(applyFilterBtn, sfGbc);

        JPanel actionControlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionControlBar.setOpaque(false);
        deleteSelectedBtn.setPreferredSize(new Dimension(130, 26));
        deleteRangeBtn.setPreferredSize(new Dimension(120, 26));
        actionControlBar.add(deleteSelectedBtn);
        actionControlBar.add(Box.createHorizontalStrut(10));
        actionControlBar.add(deleteRangeBtn);

        operationalHeaderPanel.add(searchFilterBar);
        operationalHeaderPanel.add(actionControlBar);
        rightPanel.add(operationalHeaderPanel, BorderLayout.NORTH);
        
        styleExpenseTable();
        JScrollPane tableScrollPane = new JScrollPane(expenseTable);
        tableScrollPane.setPreferredSize(new Dimension(500, 380));
        rightPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel paginationBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        paginationBar.setOpaque(false);
        paginationBar.add(firstPageBtn);
        paginationBar.add(prevPageBtn);
        pageIndicatorLabel.setFont(MAIN_FONT);
        pageIndicatorLabel.setForeground(TEXT_COLOR);
        paginationBar.add(pageIndicatorLabel);
        paginationBar.add(nextPageBtn);
        paginationBar.add(lastPageBtn);
        JLabel jumpLabel = createStyledLabel("| Jump to Page:");
        paginationBar.add(jumpLabel);
        paginationBar.add(jumpToField);
        paginationBar.add(jumpToBtn);
        rightPanel.add(paginationBar, BorderLayout.SOUTH);

        JPanel rightSideLayoutSplit = new JPanel(new BorderLayout(15, 15));
        rightSideLayoutSplit.setOpaque(false);
        rightSideLayoutSplit.add(centerContainer, BorderLayout.WEST);
        rightSideLayoutSplit.add(rightPanel, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightSideLayoutSplit, BorderLayout.CENTER);

        addBtn.addActionListener(e -> handleManualInput());
        depositBtn.addActionListener(e -> handleDeposit());
        applyFilterBtn.addActionListener(e -> { currentPage = 1; applyFiltersAndRefreshTable(); });
        
        deleteSelectedBtn.addActionListener(e -> handleDeleteSelected());
        deleteRangeBtn.addActionListener(e -> handleDeleteRange());

        firstPageBtn.addActionListener(e -> { currentPage = 1; renderTablePage(); });
        prevPageBtn.addActionListener(e -> { if (currentPage > 1) { currentPage--; renderTablePage(); } });
        nextPageBtn.addActionListener(e -> { if (currentPage < getTotalPages()) { currentPage++; renderTablePage(); } });
        lastPageBtn.addActionListener(e -> { currentPage = getTotalPages(); renderTablePage(); });
        
        jumpToBtn.addActionListener(e -> {
            try {
                int targetPage = Integer.parseInt(jumpToField.getText().trim());
                int maxPages = getTotalPages();
                if (targetPage >= 1 && targetPage <= maxPages) {
                    currentPage = targetPage;
                    renderTablePage();
                    jumpToField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Page number out of bounds. Range: 1 to " + maxPages, "Error", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid page integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return mainPanel;
    }

    private JPanel createDevPanel() {
        JPanel devPanel = new JPanel(new BorderLayout(15, 15));
        devPanel.setBackground(BG_COLOR);
        devPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel controlPanel = createCardPanel();
        controlPanel.setLayout(new GridLayout(2, 1, 5, 5));
        engineLabel.setFont(HEADER_FONT);
        engineLabel.setForeground(TEXT_COLOR);
        controlPanel.add(engineLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setOpaque(false);
        JButton toggleBtn = createStyledButton("Switch Data Structure", ACCENT_PURPLE);
        buttonPanel.add(toggleBtn);
        
        buttonPanel.add(createStyledLabel("Seed Number:"));
        buttonPanel.add(seedField);
        
        JButton seedBtn = createStyledButton("Generate Test Data", ACCENT_GREEN);
        JButton clearBtn = createStyledButton("Delete All Data", new Color(231, 76, 60)); 
        JButton profileBtn = createStyledButton("Check Method Speeds", ACCENT_BLUE);
        
        buttonPanel.add(seedBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(profileBtn);
        controlPanel.add(buttonPanel);

        devPanel.add(controlPanel, BorderLayout.NORTH);

        JPanel logCard = createCardPanel();
        logCard.setLayout(new BorderLayout());
        logArea.setEditable(false);
        logArea.setBackground(new Color(248, 249, 250));
        logArea.setForeground(TEXT_COLOR);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        logCard.add(new JScrollPane(logArea), BorderLayout.CENTER);
        devPanel.add(logCard, BorderLayout.CENTER);

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
        });

        seedBtn.addActionListener(e -> {
            long seedValue;
            try {
                seedValue = Long.parseLong(seedField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Specify a valid seed integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String countInput = JOptionPane.showInputDialog(this, "How many test records would you like to generate?", "200000");
            if (countInput == null || countInput.trim().isEmpty()) {
                return;
            }

            int recordCount;
            try {
                recordCount = Integer.parseInt(countInput.trim());
                if (recordCount <= 0) throw new IllegalArgumentException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive integer quantity.", "Invalid Count", JOptionPane.ERROR_MESSAGE);
                return;
            }

            logArea.append("⚙️ Generating " + recordCount + " records from December 2025 backward into the past...\n");

            Timer timer = new Timer(50, event -> {
                long start = System.currentTimeMillis();
                LocalDate latestDate = LocalDate.of(2025, 12, 31);
                String[] categories = {"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"};
                Random seededRand = new Random(seedValue);

                for (int i = 0; i < recordCount; i++) {
                    long randomDaysBackward = (long) (seededRand.nextDouble() * (365 * 5));
                    double amount = 5.0 + (seededRand.nextDouble() * 495.0);
                    String cat = categories[seededRand.nextInt(categories.length)];

                    currentEngine.addExpense(new Expense(
                            UUID.nameUUIDFromBytes(("id_" + seedValue + "_" + i).getBytes()).toString().substring(0, 8),
                            Math.round(amount * 100.0) / 100.0,
                            latestDate.minusDays(randomDaysBackward),
                            cat
                    ));
                }
                long end = System.currentTimeMillis();
                logArea.append("➔ [addExpense()] Finished adding " + recordCount + " items. Time taken: " + (end - start) + " ms.\n\n");

                applyFiltersAndRefreshTable();
            });
            timer.setRepeats(false);
            timer.start();
        });

        clearBtn.addActionListener(e -> {
            currentEngine.clear();
            filteredList.clear();
            currentPage = 1;
            applyFiltersAndRefreshTable();
            logArea.append("🗑️ Cleaned memory. All entries have been deleted.\n");
        });

        profileBtn.addActionListener(e -> {
            logArea.append("📋 TESTING METHOD RUNTIMES...\n");
            String[] categories = {"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"};
            
            long startCatNano = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                for (String cat : categories) {
                    currentEngine.getTotalByCategory(cat);
                }
            }
            long endCatNano = System.nanoTime();
            double avgCatTime = ((endCatNano - startCatNano) / 1000.0) / 1_000_000.0;
            logArea.append(String.format("➔ [getTotalByCategory()] Time to fetch category sums: %.4f ms\n", avgCatTime));

            LocalDate searchStart = LocalDate.of(2022, 1, 1);
            LocalDate searchEnd = LocalDate.of(2022, 3, 1);
            long startRangeNano = System.nanoTime();
            List<Expense> rangeResults = currentEngine.getExpensesInDateRange(searchStart, searchEnd);
            long endRangeNano = System.nanoTime();
            logArea.append(String.format("➔ [getExpensesInDateRange()] Found %d items between dates in: %.4f ms\n", rangeResults.size(), (endRangeNano - startRangeNano)/1_000_000.0));

            long startAllNano = System.nanoTime();
            List<Expense> allResults = currentEngine.getAllExpenses();
            long endAllNano = System.nanoTime();
            logArea.append(String.format("➔ [getAllExpenses()] Retrieved all %d list entries in: %.4f ms\n", allResults.size(), (endAllNano - startAllNano)/1_000_000.0));

            long startDeleteNano = System.nanoTime();
            currentEngine.removeExpense("NON_EXISTENT_ID_FOR_BENCHMARK");
            long endDeleteNano = System.nanoTime();
            logArea.append(String.format("➔ [removeExpense()] Single id removal traversal completed in: %.4f ms\n", (endDeleteNano - startDeleteNano)/1_000_000.0));

            String yearInput = JOptionPane.showInputDialog(this, "Enter a year to benchmark batch removal speed (or type 'X' to cancel):");
            if (yearInput != null && !yearInput.trim().isEmpty() && !yearInput.trim().equalsIgnoreCase("X")) {
                try {
                    int targetYear = Integer.parseInt(yearInput.trim());
                    LocalDate yearStart = LocalDate.of(targetYear, 1, 1);
                    LocalDate yearEnd = LocalDate.of(targetYear, 12, 31);
                    
                    logArea.append(String.format("⏳ Batch cleaning all records for the year %d...\n", targetYear));
                    
                    long startBatchDelete = System.currentTimeMillis();
                    List<Expense> elementsInYear = currentEngine.getExpensesInDateRange(yearStart, yearEnd);
                    int batchCount = elementsInYear.size();
                    
                    for (Expense exp : elementsInYear) {
                        currentEngine.removeExpense(exp.getId());
                    }
                    long endBatchDelete = System.currentTimeMillis();
                    
                    logArea.append(String.format("➔ [Batch Delete Year] Cleaned %d records for %d in: %d ms\n\n", batchCount, targetYear, (endBatchDelete - startBatchDelete)));
                    applyFiltersAndRefreshTable();
                } catch (Exception ex) {
                    logArea.append("❌ Year benchmark aborted due to formatting processing issue.\n\n");
                }
            } else {
                logArea.append("⏩ Year deletion benchmark canceled by configuration parameter input.\n\n");
            }
        });

        return devPanel;
    }

    private void handleFocusInputVerification() {
        if (filterStartField.getText().trim().isEmpty()) filterStartField.setText("2020-01-01");
        if (filterEndField.getText().trim().isEmpty()) fieldTextReset();
    }

    private void fieldTextReset() {
        filterEndField.setText("2026-12-31");
    }

    private void handleDeposit() {
        String input = JOptionPane.showInputDialog(this, "Enter deposit cash amount ($):");
        if (input == null || input.trim().isEmpty()) return;
        try {
            double depositAmount = Double.parseDouble(input.trim());
            if (depositAmount <= 0) throw new IllegalArgumentException();

            String id = "DEP-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            currentEngine.addExpense(new Expense(id, depositAmount, LocalDate.now(), "DEPOSIT"));
            
            applyFiltersAndRefreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive numeric amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSelected() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please choose a transaction from the list below to delete first.", "Selection Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String transactionId = tableModel.getValueAt(selectedRow, 0).toString();
        
        int confirmation = JOptionPane.showConfirmDialog(this, "Permanently remove transaction ID: " + transactionId + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            currentEngine.removeExpense(transactionId);
            applyFiltersAndRefreshTable();
        }
    }

    private void handleDeleteRange() {
        try {
            handleFocusInputVerification();
            LocalDate start = LocalDate.parse(filterStartField.getText().trim());
            LocalDate end = LocalDate.parse(filterEndField.getText().trim());
            
            int confirmation = JOptionPane.showConfirmDialog(this, "Permanently remove ALL transactions matching criteria from " + start + " to " + end + "?", "Confirm Batch Deletion", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                List<Expense> entriesToDelete = currentEngine.getExpensesInDateRange(start, end);
                String selectedCategory = (String) filterCategoryBox.getSelectedItem();
                
                int deletedCount = 0;
                for (Expense e : entriesToDelete) {
                    if (selectedCategory.equals("ALL") || e.getCategory().equalsIgnoreCase(selectedCategory)) {
                        currentEngine.removeExpense(e.getId());
                        deletedCount++;
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Successfully scrubbed " + deletedCount + " matched entries from core metrics.", "Scrub Complete", JOptionPane.INFORMATION_MESSAGE);
                currentPage = 1;
                applyFiltersAndRefreshTable();
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Set validation ranges via valid YYYY-MM-DD guidelines first.", "Input Processing Issue", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummaryAndGraph() {
        String[] categories = {"GROCERIES", "RENT", "UTILITIES", "ENTERTAINMENT", "MISC"};
        Map<String, Double> filteredTotalsMap = new HashMap<>();
        double calculatedBalance = 5000.00; 

        for (Expense e : currentEngine.getAllExpenses()) {
            if ("DEPOSIT".equals(e.getCategory())) {
                calculatedBalance += e.getAmount();
            } else {
                calculatedBalance -= e.getAmount();
            }
        }
        currentBalance = calculatedBalance;
        balanceLabel.setText(String.format("Wallet Balance: $%.2f", currentBalance));

        for (String cat : categories) {
            filteredTotalsMap.put(cat, 0.0);
        }

        for (Expense e : filteredList) {
            if (filteredTotalsMap.containsKey(e.getCategory())) {
                filteredTotalsMap.put(e.getCategory(), filteredTotalsMap.get(e.getCategory()) + e.getAmount());
            }
        }

        pieChart.updateData(filteredTotalsMap);

        StringBuilder sb = new StringBuilder("=== TOTAL SPENT ===\n");
        for (String cat : categories) {
            sb.append(String.format("%-14s: $%.2f\n", cat, filteredTotalsMap.get(cat)));
        }

        sb.append("\n=== PREDICTION ===\n");
        if (filteredList.isEmpty()) {
            sb.append("No entries in filter range.");
            summaryArea.setText(sb.toString());
            return;
        }

        LocalDate minDate = LocalDate.MAX;
        for (Expense e : filteredList) {
            if (!"DEPOSIT".equals(e.getCategory()) && e.getDate().isBefore(minDate)) minDate = e.getDate();
        }
        
        if (minDate == LocalDate.MAX) {
            sb.append("Awaiting expense data...");
            summaryArea.setText(sb.toString());
            return;
        }
        
        LocalDate baseMonth = minDate.withDayOfMonth(1);
        Map<String, Map<Long, Double>> historicalGrid = new HashMap<>();
        for (String cat : categories) historicalGrid.put(cat, new HashMap<>());

        long maxMonthIndex = 0;
        for (Expense e : filteredList) {
            if ("DEPOSIT".equals(e.getCategory())) continue;
            long monthIndex = ChronoUnit.MONTHS.between(baseMonth, e.getDate().withDayOfMonth(1));
            maxMonthIndex = Math.max(maxMonthIndex, monthIndex);
            
            Map<Long, Double> monthMap = historicalGrid.get(e.getCategory());
            monthMap.put(monthIndex, monthMap.getOrDefault(monthIndex, 0.0) + e.getAmount());
        }

        long targetNextMonth = maxMonthIndex + 1;
        for (String cat : categories) {
            Map<Long, Double> points = historicalGrid.get(cat);
            if (points.size() <= 1) {
                double fallbackSum = 0;
                for (double val : points.values()) fallbackSum += val;
                sb.append(String.format("%-14s: $%.2f\n", cat, points.isEmpty() ? 0.0 : fallbackSum / points.size()));
                continue;
            }

            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            int n = points.size();
            for (Map.Entry<Long, Double> entry : points.entrySet()) {
                long x = entry.getKey(); double y = entry.getValue();
                sumX += x; sumY += y; sumXY += (x * y); sumX2 += (x * x);
            }

            double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            double intercept = (sumY - slope * sumX) / n;
            double predictedExpenditure = (slope * targetNextMonth) + intercept;
            sb.append(String.format("%-14s: $%.2f\n", cat, Math.max(0.0, predictedExpenditure)));
        }
        summaryArea.setText(sb.toString());
    }

    private void applyFiltersAndRefreshTable() {
        try {
            handleFocusInputVerification();
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
            updateSummaryAndGraph();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Use format YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderTablePage() {
        tableModel.setRowCount(0);
        if (filteredList.isEmpty()) {
            pageIndicatorLabel.setText("Page 1 of 1");
            firstPageBtn.setEnabled(false); prevPageBtn.setEnabled(false);
            nextPageBtn.setEnabled(false); lastPageBtn.setEnabled(false);
            return;
        }
        int totalPages = getTotalPages();
        if (currentPage > totalPages) currentPage = totalPages;

        pageIndicatorLabel.setText("Page " + currentPage + " of " + totalPages);
        
        firstPageBtn.setEnabled(currentPage > 1);
        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < totalPages);
        lastPageBtn.setEnabled(currentPage < totalPages);

        int startIndex = (currentPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, filteredList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Expense e = filteredList.get(i);
            String displayAmt = "DEPOSIT".equals(e.getCategory()) ? "+" + e.getAmount() : String.format("%.2f", e.getAmount());
            tableModel.addRow(new Object[]{e.getId(), e.getDate(), e.getCategory(), displayAmt});
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

            currentEngine.addExpense(new Expense(UUID.randomUUID().toString().substring(0, 8), amount, date, category));
            applyFiltersAndRefreshTable();
            amountField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Check validation inputs.");
        }
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, Color.LIGHT_GRAY), 
            new EmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MAIN_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(MAIN_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(3, 5, 3, 5)
        ));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(MAIN_FONT);
        box.setBackground(Color.WHITE);
        box.setForeground(TEXT_COLOR);
        return box;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (!isEnabled()) {
                    g2.setColor(new Color(
                        Math.min(255, bgColor.getRed() + 65),
                        Math.min(255, bgColor.getGreen() + 65),
                        Math.min(255, bgColor.getBlue() + 65)
                    ));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(255, 255, 255, 180)); 
                } else if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(Color.WHITE);
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(Color.WHITE);
                }
                
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(MAIN_FONT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return button;
    }

    private void styleExpenseTable() {
        expenseTable.setFont(MAIN_FONT);
        expenseTable.setRowHeight(28); 
        expenseTable.setShowGrid(false); 
        expenseTable.setIntercellSpacing(new Dimension(0, 0));
        expenseTable.setFillsViewportHeight(true);
        expenseTable.setBackground(CARD_COLOR);

        expenseTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(ACCENT_BLUE.brighter());
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? CARD_COLOR : TABLE_STRIPE_COLOR);
                    c.setForeground(TEXT_COLOR);
                }
                setHorizontalAlignment(column == 3 ? RIGHT : LEFT); 
                setBorder(new EmptyBorder(0, 8, 0, 8)); 
                return c;
            }
        });

        JTableHeader header = expenseTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(TABLE_STRIPE_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceAppUI().setVisible(true));
    }
}