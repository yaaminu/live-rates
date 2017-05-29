package com.backup;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yaaminu on 5/18/17.
 */

public class LogEntryTypeAdapter extends TypeAdapter<LogEntry<? extends Operation>> {

    @NonNull
    private final Gson gson;

    public LogEntryTypeAdapter() {
        gson = new GsonBuilder()
                .create();
    }

    @Override
    public void write(@NonNull JsonWriter out, @Nullable LogEntry<? extends Operation> value) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException();
        }
        out.beginObject();
        if (value == null) {
            out.nullValue();
        } else {
            out.name(LogEntry.FIELD_HASH_SUM).value(value.getHashSum());
            out.name(LogEntry.FIELD_DATE_LOGGED).value(value.getDateLogged());
            out.name(LogEntry.FIELD_GROUP).value(value.getCollectionName());
            out.name(LogEntry.FIELD_SIZE).value(value.getSize());
            JsonObject data = value.getOp().data();
            JsonObject copy = new JsonObject();
            for (Map.Entry<String, JsonElement> jsonEntry : data.entrySet()) {
                copy.add(jsonEntry.getKey(), jsonEntry.getValue());
            }
            copy.addProperty(LogEntry.FIELD_OP_CLAZZ, value.getOp().getClass().getName());
            out.name(LogEntry.FIELD_OP).value(copy.toString());
        }
        out.endObject();
    }

    @Override
    public LogEntry<? extends Operation> read(JsonReader in) throws IOException {
        if (in == null) throw new IllegalArgumentException();
        if (in.peek() == JsonToken.NULL) {
            return null;
        }

        String hashSum = null, group = null;
        int size = 0;
        long dateLogged = 0;
        Operation op = null;
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case LogEntry.FIELD_HASH_SUM:
                    hashSum = in.nextString();
                    break;
                case LogEntry.FIELD_SIZE:
                    size = in.nextInt();
                    break;
                case LogEntry.FIELD_GROUP:
                    group = in.nextString();
                    break;
                case LogEntry.FIELD_DATE_LOGGED:
                    dateLogged = in.nextLong();
                    break;
                case LogEntry.FIELD_OP:
                    op = createOperation(in.nextString());
                    break;
                default:
                    break;
            }
        }
        LogEntry<Operation> logEntry = new LogEntry<>(group, op, dateLogged);
        checkIntegrity(hashSum, size, logEntry);
        in.endObject();
        return logEntry;
    }

    private void checkIntegrity(String hashSum, int size, LogEntry<Operation> logEntry) throws IOException {
        String actualHashSum = logEntry.getHashSum();
        if (!actualHashSum.equals(hashSum)) {
            throw new IOException("check sum mismatch. expected " + hashSum + " but was " + actualHashSum);
        }
        int actualSize = logEntry.getSize();
        if (actualSize != size) {
            throw new IOException("size mismatch. expected " + size + " but was " + actualSize);
        }
    }

    private Operation createOperation(String operationJson) {
        //noinspection TryWithIdenticalCatches
        try {
            JsonObject jsonObject = gson.fromJson(operationJson, JsonObject.class);
            String classPath = jsonObject.get(LogEntry.FIELD_OP_CLAZZ).getAsString();
            //noinspection unchecked
            Class<? extends Operation> op = (Class<? extends Operation>) Class.forName(classPath);
            Operation operation = op.newInstance();

            //remove it to avoid any conflict
            jsonObject.remove(LogEntry.FIELD_OP_CLAZZ);
            operation.setData(jsonObject);
            return operation;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
