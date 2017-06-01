package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.backup.BackupException;
import com.backup.Operation;
import com.google.gson.JsonObject;

/**
 * Created by yaaminu on 5/30/17.
 */

public class BaseExpenditureOperation implements Operation {

    //will be injected
    @Nullable
    public ExpenditureDataSource dataSource;

    protected JsonObject data;

    public BaseExpenditureOperation() {
        data = new JsonObject();
    }

    @NonNull
    @Override
    public final JsonObject data() {
        return data;
    }

    @Override
    public final void setData(@NonNull JsonObject data) {
        this.data = data;
    }

    protected void doReplay() throws BackupException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void replay() throws BackupException {
        if (dataSource == null) {
            throw new IllegalStateException("data source is null, did you forget to  inject it?");
        }
        //noinspection ConstantConditions
        if (data == null) {
            throw new IllegalStateException("data  is null, did you forget to  inject it?");
        }
        doReplay();
    }
}
