package com.finance;

import java.time.LocalDate;
import java.util.*;

public class TreeLedgerServiceImpl implements LedgerService {
    private final TreeSet<Expense> ledger = new TreeSet<>();
    private final Map<String, TreeMap<LocalDate, List<Expense>>> categoryTreeMap = new HashMap<>();

    @Override
    public void addExpense(Expense expense) {
        ledger.add(expense);
        categoryTreeMap
            .computeIfAbsent(expense.getCategory(), k -> new TreeMap<>())
            .computeIfAbsent(expense.getDate(), k -> new ArrayList<>())
            .add(expense);
    }

    @Override
    public double getTotalByCategory(String category) {
        TreeMap<LocalDate, List<Expense>> categoryData = categoryTreeMap.get(category);
        if (categoryData == null) return 0.0;
        return categoryData.values().stream()
                .flatMap(List::stream)
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    @Override
    public List<Expense> getExpensesInDateRange(LocalDate start, LocalDate end) {
        Expense lowBound = new Expense("START", 0, start, "");
        Expense highBound = new Expense("END", 0, end.plusDays(1), "");
        return new ArrayList<>(ledger.subSet(lowBound, highBound));
    }

    @Override
    public void clear() { ledger.clear(); categoryTreeMap.clear(); }
    
    @Override
        public List<Expense> getAllExpenses() {
        return new ArrayList<>(ledger);
    }
}