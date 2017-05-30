package com.backup;


import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yaaminu on 5/21/17.
 */

public class LocalFileSystemStorage implements Storage {
    private static final String BACKUP_LOG_SUFFIX = "-backup.log";

    private final File dir;


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
        return new FileOutputStream(new File(dir, collectionName), true);
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull String collectionName) throws IOException {
        return new FileInputStream(new File(dir, collectionName));
    }

    public File getBackupFile(@NonNull String collectionName) {
        return new File(dir, collectionName);
    }

    @Override
    public long size(@NonNull String collectionName) throws IOException {
        return new File(dir, collectionName).length();
    }

    @Override
    public long lastModified(@NonNull String collectionName) throws IOException {
        return new File(dir, collectionName).lastModified();
    }
}
