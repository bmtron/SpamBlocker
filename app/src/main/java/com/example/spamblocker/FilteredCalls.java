package com.example.spamblocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FilteredCalls {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Integer recID;

    @ColumnInfo(name = "number")
    @NonNull
    public String number;

    @ColumnInfo(name = "whitelisted")
    @NonNull
    public Integer whitelisted;

    @ColumnInfo(name = "calltime")
    @NonNull
    public String calltime;

    @ColumnInfo(name = "deleted")
    @NonNull
    public Integer deleted;

    @ColumnInfo(name="callcount")
    @NonNull
    public Integer callcount;
}
