package com.backup;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Transforms {@link LogEntry} into byte arrays  and back
 * Created by yaaminu on 5/18/17.
 */

public class LogEntryGsonSerializer implements Serializer<LogEntry<? extends Operation>> {

    private final Gson gson;

    LogEntryGsonSerializer() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LogEntry.class, new LogEntryTypeAdapter())
                .setExclusionStrategies(new LoggerIgnoreExclusionStrategy())
                .create();

    }

    @NonNull
    @Override
    public byte[] serialize(@Nullable LogEntry<? extends Operation> object) throws BackupException {
        if (object == null) return new byte[0];
        return gson.toJson(object, LogEntry.class).getBytes();
    }


    @Override
    @NonNull
    public LogEntry<? extends Operation> deserialize(@NonNull byte[] blob) throws BackupException {
        if (blob == null) throw new IllegalArgumentException("blob == null");
        return this.deserialize(blob, 0, blob.length);
    }

    @Override
    public LogEntry<? extends Operation> deserialize(@NonNull byte[] blob, int offset, int length) throws BackupException {
        if (blob == null) throw new IllegalArgumentException("blob == null");
        String json = new String(blob, offset, length);
        json = json.trim();
        try {
            return gson.fromJson(json, LogEntry.class);
        } catch (JsonSyntaxException e) {
            throw new BackupException(BackupException.EMALFORMEDINPUT, e.getMessage(), e);
        }
    }
}
