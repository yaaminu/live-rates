package com.backup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by yaaminu on 5/30/17.
 */
public class LockAwareOutPutStreamTest {

    private OutputStream stream;
    private Semaphore lock;

    @Before
    public void setup() throws Exception {
        stream = new ByteArrayOutputStream();
        lock = new Semaphore(2);
        stream = spy(stream);
        lock = spy(lock);
    }

    @Test
    public void write() throws Exception {
        LockAwareOutPutStream outPutStream = new LockAwareOutPutStream(stream, lock);
        for (int i = 48; i <= 57; i++) { //let's write 0-9 to the file
            outPutStream.write(i);
        }
        //it must forward all writes to underlying out.
        for (int i = 48; i <= 57; i++) {
            verify(stream, times(1)).write(i);
        }
    }

    @Test
    public void write1() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(20);

        LockAwareOutPutStream outPutStream = new LockAwareOutPutStream(stream, lock);
        byte[] buf = new byte[20];
        for (int i = 48, cursor = 0; i <= 57; i++) { //let's write 0-9 delimited by a new line to the file
            buf[cursor++] = (byte) i;
            buf[cursor++] = 10; //LF
            outPutStream.write(new byte[]{(byte) i, 10});
        }
        //it must forward all writes to underlying out.
        Assert.assertArrayEquals(buf, stream.toByteArray());
    }

    @Test
    public void write2() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(20);

        LockAwareOutPutStream outPutStream = new LockAwareOutPutStream(stream, lock);
        byte[] buf = new byte[20];
        for (int i = 48, cursor = 0; i <= 57; i++) { //let's write 0-9 delimited by a new line to the file
            buf[cursor++] = (byte) i;
            buf[cursor++] = 10; //LF
            outPutStream.write(new byte[]{(byte) i, 10}, 0, 2);
        }
        //it must forward all writes to underlying out.
        Assert.assertArrayEquals(buf, stream.toByteArray());

        buf = new byte[10];
        stream.reset();
        outPutStream = new LockAwareOutPutStream(stream, lock);
        for (int i = 48, cursor = 0; i <= 57; i++) { //let's write 0-9 to the file
            buf[cursor++] = (byte) i;
            outPutStream.write(new byte[]{(byte) i, 10}, 0, 1);
        }
        //it must forward all writes to underlying out.
        Assert.assertArrayEquals(buf, stream.toByteArray());
    }

    @Test
    public void close() throws Exception {
        LockAwareOutPutStream lockAwareOutPutStream = new LockAwareOutPutStream(stream, lock);
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