package com.backup;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by yaaminu on 5/18/17.
 */
public class FileSystemLoggerTest {


    @Test
    public void testCreate() throws Exception {
        File testFile = PowerMockito.mock(File.class);
        Serializer<LogEntry<? extends Operation>> serializer = PowerMockito.mock(Serializer.class);
        DependencyInjector injector = PowerMockito.mock(DependencyInjector.class);
        checkInvalidArgsHandling(testFile, serializer, injector);

        try {
            when(testFile.exists()).thenReturn(false);
            FileSystemLogger.create(testFile, injector, serializer);
            when(testFile.exists()).thenReturn(true);
            when(testFile.isDirectory()).thenReturn(false);
            when(testFile.mkdirs()).thenReturn(false);
            FileSystemLogger.create(testFile, injector, serializer);
            fail("must reject invalid file arguments");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        try {
            when(testFile.exists()).thenReturn(true);
            FileSystemLogger.create(testFile, injector, serializer);
            when(testFile.exists()).thenReturn(true);
            when(testFile.isDirectory()).thenReturn(false);
            fail("must throw when the backup dir already exists and it's not a dir");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        File realFile = new File("/tmp/foo" + new SecureRandom().nextLong());
        if (realFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            realFile.delete();
        }
        //for test correctness
        assertFalse("file must not exist", realFile.exists());
        FileSystemLogger logger =
                FileSystemLogger.create(realFile, PowerMockito.mock(DependencyInjector.class), serializer);
        assertTrue("it must create the directory if it does not exist", realFile.isDirectory());
        assertNotNull(logger);

        //noinspection ResultOfMethodCallIgnored
        realFile.delete();
    }

    private void checkInvalidArgsHandling(File testFile, Serializer<LogEntry<? extends Operation>> serializer, DependencyInjector injector) {
        try {
            FileSystemLogger.create(null, null, null);
            fail("must reject null arguments");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            FileSystemLogger.create(testFile, null, null);
            fail("must reject null arguments");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            FileSystemLogger.create(null, injector, null);
            fail("must reject null arguments");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            FileSystemLogger.create(null, injector, serializer);
            fail("must reject null arguments");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            FileSystemLogger.create(new File("/tmp"), injector, null);
            fail("must reject null arguments");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
    }

    @Test
    public void testHashCodeBehavior() throws Exception {
        //we want to make sure that we are implementing our hashcode/equals well
        //as it's a requirement.
        DependencyInjector injector = PowerMockito.mock(DependencyInjector.class);
        Serializer<LogEntry<? extends Operation>> serializer = PowerMockito.mock(Serializer.class);
        File dir = PowerMockito.mock(File.class);
        when(dir.isDirectory()).thenReturn(true);
        when(dir.exists()).thenReturn(true);
        when(dir.mkdirs()).thenReturn(true);
        when(dir.getAbsolutePath()).thenReturn("/path/to/file");
        BackupManager manager = BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer));
        assertNotNull(manager);
        //we are checking that so far as we are logging to the same file, we should  use the same
        //Backup manager
        assertSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));
        assertSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));
        assertSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));
        assertSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));
        assertSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));
        assertSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));

        //we are checking that when an instance correctly implements equals/hashCode, we correctly
        //return a different backup manager
        when(dir.getAbsolutePath()).thenReturn("/path/to/file2");
        assertNotSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));

        when(dir.getAbsolutePath()).thenReturn("/path/to/file3");
        assertNotSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));

        when(dir.getAbsolutePath()).thenReturn("/path/to/file4");
        assertNotSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));

        when(dir.getAbsolutePath()).thenReturn("/path/to/file5");
        assertNotSame(manager, BackupManager.getInstance(FileSystemLogger.create(dir, injector, serializer)));
    }

    @Test
    public void appendEntry() throws Exception {
        File backupDir = new File("/tmp/backups");

        /// /the logger will not use it when appending
        DependencyInjector injector = PowerMockito.mock(DependencyInjector.class);
        LogEntryGsonSerializer serializer = new LogEntryGsonSerializer();
        FileSystemLogger logger = FileSystemLogger.create(backupDir, injector, serializer);

        String collectionName = "files";
        File backupFile = logger.getBackupFile(collectionName);


        FileUtils.deleteDirectory(backupDir);

        assertFalse(backupFile.exists());
        File testDir = new File("/tmp/testfiles/");
        FileUtils.deleteDirectory(testDir);
        assertFalse(testDir.exists());

        File testBackupFile = new File(testDir, "testbackup.log");
        assertFalse(testBackupFile.exists());

        for (int i = 0; i < 100; i++) {
            File testFile = new File(testDir, "testfile" + i);
            byte[] blob = (testFile.getAbsolutePath() + ":" + "i").getBytes();
            writeByteArrayToFile(testFile, blob, false);

            long dateLogged = currentTimeMillis();
            LogEntry<? extends Operation> entry =
                    new LogEntry<>(collectionName, new FileCreationOperation(testFile, blob), dateLogged);

            //generate the expected buffer
            byte[] serialized = serializer.serialize(entry);
            ByteBuffer buffer = ByteBuffer.allocate(serialized.length + 10); //the 10 is the header.
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.put((byte) 0).put((byte) 0)
                    .putLong(serialized.length)
                    .put(serialized);

            //append the generated buffer (we expect the logger to generate this same too) to the
            //test backup log file
            writeByteArrayToFile(testBackupFile, buffer.array(), true);

            logger.appendEntry(entry);
            assertEquals(testBackupFile.length(), backupFile.length());
            //noinspection ResultOfMethodCallIgnored
        }
        assertTrue(backupFile.exists());
        assertTrue(testBackupFile.exists());
        assertEquals(testBackupFile.length(), backupFile.length());
        assertArrayEquals(sha1(testBackupFile), sha1(backupFile));
        FileUtils.deleteDirectory(backupDir);
        FileUtils.deleteDirectory(backupFile);
        FileUtils.deleteDirectory(testDir);
    }

    private byte[] sha1(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("sha1");
        digest.update(FileUtils.readFileToByteArray(file));
        return digest.digest();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void retrieveAllEntries() throws Exception {
        File backupDir = new File("/tmp/backups");

        FileSystemLogger logger = FileSystemLogger.create(backupDir, injector, new LogEntryGsonSerializer());

        try {
            logger.retrieveAllEntries(null);
            fail("FileSystemLogger must not accept null restore handlers in retrieveAllEntries()");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        String collectionName = "files";
        File backupFile = logger.getBackupFile(collectionName);
        backupFile.delete();
        assertFalse(backupFile.exists());
        List<Pair<File, byte[]>> testFiles = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            File testFile = new File("/tmp/testfiles/testfile" + i);
            byte[] blob = (testFile.getAbsolutePath() + ":" + "i").getBytes();
            writeByteArrayToFile(testFile, blob, false);
            logger.appendEntry(new LogEntry<>(collectionName, new FileCreationOperation(testFile, blob), currentTimeMillis()));
            testFiles.add(Pair.create(testFile, blob));
        }
        assertTrue(backupFile.exists());

        for (Pair<File, byte[]> testFile : testFiles) {
            testFile.first.delete();
        }
        logger.retrieveAllEntries(new FileCreationLoggerRestoreHandler(injector));

        for (Pair<File, byte[]> testFile : testFiles) {
            assertTrue(testFile.first.exists());
            assertEquals(testFile.second.length, testFile.first.length());
            assertArrayEquals(testFile.second, FileUtils.readFileToByteArray(testFile.first));
        }

        backupFile.delete();
        backupDir.delete();
    }

    @Test
    public void retrieveAllEntries2() throws Exception {
        File backupDir = new File("/tmp/backups");

        FileSystemLogger logger = FileSystemLogger.create(backupDir, injector, new LogEntryGsonSerializer());

        try {
            logger.retrieveAllEntries(null);
            fail("FileSystemLogger must not accept null restore handlers in retrieveAllEntries()");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        String collectionName = "files";
        File backupFile = logger.getBackupFile(collectionName);
        FileUtils.deleteQuietly(backupFile);
        assertFalse(backupFile.exists());
        File testFile = new File("/tmp/testfiles/editingTesFile"),
                secondTestFile = new File("/tmp/testfiles/editingTesFile2");
        FileUtils.deleteQuietly(testFile);
        FileUtils.deleteQuietly(secondTestFile);
        long size = 0;
        for (int i = 1; i < 100; i++) {
            byte[] blob = ("line " + i + "\n").getBytes();
            size += blob.length;
            writeByteArrayToFile(testFile, blob, true);
            logger.appendEntry(new LogEntry<>(collectionName, new DateWriterOperation(testFile, blob), currentTimeMillis()));
        }
        assertTrue(backupFile.exists());
        assertEquals(size, testFile.length());

        FileUtils.moveFile(testFile, secondTestFile);
        FileUtils.deleteQuietly(testFile);
        assertFalse(testFile.exists());

        //after this call the testFile should be reconstructed from the backup log
        logger.retrieveAllEntries(new FileCreationLoggerRestoreHandler(injector));

        assertTrue(testFile.exists());
        assertArrayEquals(sha1(testFile), sha1(secondTestFile));

        FileUtils.deleteQuietly(testFile);
        FileUtils.deleteQuietly(secondTestFile);
        FileUtils.deleteDirectory(backupDir);
    }

    @Test
    public void stats() throws Exception {
        File dir = new File("/tmp/test/");
        FileUtils.deleteDirectory(dir);
        LogEntryGsonSerializer serializer = new LogEntryGsonSerializer();
        FileSystemLogger logger = FileSystemLogger.create(dir, injector, serializer);
        assertNotNull(logger.stats());
        assertEquals(0, logger.stats().getSize());
        assertEquals(0, logger.stats().getLastModified());

        long size = 0;
        File testFile = new File("/tmp/test/file");
        String collectionName = "files";
        long lastModified =
                currentTimeMillis();
        for (int i = 1; i < 100; i++) {
            byte[] blob = ("line " + i + "\n").getBytes();
            LogEntry<DateWriterOperation> logEntry =
                    new LogEntry<>(collectionName, new DateWriterOperation(testFile, blob), lastModified);
            writeByteArrayToFile(testFile, blob, true);
            /*the logger appends a 10 byte header. check the source*/
            size += (serializer.serialize(logEntry).length + 10);
            logger.appendEntry(logEntry);
        }
        BackupStats stats = logger.stats();
        assertNotNull(stats);
        assertEquals(size, stats.getSize());
        System.out.println((lastModified / 1000) * 1000);
        System.out.println(stats.getLastModified());
        //File#lastModified rounds to the nearest thousand so we have to
        //do same to our version of lastModified
        assertTrue((lastModified / 1000) * 1000 <= stats.getLastModified());
        assertEquals(logger.getBackupFile(collectionName).lastModified(), stats.getLastModified());
        FileUtils.deleteDirectory(dir);
        assertFalse(dir.exists());
    }

    @Test
    public void getInjector() throws Exception {
        File dir = PowerMockito.mock(File.class);
        when(dir.exists()).thenReturn(true);
        when(dir.isDirectory()).thenReturn(true);
        Serializer<LogEntry<?>> serializer = PowerMockito.mock(Serializer.class);
        FileSystemLogger logger = FileSystemLogger.create(dir, injector, serializer);

        assertSame(logger.getInjector(), injector);
    }

    private static class FileCreationOperation implements Operation {

        @Nullable
        private JsonObject data;

        /*
         required no arg constructor
         */
        public FileCreationOperation() {
            this.data = new JsonObject();
        }

        public FileCreationOperation(File file, byte[] blob) {
            this.data = new JsonObject();
            this.data.addProperty("path", file.getAbsolutePath());
            this.data.addProperty("lastModified", file.lastModified());
            this.data.addProperty("blob", org.apache.commons.codec.binary.Base64.encodeBase64String(blob));
        }

        @NonNull
        @Override
        public JsonObject data() {
            return data;
        }

        @Override
        public void setData(@NonNull JsonObject object) {
            this.data = object;
        }

        @Override
        public void replay() throws BackupException {
            if (data == null) {
                throw new IllegalStateException("replay invoked while data is not yet available");
            }
            File file = new File(data.get("path").getAsString());
            byte[] blob = org.apache.commons.codec.binary.Base64.decodeBase64(data().get("blob").getAsString());
            try {
                writeByteArrayToFile(file, blob, false);
                if (!file.setLastModified(data.get("lastModified").getAsLong())) {
                    throw new BackupException(BackupException.EIOERROR, "failed to update last modified of the file", null);
                }
            } catch (IOException e) {
                throw new BackupException(BackupException.EIOERROR, e.getMessage(), e);
            }
        }
    }

    private static class DateWriterOperation implements Operation {

        @Nullable
        private JsonObject data;

        public DateWriterOperation() {
        }

        public DateWriterOperation(File file, byte[] blob) {
            this.data = new JsonObject();
            this.data.addProperty("path", file.getAbsolutePath());
            this.data.addProperty("lastModified", file.lastModified());
            this.data.addProperty("blob", org.apache.commons.codec.binary.Base64.encodeBase64String(blob));
        }

        @NonNull
        @Override
        public JsonObject data() {
            return data;
        }

        @Override
        public void setData(@NonNull JsonObject object) {
            this.data = object;
        }

        @Override
        public void replay() throws BackupException {
            if (data == null) {
                throw new IllegalStateException("replay invoked while data is not yet available");
            }
            File file = new File(data.get("path").getAsString());
            byte[] blob = org.apache.commons.codec.binary.Base64.decodeBase64(data().get("blob").getAsString());
            try {
                writeByteArrayToFile(file, blob, true);
                if (!file.setLastModified(data.get("lastModified").getAsLong())) {
                    throw new BackupException(BackupException.EIOERROR, "failed to update last modified of the file", null);
                }
            } catch (IOException e) {
                throw new BackupException(BackupException.EIOERROR, e.getMessage(), e);
            }
        }
    }

    private static class FileCreationLoggerRestoreHandler implements Logger.RestoreHandler {

        private final DependencyInjector injector;

        public FileCreationLoggerRestoreHandler(DependencyInjector injector) {
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