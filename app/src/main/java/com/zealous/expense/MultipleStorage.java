package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.backup.GoogleDriveStorage;
import com.backup.LocalFileSystemStorage;
import com.backup.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yaaminu on 5/30/17.
 */

public class MultipleStorage implements Storage {

    private final Storage storage, googleDriveStorage;

    @Nullable
    private OutputStream localOut, googleDriveOut;

    public MultipleStorage(@Nullable LocalFileSystemStorage storage,
                           @Nullable GoogleDriveStorage googleDriveStorage) {
        this.storage = storage;
        this.googleDriveStorage = googleDriveStorage;
    }

    @NonNull
    @Override
    public OutputStream newAppendableOutPutStream(String collectionName) throws IOException {
        if (storage != null) {
            localOut = this.storage.newAppendableOutPutStream(collectionName);
        }
        if (googleDriveStorage != null) {
            this.googleDriveOut = googleDriveStorage.newAppendableOutPutStream(collectionName);
        }

        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (localOut != null) {
                    localOut.write(b);
                }
                if (googleDriveOut != null) {
                    googleDriveOut.write(b);
                }
            }

            @Override
            public void write(@NonNull byte[] b) throws IOException {
                if (localOut != null) {
                    localOut.write(b);
                }
                if (googleDriveOut != null) {
                    googleDriveOut.write(b);
                }
            }

            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                if (localOut != null) {
                    localOut.write(b, off, len);
                }
                if (googleDriveOut != null) {
                    googleDriveOut.write(b, off, len);
                }
            }

            @Override
            public void close() throws IOException {
                if (localOut != null) {
                    localOut.close();
                    localOut = null;
                }
                if (googleDriveOut != null) {
                    googleDriveOut.close();
                    googleDriveOut = null;
                }
            }
        };
    }

    @NonNull
    @Override
    public InputStream newInputStream(String collectionName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long size(String collectionName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long lastModified(String collectionName) throws IOException {
        throw new UnsupportedOperationException();
    }
}
