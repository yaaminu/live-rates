package com.backup;

import com.android.annotations.NonNull;
import com.backup.annotations.LoggerIgnore;

import java.security.NoSuchAlgorithmException;

/**
 * Created by yaaminu on 5/17/17.
 */
public class LogEntry<T extends Operation> {
    public static final String FIELD_OP_CLAZZ = "opClazz";
    public static final String FIELD_HASH_SUM = "hashSum";
    public static final String FIELD_DATE_LOGGED = "dateLogged";
    public static final String FIELD_GROUP = "group";
    public static final String FIELD_SIZE = "size";
    public static final String FIELD_OP = "op";
    @NonNull
    private final String group;
    @NonNull
    private final String hashSum;
    private final long dateLogged;
    @NonNull
    private final T op;

    @LoggerIgnore
    private final long size;
    private static final int HASH_SIZE = 40;//sha1 check sum as hex_string  is 40 bytes long


    LogEntry(@NonNull String group, @NonNull T op, long dateLogged) {
        this.group = group;
        this.op = op;
        this.dateLogged = dateLogged;


        this.size = group.getBytes().length + HASH_SIZE +
                8/*sizeOf(dateLogged)*/ + 8 /*sizeOf(size)*/ + op.data().toString().getBytes().length;

        this.hashSum = calculateHashSum(this.group, this.size, this.op, this.dateLogged);
    }



    private static String calculateHashSum(String group, long size, Operation operation, long dateLogged) {
        try {
            //don't change the algorithm without changing the HASH_SIZE
            StringBuilder builder = new StringBuilder(group.length() + 8 + operation.data().toString().length() + 8);

            return Utils.sha1String(builder.append(group)
                    .append(String.valueOf(size))
                    .append(operation.data().toString())
                    .append(String.valueOf(dateLogged)).toString().getBytes());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    @NonNull
    public String getCollectionName() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntry logEntry = (LogEntry) o;

        return hashSum.equals(logEntry.hashSum);

    }

    @Override
    public int hashCode() {
        return hashSum.hashCode();
    }
}