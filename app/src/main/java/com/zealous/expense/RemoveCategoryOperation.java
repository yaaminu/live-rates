package com.zealous.expense;

import com.backup.BackupException;
import com.zealous.errors.ZealousException;
import com.zealous.utils.PLog;

/**
 * Created by yaaminu on 5/30/17.
 */

public class RemoveCategoryOperation extends BaseExpenditureOperation {
    private static final String TAG = "RemoveCategoryOperation";

    public RemoveCategoryOperation() {
    }//required

    public RemoveCategoryOperation(ExpenditureCategory category) {
        this.data = category.toJson();
    }

    @Override
    protected void doReplay() throws BackupException {
        PLog.d(TAG, "replaying operation %s", getClass().getName());
        try {
            //noinspection ConstantConditions
            dataSource.removeCategory(ExpenditureCategory.fromJson(data));
        } catch (ZealousException e) {
            //this happens if the category is associated with an existing expenditure.
            //under normal circumstances it should never happen that we are able to remove
            // a category and can't repeat it. However if the logger
            //implementation is not synchronous, the operations can get out of order.
            //lets fail fast
            PLog.f(TAG, "couldn't replay operation %s ", getClass().getName());
            PLog.f(TAG, e.getMessage(), e);
        }
    }
}
