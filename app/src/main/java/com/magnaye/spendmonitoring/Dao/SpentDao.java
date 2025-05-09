package com.magnaye.spendmonitoring.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.magnaye.spendmonitoring.Model.Spent;

import java.util.Date;
import java.util.List;

@Dao
public interface SpentDao {
    @Insert
    void insert(Spent spent);

    @Update
    void update(Spent spent);

    @Delete
    void delete(Spent spent);


    @Query("DELETE FROM spent")
    void deleteAll();

    @Query("SELECT * FROM spent WHERE date BETWEEN :endDate AND :startDate ORDER BY date ASC")
    LiveData<List<Spent>> getAllSpentLive(Date startDate, Date endDate);

    @Query("SELECT * FROM spent WHERE date BETWEEN :endDate AND :startDate ORDER BY date DESC")
    List<Spent> getSpentByDateRange(Date startDate, Date endDate);


    // Get total sum between two dates (for the total display)
    @Query("SELECT SUM(amount) FROM spent WHERE date BETWEEN :endDate AND :startDate")
    LiveData<Double> getTotalSpentByDateRange(Date startDate, Date endDate);

    @Query("SELECT COUNT(*) FROM spent WHERE date BETWEEN :startDate AND :endDate")
    LiveData<Integer> hasDataForDate(Date startDate, Date endDate);

    @Query("SELECT category, SUM(amount) as total FROM spent WHERE date BETWEEN :endDate AND :startDate GROUP BY category ORDER BY total ASC")
    LiveData<List<CategoryTotal>> getCategoryTotals(Date startDate, Date endDate);

    public class CategoryTotal {
        public String category;
        public double total;
    }

    @Query("SELECT strftime('%Y-%m', date/1000, 'unixepoch') as month, " +
            "SUM(amount) as total FROM spent " +
            "GROUP BY strftime('%Y-%m', date/1000, 'unixepoch') " +
            "ORDER BY month ASC")
    LiveData<List<MonthlyTotal>> getMonthlyTotals();

    class MonthlyTotal {
        public String month; // Format: "YYYY-MM"
        public double total;
    }
}
