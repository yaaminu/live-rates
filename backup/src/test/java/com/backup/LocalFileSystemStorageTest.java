package com.backup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by yaaminu on 5/21/17.
 */
@SuppressWarnings("EmptyCatchBlock")
public class LocalFileSystemStorageTest {

    @Test
    public void testConstructor() throws Exception {
        File dir = mock(File.class);
        when(dir.exists()).thenReturn(false);
        when(dir.isDirectory()).thenReturn(false);
        when(dir.mkdirs()).thenReturn(true);
        try {
            new LocalFileSystemStorage(null);
            fail("must not accept null file");
        } catch (IllegalArgumentException e) {

        }
        try {
            when(dir.exists()).thenReturn(true);
            when(dir.isDirectory()).thenReturn(false);
            new LocalFileSystemStorage(dir);
            fail("must throw when dir exist but it's a regular file");
        } catch (IllegalArgumentException e) {

        }
        try {
            when(dir.exists()).thenReturn(false);
            when(dir.mkdirs()).thenReturn(false);
            new LocalFileSystemStorage(dir);
            fail("must throw when dir does not exit and we are unable to create it");
        } catch (IllegalArgumentException e) {

        }


        //it must try creating the directory if it doesn't exist
        dir = mock(File.class);
        when(dir.exists()).thenReturn(false);
        when(dir.mkdirs()).thenReturn(true);
        when(dir.isDirectory()).thenReturn(true);
        new LocalFileSystemStorage(dir);
        verify(dir, times(1)).mkdirs();
    }

    @Test
    public void newAppendableOutPutStream() throws Exception {
        File dir = new File("/tmp");
        LocalFileSystemStorage storage = new LocalFileSystemStorage(dir);
        String collectionName = "somefile";
        File backupFileForCollection = new File(dir, collectionName);

        FileUtils.deleteQuietly(backupFileForCollection);
        assertFalse(backupFileForCollection.exists());

        //test that it appends instead of overwriting
        byte[] initialData = "some initial data".getBytes();
        FileUtils.writeByteArrayToFile(backupFileForCollection, initialData, false);

        OutputStream outputStream = storage.newAppendableOutPutStream(collectionName);
        assertNotNull(outputStream);

        assertTrue("it must create the backup file if it doesn't exist", backupFileForCollection.exists());
        assertEquals("it must append instead of overwriting", initialData.length, backupFileForCollection.length());

        byte[] nextData = "hello".getBytes();
        outputStream.write(nextData);
        assertEquals("it must append instead of overwriting", initialData.length + nextData.length, backupFileForCollection.length());
        ByteBuffer allBuffer = ByteBuffer.allocate((int) backupFileForCollection.length()).put(initialData).put(nextData);
        assertArrayEquals(allBuffer.array(), FileUtils.readFileToByteArray(backupFileForCollection));

        //close the stream and open again check it has not changed
        IOUtils.closeQuietly(outputStream);
        outputStream = storage.newAppendableOutPutStream(collectionName);
        assertArrayEquals(allBuffer.array(), FileUtils.readFileToByteArray(backupFileForCollection));

        FileUtils.deleteQuietly(backupFileForCollection);
        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void newInputStream() throws Exception {
        File dir = new File("/tmp");
        LocalFileSystemStorage storage = new LocalFileSystemStorage(dir);
        String collectionName = "somefile";
        File backupFileForCollection = new File(dir, collectionName);

        try {
            storage.newInputStream(null);
            fail("must not accept null args");
        } catch (NullPointerException e) {

        }
        try {
            storage.newInputStream("some non existent file");
            fail("must throw when file does not exist");
        } catch (IOException e) {

        }
        //this create the file to prevent FileNotFoundExceptions.
        new FileOutputStream(backupFileForCollection);
        assertNotNull(storage.newInputStream(collectionName));

        FileUtils.deleteQuietly(backupFileForCollection);

        assertFalse(backupFileForCollection.exists());


        byte[] data = "testing this rather simple example".getBytes();
        FileUtils.writeByteArrayToFile(backupFileForCollection,
                data);
        //check that our test data was correctly written to disk.
        assertTrue(data.length == backupFileForCollection.length());

        InputStream stream = storage.newInputStream(collectionName);
        assertNotNull(stream);
        assertArrayEquals("it must open the stream to appropriate backup file for collectionName",
                data, FileUtils.readFileToByteArray(backupFileForCollection));
        IOUtils.closeQuietly(stream);
        FileUtils.deleteQuietly(backupFileForCollection);
    }

    @Test
    public void size() throws Exception {
        File dir = new File("/tmp");
        LocalFileSystemStorage storage = new LocalFileSystemStorage(dir);
        String collectionName = "somefile";
        File backupFileForCollection = new File(dir, collectionName);
        FileUtils.deleteQuietly(backupFileForCollection);
        assertFalse(backupFileForCollection.exists());

        assertEquals(0, backupFileForCollection.length());
        assertEquals(0, backupFileForCollection.lastModified());
        assertEquals(0, storage.size(collectionName));

        byte[] testBuffer = "test".getBytes();
        OutputStream stream = null;
        ByteBuffer buffer = ByteBuffer.allocate(100 * testBuffer.length);
        for (int i = 0; i < 100; i++) {
            stream = storage.newAppendableOutPutStream(collectionName);
            IOUtils.write(testBuffer, stream);
            buffer.put(testBuffer);
            IOUtils.closeQuietly(stream);
            assertEquals(testBuffer.length * (i + 1), storage.size(collectionName));
        }

        InputStream inStream = storage.newInputStream(collectionName);
        assertArrayEquals(buffer.array(), IOUtils.toByteArray(inStream));
        assertEquals(100 * testBuffer.length, storage.size(collectionName));
        IOUtils.closeQuietly(inStream);
    }

    @Test
    public void lastModified() throws Exception {
        File dir = new File("/tmp");
        LocalFileSystemStorage storage = new LocalFileSystemStorage(dir);
        String collectionName = "somefile";
        File backupFileForCollection = new File(dir, collectionName);
        FileUtils.deleteQuietly(backupFileForCollection);
        assertFalse(backupFileForCollection.exists());

        assertEquals(0, backupFileForCollection.length());
        assertEquals(0, backupFileForCollection.lastModified());
        assertEquals(0, storage.lastModified(collectionName));
        OutputStream stream;
        long lastModified = currentTimeMillis();
        for (int i = 0; i < 2; i++) {
            stream = storage.newAppendableOutPutStream(collectionName);
            IOUtils.write("hello".getBytes(), stream);
            IOUtils.closeQuietly(stream);
            assertEquals((lastModified / 1000) * 1000, storage.lastModified(collectionName));
            Thread.sleep(1100);
            lastModified = currentTimeMillis();
        }
    }
}