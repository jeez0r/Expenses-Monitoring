package com.magnaye.spendmonitoring.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.magnaye.spendmonitoring.Dao.SpentDao;
import com.magnaye.spendmonitoring.DatabaseHelper.DatabaseClient;

import java.util.List;

public class MonthlyChartViewModel extends AndroidViewModel {
    private SpentDao spentDao;

    public MonthlyChartViewModel(@NonNull Application application) {
        super(application);
        spentDao = DatabaseClient.getInstance(application).getAppDatabase().spentDao();
    }

    public LiveData<List<SpentDao.MonthlyTotal>> getMonthlyTotals() {
        return spentDao.getMonthlyTotals();
    }
}
