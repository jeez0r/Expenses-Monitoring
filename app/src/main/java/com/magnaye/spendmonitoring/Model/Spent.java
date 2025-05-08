package com.magnaye.spendmonitoring.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "spent")
public class Spent {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private double amount;
    private String category;
    private Date date;

    // Constructor (without id as it's auto-generated)
    public Spent(double amount, String category, Date date) {
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
