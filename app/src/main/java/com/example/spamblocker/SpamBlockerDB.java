package com.example.spamblocker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {FilteredCalls.class}, version = 1)
public abstract class SpamBlockerDB extends RoomDatabase {
    public abstract FilteredCallsDao callsDao();
}
