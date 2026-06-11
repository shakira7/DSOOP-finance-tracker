package com.finance;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// list-based expense storage
public class LinearLedgerServiceImpl implements LedgerService {
    private final List<Expense> ledger = new ArrayList<>();
    private final Map<String, Double> categoryTotals = new HashMap<>();

    @Override
    public void addExpense(Expense expense) {
        ledger.add(expense);
        categoryTotals.put(expense.getCategory(), 
            categoryTotals.getOrDefault(expense.getCategory(), 0.0) + expense.getAmount());
    }

    @Override
    public double getTotalByCategory(String category) {
        return categoryTotals.getOrDefault(category, 0.0);
    }

    @Override
    public List<Expense> getExpensesInDateRange(LocalDate start, LocalDate end) {
        List<Expense> result = new ArrayList<>();
        for (Expense e : ledger) {
            if (!e.getDate().isBefore(start) && !e.getDate().isAfter(end)) {
                result.add(e);
            }
        }
        return result;
    }

    @Override
    public void clear() { ledger.clear(); categoryTotals.clear(); }
    
    @Override
    public List<Expense> getAllExpenses() {
        return new ArrayList<>(ledger);
    }
    
    @Override
    public void removeExpense(String id) {
    ledger.removeIf(expense -> expense.getId().equals(id));
}
}