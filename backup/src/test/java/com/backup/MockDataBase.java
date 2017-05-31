package com.backup;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yaaminu on 5/17/17.
 */

public class MockDataBase {
    private final BackupManager manager;
    List<MockDto> records;

    public MockDataBase(BackupManager manager) {
        records = new LinkedList<>();
        this.manager = manager;
    }

    public void insert(MockDto data) {
        records.add(data);
        try {
            manager.log(new InsertOperation(data), System.currentTimeMillis());
        } catch (BackupException e) {
            throw new RuntimeException();
        }
    }

}
