package com.backup;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * An interface for transforming POJO's to byte stream and back.
 * <p>
 * To correctly implement this interface:
 * <li><code>deserialize(serialize(obj)).equals(obj) must always be true</code> </li>
 * <li>serialize(null) must return an empty blob</li>
 * <li>deserialize(emptyBlob) must return null</li>
 * <p>
 * <li>you  must never serialize fields in the object that are marked with
 * {@link com.backup.annotations.LoggerIgnore}</li>
 * <p>
 *
 * @see com.backup.annotations.LoggerIgnore
 * @see Logger
 * Created by yaaminu on 5/17/17.
 */

public interface Serializer<T> {
    /**
     * transforms an object to an opaque blob
     *
     * @param object the object to be serialized
     * @return a blob representing a serialized form of the object, or an empty blob if the POJO
     * is null. Should never return null
     * @throws BackupException
     */
    @NonNull
    byte[] serialize(@Nullable T object) throws BackupException;

    /**
     * deserialize the blob and return it's corresponding POJO.
     * if the blob is empty, null should be returned
     *
     * @param blob the serialized form of the corresponding POJO.
     * @return the POJO or null if the blob is empty
     * @throws BackupException
     */
    @Nullable
    T deserialize(@NonNull byte[] blob) throws BackupException;

    /**
     * deserialize the blob region starting from offset to offset+length
     * and return it's corresponding POJO
     *
     * @param blob the serialized form of the corresponding POJO.
     * @return the POJO or null if the blob is empty
     * @throws BackupException
     * @throws IndexOutOfBoundsException if offset < 0 || length < 0 || (offset+length) > blob.length;
     */
    T deserialize(@NonNull byte[] blob, int offset, int length) throws BackupException;
}
