package com.backup;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

/**
 * Created by yaaminu on 5/30/17.
 */

public class LockAwareInputStream extends InputStream {
    private final InputStream in;
    private Semaphore lock;

    public LockAwareInputStream(InputStream in, Semaphore lock) {
        this.in = in;
        this.lock = lock;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
//        //calling close multiple times could cause releasing the
//        //lock more than once  times
        if (lock != null) {
            try {
                in.close();
            } finally {
                lock.release();
                lock = null;
            }
        }
    }
}
