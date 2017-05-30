package com.backup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by yaaminu on 5/30/17.
 */
public class LockAwareInputStreamTest {
    ByteArrayInputStream stream;
    Semaphore lock;
    private static final byte[] testBuffer = "testbuffer".getBytes();

    @Before
    public void setup() throws Exception {
        stream = new ByteArrayInputStream(testBuffer);
        lock = new Semaphore(2);
        stream = spy(stream);
        lock = spy(lock);
    }

    @Test
    public void read() throws Exception {
        LockAwareInputStream inputStream = new LockAwareInputStream(stream, lock);
        byte[] buf = new byte[testBuffer.length];
        for (int i = 0; i < testBuffer.length; i++) {
            buf[i] = (byte) inputStream.read();
        }
        assertArrayEquals(testBuffer, buf);
        //noinspection ResultOfMethodCallIgnored
        verify(stream, times(testBuffer.length)).read();
    }

    @Test
    public void read1() throws Exception {
        byte[] testBuffer = "testbuff".getBytes(); //the length must be evenly divisible by 2, see below

        stream = new ByteArrayInputStream(testBuffer);
        stream = Mockito.spy(stream);
        LockAwareInputStream inputStream = new LockAwareInputStream(stream, lock);

        byte[] buf = new byte[testBuffer.length];
        byte[] tmpBuf = new byte[2];
        for (int i = 0; i < testBuffer.length; i += 2) {
            int read = inputStream.read(tmpBuf);
            if (read != -1) {
                buf[i] = tmpBuf[0];
                buf[i + 1] = tmpBuf[1];
            }
        }
        assertArrayEquals(testBuffer, buf);
        //noinspection ResultOfMethodCallIgnored
        verify(stream, times(testBuffer.length / 2)).read(tmpBuf);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void read2() throws Exception {
        byte[] testBuffer = "testbuff".getBytes(); //the length must be evenly divisible by 2, see below

        stream = new ByteArrayInputStream(testBuffer);
        stream = Mockito.spy(stream);
        LockAwareInputStream inputStream = new LockAwareInputStream(stream, lock);

        byte[] buf = new byte[testBuffer.length];
        for (int i = 0; i < testBuffer.length; i += 2) {
            inputStream.read(buf, i, 2);
        }
        assertArrayEquals(testBuffer, buf);
        //noinspection ResultOfMethodCallIgnored
        verify(stream, times(1)).read(buf, 0, 2);
        verify(stream, times(1)).read(buf, 2, 2);
        verify(stream, times(1)).read(buf, 4, 2);
        verify(stream, times(1)).read(buf, 6, 2);
    }

    @Test
    public void close() throws Exception {
        LockAwareInputStream lockAwareOutPutStream = new LockAwareInputStream(stream, lock);
        lockAwareOutPutStream.close();
        //must release the lock when closed
        verify(lock, times(1)).release();
        //must close the underlying stream
        verify(stream, times(1)).close();

        //test behaviour on multiple streams
        lockAwareOutPutStream.close();
        //must close the underlying stream only once
        verify(stream, times(1)).close();
        //must release the lock only once
        verify(lock, times(1)).release();
        lockAwareOutPutStream.close();
        //must close the underlying stream only once
        verify(stream, times(1)).close();
        //must release the lock only once
        verify(lock, times(1)).release();
    }

}