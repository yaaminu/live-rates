package com.backup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

/**
 * Created by yaaminu on 5/30/17.
 */

public class LockAwareOutPutStream extends OutputStream {

    @Nullable
    private Semaphore lock;
    @NonNull
    private final OutputStream outputStream;

    public LockAwareOutPutStream(@NonNull OutputStream out, @NonNull Semaphore lock) {
        this.outputStream = out;
        this.lock = lock;

    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    @Override
    public void write(@NonNull byte[] b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        //calling close multiple times could cause releasing the
        //lock more than once  times
        if (lock != null) {
            try {
                outputStream.close();
            } finally {
                lock.release();
                lock = null;
            }
        }
    }
}
