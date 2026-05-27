package com.finance;

import java.time.LocalDate;
import java.util.List;

public interface LedgerService {
    void addExpense(Expense expense);
    double getTotalByCategory(String category);
    List<Expense> getExpensesInDateRange(LocalDate start, LocalDate end);
    List<Expense> getAllExpenses();
    void clear();
}