package com.finance;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class LinearLedgerServiceImpl implements LedgerService {
    private final List<Expense> ledger = new ArrayList<>();
    private final Map<String, Double> categoryTotals = new HashMap<>();

    @Override
    public void addExpense(Expense expense) {
        int insertionIndex = 0;
        for (int i = 0; i < ledger.size(); i++) {
            if (ledger.get(i).getDate().isAfter(expense.getDate())) {
                break;
            }
            insertionIndex++;
        }
        
        ledger.add(insertionIndex, expense);
        
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
        Iterator<Expense> iterator = ledger.iterator();
        while (iterator.hasNext()) {
            Expense expense = iterator.next();
            if (expense.getId().equals(id)) {
                // Update category total before removing the expense
                double currentTotal = categoryTotals.getOrDefault(expense.getCategory(), 0.0);
                categoryTotals.put(expense.getCategory(), currentTotal - expense.getAmount());
                
                // Remove from the ledger
                iterator.remove();
                break;
            }
        }
    }
}