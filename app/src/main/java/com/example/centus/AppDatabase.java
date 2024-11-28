package com.example.centus;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Debt.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DebtDao debtDao();
}

