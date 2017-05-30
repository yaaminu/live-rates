package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.backup.BackupException;
import com.backup.Operation;
import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

/**
 * Created by yaaminu on 5/30/17.
 */

public class AddOrUpdateExpenditureOperation implements Operation {
    private static final String TAG = "AddOrUpdateExpenditureOperation";
    //will be injected
    @Nullable
    ExpenditureDataSource dataSource;
    @NonNull
    private JsonObject data;

    public AddOrUpdateExpenditureOperation() {
        data = new JsonObject();
    }

    public AddOrUpdateExpenditureOperation(@NonNull Expenditure expenditure) {
        data = expenditure.toJson();
    }


    @NonNull
    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public void setData(@NonNull JsonObject object) {
        this.data = object;
    }

    @Override
    public void replay() throws BackupException {
        PLog.d(TAG, "replaying operation:  %s", getClass().getName());
        if (dataSource == null) {
            throw new IllegalStateException("data source is null, did you forget to  inject it?");
        }
        //noinspection ConstantConditions
        if (data == null) {
            throw new IllegalStateException("data  is null, did you forget to  inject it?");
        }
        Expenditure expenditure = Expenditure.fromJson(data);
        assert dataSource != null;
        dataSource.addOrUpdateExpenditure(expenditure);
    }
}
