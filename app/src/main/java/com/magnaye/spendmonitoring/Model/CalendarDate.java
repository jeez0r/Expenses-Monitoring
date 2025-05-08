package com.magnaye.spendmonitoring.Model;

import java.time.LocalDate;

public class CalendarDate {
    private String dayName;
    private int dayNumber;
    private boolean isSelected;
    private boolean isFuture;
    private LocalDate date;
    private boolean hasData; // New field to track if date has data

    public CalendarDate(String dayName, int dayNumber, boolean isSelected, boolean isFuture, LocalDate date,boolean hasData) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.isSelected = isSelected;
        this.isFuture = isFuture;
        this.date = date;
        this.hasData = hasData;
    }

    // Getters
    public String getDayName() { return dayName; }
    public int getDayNumber() { return dayNumber; }
    public boolean isSelected() { return isSelected; }
    public boolean isFuture() { return isFuture; }
    public LocalDate getDate() { return date; }

    // Setters
    public void setSelected(boolean selected) { isSelected = selected; }

    public boolean hasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }
}
