package com.backup;


import android.support.annotation.NonNull;

import com.backup.annotations.LoggerIgnore;
import com.google.gson.JsonObject;

/**
 * Created by yaaminu on 5/17/17.
 */
public class InsertOperation implements Operation {

    private JsonObject payload;

    @LoggerIgnore
    private MockDataBase dataBase;

    public InsertOperation() {
    }

    public InsertOperation(MockDto dto) {
        payload = new JsonObject();
        payload.addProperty(MockDto.FIELD_EMAIL, dto.email);
        payload.addProperty(MockDto.FIELD_NAME, dto.name);
    }

    public void setDataBase(@NonNull MockDataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public JsonObject data() {
        return payload;
    }

    @Override
    public void setData(JsonObject object) {
        this.payload = object;
    }

    @Override
    public void replay() throws BackupException {
        if (dataBase == null) {
            throw new RuntimeException();
        }
        dataBase.insert(new MockDto(payload.get(MockDto.FIELD_NAME).getAsString(),
                payload.get(MockDto.FIELD_EMAIL).getAsString()));
    }
}
