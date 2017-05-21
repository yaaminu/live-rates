package com.backup;

import com.android.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An abstraction for writing/reading byte streams to/from a file storage
 */
public interface Storage {
    /**
     * return a new {@link OutputStream} to the resource with name
     * {@code collectionName}. The stream must be opened such that new data
     * can be appended instead of overwriting..
     *
     * @param collectionName the name of the collection
     * @return the outPutStream.
     * @throws IOException when it can't get the stream
     */
    @NonNull
    OutputStream newAppendableOutPutStream(String collectionName) throws IOException;

    /**
     * return a new {@link InputStream} to the resource with name
     * {@code collectionName}
     *
     * @param collectionName the name of the collection
     * @return the inputStream.
     * @throws IOException when it can't get the stream
     */
    @NonNull
    InputStream newInputStream(String collectionName) throws IOException;

    /**
     * return the size of the backup for {@code collectionName}
     *
     * @param collectionName the name of the collection.
     * @return the size of the collection.
     */
    long size(String collectionName);

    /**
     * returns the last time this collection was modified
     *
     * @param collectionName the name of the collection.
     * @return the lastModified
     */
    long lastModified(String collectionName);
}
