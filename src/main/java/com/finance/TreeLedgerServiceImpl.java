package com.finance;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TreeLedgerServiceImpl implements LedgerService {
    private final TreeSet<Expense> ledger = new TreeSet<>((a, b) -> {
        int d = a.getDate().compareTo(b.getDate());
        if (d != 0) return d;
        return a.getId().compareTo(b.getId());
    });

    @Override
    public void addExpense(Expense expense) {
        ledger.add(expense);
    }

    @Override
    public double getTotalByCategory(String category) {
        double total = 0.0;
        for (Expense e : ledger) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                total += e.getAmount();
            }
        }
        return total;
    }

    @Override
    public List<Expense> getExpensesInDateRange(LocalDate start, LocalDate end) {
        Expense lowBound = new Expense("START", 0, start, "");
        Expense highBound = new Expense("END", 0, end.plusDays(1), "");
        return new ArrayList<>(ledger.subSet(lowBound, highBound));
    }

    @Override
    public void clear() { ledger.clear(); }
    
    @Override
    public List<Expense> getAllExpenses() { return new ArrayList<>(ledger); }
        
    @Override
    public void removeExpense(String id) { ledger.removeIf(e -> e.getId().equals(id)); }
}