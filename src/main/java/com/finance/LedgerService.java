package com.finance;

import java.time.LocalDate;
import java.util.List;

public interface LedgerService {
    void addExpense(Expense expense);
    List<Expense> getAllExpenses();
    List<Expense> getExpensesInDateRange(LocalDate start, LocalDate end);
    double getTotalByCategory(String category);
    void clear();
    
    void removeExpense(String id);
}