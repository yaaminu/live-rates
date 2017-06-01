package com.backup;


import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

/**
 * Created by yaaminu on 5/21/17.
 */

public class LocalFileSystemStorage implements Storage {

    private final File dir;

    private static final Semaphore lock = new Semaphore(1, true);

    public LocalFileSystemStorage(@NonNull File dir) {
        checkArgs(dir);
        this.dir = dir;
    }

    private void checkArgs(File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("directory == null");
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IllegalArgumentException("failed to create directory");
            }
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("backup  dir already exists and it's not a directory");
        }
    }

    @NonNull
    @Override
    public OutputStream newAppendableOutPutStream(@NonNull String collectionName) throws IOException {
        return new LockAwareOutPutStream(new FileOutputStream(new File(dir, collectionName), true), lock);
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull String collectionName) throws IOException {
        return new LockAwareInputStream(new FileInputStream(new File(dir, collectionName)), lock);
    }

    public File getBackupFile(@NonNull String collectionName) {
        return new File(dir, collectionName);
    }

    @Override
    public long size(@NonNull String collectionName) throws IOException {
        try {
            lock.acquireUninterruptibly();
            return new File(dir, collectionName).length();
        } finally {
            lock.release();
        }
    }

    @Override
    public long lastModified(@NonNull String collectionName) throws IOException {
        try {
            lock.acquireUninterruptibly();
            return new File(dir, collectionName).lastModified();
        } finally {
            lock.release();
        }
    }
}
