package com.magnaye.spendmonitoring.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.magnaye.spendmonitoring.Dao.SpentDao;
import com.magnaye.spendmonitoring.DatabaseHelper.DatabaseClient;
import com.magnaye.spendmonitoring.Model.Spent;

public class SpentViewModel extends AndroidViewModel {
    private SpentDao spentDao;

    public SpentViewModel(@NonNull Application application) {
        super(application);
        spentDao = DatabaseClient.getInstance(application).getAppDatabase().spentDao();
    }

    public void updateSpent(Spent spent) {
        new Thread(() -> {
            spentDao.update(spent);
        }).start();
    }

    public void deleteSpent(Spent spent) {
        new Thread(() -> {
            spentDao.delete(spent);
        }).start();
    }


}