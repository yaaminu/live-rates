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
import static org.junit.Assert.assertNotNull;
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
        WellImplementedLogger logger = new WellImplementedLogger();

        try {
            BackupManager.getInstance(null);
            fail("must not accept null  logger");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        BackupManager instance = BackupManager.getInstance(logger);
        assertNotNull("Should never return null", instance);
        assertEquals("Should always return the same backup manager for the same logger instance",
                instance, BackupManager.getInstance(logger));

        try {
            //since the backup manager uses a HashMap to implement it's
            //singleton per logger, poorly implemented
            //equals/hashcode could lead to really dire consequences where
            //a different logger will be returned which can easily lead to
            //corrupting the data of the mistaken logger as it will mix
            //new unrelated data to it's log leading to parsing errors etc.
            //this test test that we are able to check  that and fail fast
            //if such situations are encountered.

            BackupManager.getInstance(new PoorlyImplementedLogger1());
            BackupManager.getInstance(new PoorlyImplementedLogger2());
            fail("must catch poor logger implementations and fail fast");
        } catch (RuntimeException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

    }

    @Test
    public void log() throws Exception {
        WellImplementedLogger logger = new WellImplementedLogger();
        assertEquals(logger.entries.size(), 0); //check to ensure things are alright

        BackupManager manager = BackupManager.getInstance(logger);

        checkInvalidArgsHandling(manager);

        Operation mock = PowerMockito.mock(Operation.class);
        when(mock.data()).thenReturn(new JsonObject());
        manager.log("group", mock, System.currentTimeMillis());
        assertEquals("must append to the log", logger.entries.size(), 1);

        for (int i = 0; i < 10; i++) {
            mock = PowerMockito.mock(Operation.class);
            when(mock.data()).thenReturn(new JsonObject());
            manager.log("group", mock, System.currentTimeMillis());
        }
        assertEquals("must append 10 more entries to the log", logger.entries.size(), 11);
    }

    private void checkInvalidArgsHandling(BackupManager manager) throws BackupException {
        try {
            manager.log(null, null, 0);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            manager.log(null, PowerMockito.mock(Operation.class), -1);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            manager.log("", PowerMockito.mock(Operation.class), 0);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        try {
            manager.log("group", null, 0);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            manager.log("group", PowerMockito.mock(Operation.class), -1);
            fail("must reject null args");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
    }

    @Test
    public void restore() throws Exception {
        WellImplementedLogger logger = new WellImplementedLogger();
        logger = PowerMockito.spy(logger);
        BackupManager.ProgressListener listener = PowerMockito.mock(BackupManager.ProgressListener.class);
        BackupManager manager = BackupManager.getInstance(logger);
        InsertOperation operation = mock(InsertOperation.class);
        when(operation.data()).thenReturn(new JsonObject());

        for (int i = 0; i < 10; i++) {
            manager.log("group", operation, System.currentTimeMillis());
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
        WellImplementedLogger logger = new WellImplementedLogger();
        BackupManager manager = BackupManager.getInstance(logger);
        InsertOperation operation = mock(InsertOperation.class);
        when(operation.data()).thenReturn(new JsonObject());
        for (int i = 0; i < 10; i++) {
            manager.log("group", operation, System.currentTimeMillis());
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

    public static class WellImplementedLogger implements Logger {
        List<LogEntry> entries;
        private long lastModified;

        public WellImplementedLogger() {
            entries = new LinkedList<>();
            lastModified = System.currentTimeMillis();
        }

        @Override
        public BackupStats stats() throws BackupException {
            long size = 0;
            for (LogEntry entry : entries) {
                size += entry.getSize();
            }
            return new BackupStats(size, lastModified);
        }

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
        public void retrieveAllEntries(@NonNull RestoreHandler handler) throws BackupException {
            handler.onPrepareRestore();
            for (LogEntry entry : entries) {
                handler.onEntry(entry);
            }
            handler.onRestoreComplete();
        }
    }

    private static class PoorlyImplementedLogger1 implements Logger {
        @Override
        public void appendEntry(@NonNull LogEntry logEntry) throws BackupException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void retrieveAllEntries(@NonNull RestoreHandler handler) throws BackupException {
            throw new UnsupportedOperationException();
        }

        @Override
        public BackupStats stats() throws BackupException {
            return null;
        }

        @Override
        public DependencyInjector getInjector() {
            return INJECTOR;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            return true;
        }


        @Override
        public int hashCode() {
            return 1;
        }
    }

    private static class PoorlyImplementedLogger2 implements Logger {
        @Override
        public void appendEntry(@NonNull LogEntry logEntry) throws BackupException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void retrieveAllEntries(@NonNull RestoreHandler handler) throws BackupException {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            return true;
        }

        @Override
        public BackupStats stats() throws BackupException {
            return null;
        }

        @Override
        public DependencyInjector getInjector() {
            return INJECTOR;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    private static DependencyInjector INJECTOR = new DependencyInjector() {
        @Override
        public void inject(Operation operation) {
            if (operation instanceof InsertOperation) {
                ((InsertOperation) operation)
                        .setDataBase(new MockDataBase(BackupManager.getInstance(new WellImplementedLogger())));
            }
        }
    };
}