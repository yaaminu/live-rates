package com.backup;


import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by yaaminu on 5/17/17.
 */
public class BackupManagerTest {

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    @Test
    public void testGetInstance() throws Exception {
        LoggerImplTest logger = new LoggerImplTest("group");

        try {
            BackupManager.getInstance(null);
            fail("must not accept null  logger");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
    }

    @Test
    public void log() throws Exception {
        LoggerImplTest logger = new LoggerImplTest("group");
        assertEquals(logger.entries.size(), 0); //check to ensure things are alright

        BackupManager manager = BackupManager.getInstance(logger);

        checkInvalidArgsHandling(manager);

        Operation mock = PowerMockito.mock(Operation.class);
        when(mock.data()).thenReturn(new JsonObject());
        manager.log(mock, System.currentTimeMillis());
        assertEquals("must append to the log", logger.entries.size(), 1);

        for (int i = 0; i < 10; i++) {
            mock = PowerMockito.mock(Operation.class);
            when(mock.data()).thenReturn(new JsonObject());
            manager.log(mock, System.currentTimeMillis());
        }
        assertEquals("must append 10 more entries to the log", logger.entries.size(), 11);
    }

    private void checkInvalidArgsHandling(BackupManager manager) throws BackupException {
        try {
            manager.log(null, 0);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            manager.log(PowerMockito.mock(Operation.class), -1);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }


        try {
            manager.log(null, 0);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            manager.log(PowerMockito.mock(Operation.class), -1);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
    }

    @Test
    public void restore() throws Exception {
        LoggerImplTest logger = new LoggerImplTest("group");
        logger = PowerMockito.spy(logger);
        BackupManager.ProgressListener listener = PowerMockito.mock(BackupManager.ProgressListener.class);
        BackupManager manager = BackupManager.getInstance(logger);
        InsertOperation operation = mock(InsertOperation.class);
        when(operation.data()).thenReturn(new JsonObject());

        for (int i = 0; i < 10; i++) {
            manager.log(operation, System.currentTimeMillis());
        }
        manager.restore(listener);
        verify(listener, times(10)).onProgress(anyLong(), anyLong());
        verify(operation, times(10)).replay();
        //test the dependency injector
        verify(operation, times(10)).setDataBase(Mockito.any(MockDataBase.class));
        verify(logger, times(1)).retrieveAllEntries(Mockito.any(Logger.RestoreHandler.class));

        //must accept null listeners but it should use the default listener
        manager.restore(null);

        verify(logger, times(2)).retrieveAllEntries(Mockito.any(Logger.RestoreHandler.class));
    }

    @Test
    public void stats() throws Exception {
        LoggerImplTest logger = new LoggerImplTest("group");
        BackupManager manager = BackupManager.getInstance(logger);
        InsertOperation operation = mock(InsertOperation.class);
        when(operation.data()).thenReturn(new JsonObject());
        for (int i = 0; i < 10; i++) {
            manager.log(operation, System.currentTimeMillis());
        }
        BackupStats stats = manager.stats();
        assertEquals(stats.getLastModified(), logger.lastModified);
        assertEquals(stats.getSize(), logger.stats().getSize());
    }

    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    public static class LoggerImplTest implements Logger {
        private final String collectionName;
        List<LogEntry> entries;
        private long lastModified;

        public LoggerImplTest(String collectionName) {
            entries = new LinkedList<>();
            lastModified = System.currentTimeMillis();
            this.collectionName = collectionName;
        }

        @NonNull
        @Override
        public BackupStats stats() throws BackupException {
            long size = 0;
            for (LogEntry entry : entries) {
                size += entry.getSize();
            }
            return new BackupStats(size, lastModified);
        }

        @NonNull
        @Override
        public DependencyInjector getInjector() {
            return INJECTOR;
        }

        @Override
        public void appendEntry(@NonNull LogEntry logEntry) throws BackupException {
            entries.add(logEntry);
            lastModified = System.currentTimeMillis();
        }

        @Override
        public String getCollectionName() {
            return collectionName;
        }

        @Override
        public void retrieveAllEntries(@NonNull RestoreHandler handler) throws BackupException {
            handler.onPrepareRestore();
            for (LogEntry entry : entries) {
                handler.onEntry(entry);
            }
            handler.onRestoreComplete();
        }
    }

    private static DependencyInjector INJECTOR = new DependencyInjector() {
        @Override
        public void inject(Operation operation) {
            if (operation instanceof InsertOperation) {
                ((InsertOperation) operation)
                        .setDataBase(new MockDataBase(BackupManager.getInstance(new LoggerImplTest("group"))));
            }
        }
    };
}