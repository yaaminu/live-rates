package com.backup;

import com.android.annotations.NonNull;

/**
 * An operation represent a piece of action that mutates
 * the dataStore. All implementations must be <a href=http://en.wikipedia.com/wiki/crdt>CRDT</a>
 * Created by yaaminu on 5/17/17.
 */
public interface Operation {
    /**
     * the type of operation
     *
     * @return the type of operation
     */
    @NonNull
    String getOperationType();

    /**
     * the serialized
     *
     * @return the serialized blob that can later be used to  re-construct this
     * object.
     */
    byte[] serialize();

    /**
     * This essentially repeats the operation. All implementations
     * must strive to be idempotent
     *
     * @throws BackupException
     */
    void replay() throws BackupException;
}
