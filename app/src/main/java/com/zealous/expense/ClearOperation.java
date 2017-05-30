package com.zealous.expense;

import com.backup.BackupException;

/**
 * Created by yaaminu on 5/30/17.
 */

public class ClearOperation extends BaseExpenditureOperation {

    @Override
    protected void doReplay() throws BackupException {
        assert dataSource != null;
        dataSource.clear();
    }
}
