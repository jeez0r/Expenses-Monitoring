package com.magnaye.spendmonitoring.DatabaseHelper;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.magnaye.spendmonitoring.Dao.SpentDao;
import com.magnaye.spendmonitoring.Model.Spent;
import com.magnaye.spendmonitoring.Module.Converters;

@Database(entities = {Spent.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract SpentDao spentDao();
}