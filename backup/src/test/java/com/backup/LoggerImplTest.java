package com.backup;


import android.support.annotation.NonNull;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by yaaminu on 5/20/17.
 */
@SuppressWarnings("EmptyCatchBlock")
public class LoggerImplTest {

    public static final int HEADER_SIZE = 8;

    @Test
    public void testConstructor() throws Exception {
        DependencyInjector injector = mock(DependencyInjector.class);
        Storage storage = mock(Storage.class);
        Serializer<LogEntry<? extends Operation>> serializer = mock(Serializer.class);
        String collectionName = "collectionName";
        checkArgHandling(injector, storage, serializer, collectionName);
    }

    private void checkArgHandling(DependencyInjector injector, Storage storage, Serializer<LogEntry<? extends Operation>> serializer, String collectionName) {
        try {
            new LoggerImpl(null, injector, serializer, storage);
            fail("must not accept null collectionName");
        } catch (IllegalArgumentException e) {

        }
        try {
            new LoggerImpl("", injector, serializer, storage);
            fail("must not accept empty collectionName");
        } catch (IllegalArgumentException e) {

        }
        try {
            new LoggerImpl("  ", injector, serializer, storage);
            fail("must not accept empty collectionName");
        } catch (IllegalArgumentException e) {

        }

        try {
            new LoggerImpl(collectionName, null, serializer, storage);
            fail("must not accept null injector");
        } catch (IllegalArgumentException e) {

        }

        try {
            new LoggerImpl(collectionName, injector, null, storage);
            fail("must not accept null serializer");
        } catch (IllegalArgumentException e) {

        }

        try {
            new LoggerImpl(collectionName, injector, serializer, null);
            fail("must not accept null storage");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void appendEntry() throws Exception {
        DependencyInjector injector = mock(DependencyInjector.class);
        Storage storage = new DummyStorage(new File("/tmp"));
        Serializer<LogEntry<? extends Operation>> serializer = new LogEntryGsonSerializer();
        String collectionName = "collectionName";

        File outFile = new File("/tmp/" + collectionName);
        FileUtils.deleteQuietly(outFile);
        assertFalse(outFile.exists());


        Logger logger = new LoggerImpl(collectionName, injector, serializer, storage);
        long beforeWrite = currentTimeMillis();
        long sizeSoFar = 0L;
        for (int i = 0; i < 100; i++) {
            LogEntry<DummyOp> entry = new LogEntry<>("group", new DummyOp(i), currentTimeMillis());
            sizeSoFar += serializer.serialize(entry).length + HEADER_SIZE; //the logger must add a header of 10 bytes
            logger.appendEntry(entry);
        }
        assertTrue(outFile.length() > 0);
        assertEquals("must write exactly " + sizeSoFar + " to outFile", sizeSoFar, outFile.length());
        assertTrue((beforeWrite / 1000) * 1000/*last modified is rounded to nearest 100*/ <= outFile.lastModified());
        FileUtils.deleteQuietly(outFile);
    }

    @Test
    public void retrieveAllEntries() throws Exception {
        DependencyInjector injector = mock(DependencyInjector.class);
        Storage storage = new DummyStorage(new File("/tmp"));
        Serializer<LogEntry<? extends Operation>> serializer = new LogEntryGsonSerializer();
        String collectionName = "collectionName";

        Logger logger = new LoggerImpl(collectionName, injector, serializer, storage);

        BackupStats stats = logger.stats();
        assertEquals(0, stats.getSize());
        assertEquals(0, stats.getLastModified());

        File outFile = new File("/tmp/" + collectionName);
        FileUtils.deleteQuietly(outFile);
        assertFalse(outFile.exists());


        long beforeWrite = currentTimeMillis();
        long sizeSoFar = 0L;
        File editingFile = new File("/tmp/editingfile");
        FileUtils.deleteQuietly(editingFile);
        assertFalse(editingFile.exists());

        for (int i = 0; i < 100; i++) {
            byte[] bytes = new Date().toString().getBytes();
            FileUtils.writeByteArrayToFile(editingFile, bytes, true);
            sizeSoFar += bytes.length;
            LogEntry<DateWriterOperation> entry = new LogEntry<>("group", new DateWriterOperation(editingFile, bytes), currentTimeMillis());
//            sizeSoFar += serializer.serialize(entry).length + 10; //the logger must add a header of 10 bytes
            logger.appendEntry(entry);
        }

        assertTrue(editingFile.exists());
        assertEquals(sizeSoFar, editingFile.length());
        FileUtils.deleteQuietly(editingFile);
        assertFalse(editingFile.exists());

        logger.retrieveAllEntries(new RestoreHandler(injector));
        verify(injector, times(100)).inject(any(DateWriterOperation.class));
        assertTrue(editingFile.exists());
        assertEquals(sizeSoFar, editingFile.length());
        FileUtils.deleteQuietly(outFile);
    }

    @Test
    public void stats() throws Exception {
        DependencyInjector injector = mock(DependencyInjector.class);
        Storage storage = new DummyStorage(new File("/tmp"));
        Serializer<LogEntry<? extends Operation>> serializer = new LogEntryGsonSerializer();
        String collectionName = "collectionName";

        File outFile = new File("/tmp/" + collectionName);
        FileUtils.deleteQuietly(outFile);
        assertFalse(outFile.exists());


        Logger logger = new LoggerImpl(collectionName, injector, serializer, storage);
        long beforeWrite = currentTimeMillis();
        long sizeSoFar = 0L;
        for (int i = 0; i < 100; i++) {
            LogEntry<DummyOp> entry = new LogEntry<>("group", new DummyOp(i), currentTimeMillis());
            sizeSoFar += serializer.serialize(entry).length + HEADER_SIZE; //the logger must add a header of 10 bytes
            logger.appendEntry(entry);
        }
        BackupStats stats = logger.stats();

        assertTrue(outFile.length() > 0);
        assertEquals("must write exactly " + sizeSoFar + " to outFile", sizeSoFar, outFile.length());
        assertTrue((beforeWrite / 1000) * 1000/*last modified is rounded to nearest 100*/ <= outFile.lastModified());
        assertEquals(stats.getLastModified(), outFile.lastModified());
        assertEquals(stats.getSize(), outFile.length());
        FileUtils.deleteQuietly(outFile);
    }

    @Test
    public void getInjector() throws Exception {
        DependencyInjector injector = mock(DependencyInjector.class);
        Storage storage = new DummyStorage(new File("/tmp"));
        Serializer<LogEntry<? extends Operation>> serializer = new LogEntryGsonSerializer();
        String collectionName = "collectionName";
        LoggerImpl logger = new LoggerImpl(collectionName, injector, serializer, storage);
        assertNotNull(logger.getInjector());
        assertSame(injector, logger.getInjector());

    }

    private static class DummyStorage implements Storage {

        private final File dir;

        public DummyStorage(File dir) {
            this.dir = dir;
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public OutputStream newAppendableOutPutStream(String collectionName) throws IOException {
            return new FileOutputStream(new File(dir, collectionName), true);
        }

        @Override
        public InputStream newInputStream(String collectionName) throws IOException {
            return new FileInputStream(new File(dir, collectionName));
        }

        @Override
        public long size(String collectionName) {
            return new File(dir, collectionName).length();
        }

        @Override
        public long lastModified(String collectionName) {
            return new File(dir, collectionName).lastModified();
        }
    }

    public static class RestoreHandler implements Logger.RestoreHandler {

        private final DependencyInjector injector;

        public RestoreHandler(DependencyInjector injector) {
            this.injector = injector;
        }

        @Override
        public void onPrepareRestore() {
            System.out.println("on preparing at  " + new Date());
        }

        @Override
        public void onEntry(@NonNull LogEntry logEntry) throws BackupException {
            System.out.println("restoring entry with checksum " + logEntry.getHashSum());
            Operation op = logEntry.getOp();
            injector.inject(op);
            op.replay();
        }

        @Override
        public void onRestoreComplete() {
            System.out.println("restore complete " + new Date());
        }

        @Override
        public void onRestoreError(BackupException e) {
            throw new RuntimeException(e); //force the test to fail
        }
    }

    private static final DependencyInjector injector = new DependencyInjector() {
        @Override
        public void inject(Operation operation) {

        }
    };
}