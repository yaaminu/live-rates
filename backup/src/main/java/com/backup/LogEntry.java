package com.backup;

import com.android.annotations.NonNull;

/**
 * Created by yaaminu on 5/17/17.
 */
public class LogEntry {
    @NonNull
    private final String group;
    @NonNull
    private final String hashSum;
    private final long dateLogged;
    @NonNull
    private final Operation op;

    private final long size;


    LogEntry(@NonNull String group, @NonNull Operation op, long dateLogged) {
        this.group = group;
        this.op = op;
        this.dateLogged = dateLogged;

        this.hashSum = calculateHashSum(group, op, dateLogged);

        this.size = group.getBytes().length + hashSum.getBytes().length +
                8/*sizeOf(dateLogged)*/ + op.serialize().length;
    }

    private static String calculateHashSum(String group, Operation operation, long dateLogged) {
        // FIXME: 5/17/17 calculate a real checksum
        return group + dateLogged;
    }

    @NonNull
    public String getGroup() {
        return group;
    }

    public long getDateLogged() {
        return dateLogged;
    }

    @NonNull
    public String getHashSum() {
        return hashSum;
    }

    public long getSize() {
        return size;
    }

    @NonNull
    public Operation getOp() {
        return op;
    }
}