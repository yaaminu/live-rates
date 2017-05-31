package com.zealous.expense;

import com.backup.BackupException;
import com.google.gson.JsonObject;
import com.zealous.errors.ZealousException;
import com.zealous.utils.PLog;

import static com.zealous.expense.Expenditure.FIELD_ID;

/**
 * Created by yaaminu on 5/30/17.
 */

public class RemoveExpenditureOperation extends BaseExpenditureOperation {
    private static final String TAG = "RemoveExpenditureOperat";

    //required no-arg
    public RemoveExpenditureOperation() {
    }

    public RemoveExpenditureOperation(String expenditureId) {
        this.data = new JsonObject();
        data.addProperty(FIELD_ID, expenditureId);
    }

    @Override
    protected void doReplay() throws BackupException {
        PLog.d(TAG, "replaying operation: %s", getClass().getName());
        String expenditureId = data().get(FIELD_ID).getAsString();
        //nullness should have been checked in the parent class
        //noinspection ConstantConditions
        if (!dataSource.removeExpenditure(expenditureId)) {
            PLog.d(TAG, "failed to remove expenditure with id %s", expenditureId);
        }
    }
}
