package com.backup;

import com.android.annotations.NonNull;

/**
 * Created by yaaminu on 5/17/17.
 */
public class InsertOperation implements Operation {

    private final MockDto dto;
    private MockDataBase dataBase;

    public InsertOperation(MockDto dto) {
        this.dto = dto;
    }

    public void setDataBase(@NonNull MockDataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public String getOperationType() {
        return "Insert";
    }

    @Override
    public byte[] serialize() {
        return (getOperationType() + ": " + dto.email + ":" + dto.name).getBytes();
    }

    @Override
    public void replay() throws BackupException {
        if (dataBase == null) {
            throw new RuntimeException();
        }
        dataBase.insert(dto);
    }
}
