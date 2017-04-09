package com.zealous.expense;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.zealous.utils.FileUtils;
import com.zealous.utils.GenericUtils;

import java.util.UUID;

public class ExpenditureBuilder {
    private String description;
    private long amountSpent;
    private ExpenditureCategory category;
    private long time;
    private String location;

    public ExpenditureBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ExpenditureBuilder setAmountSpent(long amountSpent) {
        this.amountSpent = amountSpent;
        return this;
    }

    public ExpenditureBuilder setCategory(@NonNull ExpenditureCategory category) {
        GenericUtils.ensureNotNull(category);
        this.category = category;
        return this;
    }

    public ExpenditureBuilder setTime(long time) {
        this.time = time;
        return this;
    }

    public ExpenditureBuilder setLocation(String location) {
        this.location = location;
        return this;
    }


    public Expenditure createExpenditure() {
        return new Expenditure(FileUtils.sha1(UUID.randomUUID().toString() + SystemClock.elapsedRealtime() + location), description, amountSpent, category, time, location);
    }
}