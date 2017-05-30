package com.zealous.expense;

import android.support.annotation.NonNull;

import com.backup.BackupException;
import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

/**
 * Created by yaaminu on 5/30/17.
 */

public class AddOrUpdateExpenditureOperation extends BaseExpenditureOperation {
    private static final String TAG = "AddOrUpdateExpenditureOperation";

    //required no-arg constructor
    public AddOrUpdateExpenditureOperation() {
        data = new JsonObject();
    }

    public AddOrUpdateExpenditureOperation(@NonNull Expenditure expenditure) {
        data = expenditure.toJson();
    }

    @Override
    protected void doReplay() throws BackupException {
        PLog.d(TAG, "replaying operation:  %s", getClass().getName());
        Expenditure expenditure = Expenditure.fromJson(data);
        assert dataSource != null;
        dataSource.addOrUpdateExpenditure(expenditure);
    }
}
