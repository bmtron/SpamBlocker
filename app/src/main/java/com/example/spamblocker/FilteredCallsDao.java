package com.example.spamblocker;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface FilteredCallsDao {
    @Query("SELECT * FROM FilteredCalls")
    List<FilteredCalls> getAll();

    @Query("SELECT * FROM FilteredCalls where recID = :id")
    FilteredCalls getByRecID(int id);

    @Query("SELECT * FROM filteredcalls WHERE whitelisted = :wl and deleted = :del")
    List<FilteredCalls> getWhiteList(int wl, int del);

    @Query("SELECT * FROM FilteredCalls WHERE deleted = 0")
    List<FilteredCalls> getAllExceptDeleted();

    @Query("SELECT * FROM FilteredCalls where number = :number")
    FilteredCalls getCallByNumber(String number);

    @Query("UPDATE filteredcalls set whitelisted = :wl where recid = :id")
    void updateTest(int wl, int id);

    @Query("UPDATE filteredcalls set calltime = :newTime where recid = :id")
    void updateCallTime(String newTime, int id);

    @Query("UPDATE FILTEREDCALLS set callcount = :count where recid = :id")
    void updateCallCount(int count, int id);

    @Insert
    void insert(FilteredCalls call);

    @Query("DELETE FROM FilteredCalls")
    void nukeDB();

    @Query("SELECT callcount FROM FilteredCalls where recID = :id")
    Integer getCallCount(int id);


}
