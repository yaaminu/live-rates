package com.zealous.expense;

import com.backup.BackupException;
import com.zealous.utils.PLog;

/**
 * Created by yaaminu on 5/30/17.
 */

public class RemoveExpenditureOperation extends BaseExpenditureOperation {
    private static final String TAG = "RemoveExpenditureOperat";

    //required no-arg
    public RemoveExpenditureOperation() {
    }

    public RemoveExpenditureOperation(Expenditure expenditure) {
        this.data = expenditure.toJson();
    }

    @Override
    protected void doReplay() throws BackupException {
        PLog.d(TAG, "replaying operation: %s", getClass().getName());
        Expenditure toRestore = Expenditure.fromJson(data());
        //nullness should have been checked in the parent class
        //noinspection ConstantConditions
        if (!dataSource.removeExpenditure(toRestore.getId())) {
            PLog.d(TAG, "failed to remove expenditure with id %s", toRestore.getId());
        }
    }
}
