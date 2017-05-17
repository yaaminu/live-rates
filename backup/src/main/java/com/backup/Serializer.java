package com.backup;

/**
 * Created by yaaminu on 5/17/17.
 */

public interface Serializer<E extends Operation> {
    byte[] serialize(E op) throws BackupException;

    E deserialize(byte[] blob) throws BackupException;
}
