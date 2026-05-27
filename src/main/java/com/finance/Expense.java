package com.finance;

import java.time.LocalDate;
import java.util.Objects;

public class Expense implements Comparable<Expense> {
    private final String id;
    private final double amount;
    private final LocalDate date;
    private final String category;

    public Expense(String id, double amount, LocalDate date, String category) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public String getId() { return id; }

    @Override
    public int compareTo(Expense o) {
        int dateComp = this.date.compareTo(o.date);
        if (dateComp != 0) return dateComp;
        return this.id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Expense)) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}