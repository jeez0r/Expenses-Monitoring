package com.magnaye.spendmonitoring.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.magnaye.spendmonitoring.Dao.SpentDao;
import com.magnaye.spendmonitoring.DatabaseHelper.DatabaseClient;

import java.util.Date;
import java.util.List;

public class CategoryChartViewModel extends AndroidViewModel {
    private final SpentDao spentDao;

    public CategoryChartViewModel(@NonNull Application application) {
        super(application);
        spentDao = DatabaseClient.getInstance(application).getAppDatabase().spentDao();
    }

    public LiveData<List<SpentDao.CategoryTotal>> getCategoryTotals(Date startDate, Date endDate) {
        return spentDao.getCategoryTotals(startDate, endDate);
    }
}