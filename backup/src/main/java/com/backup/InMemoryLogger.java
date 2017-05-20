package com.backup;

/**
 * Created by yaaminu on 5/17/17.
 */

public abstract class InMemoryLogger implements Logger {
//    @NonNull
//    private final List<LogEntry> records;
//    @NonNull
//    private final DependencyInjector injector;
//    private long lastModified;
//
//    public InMemoryLogger(@NonNull DependencyInjector injector) {
//        records = new LinkedList<>();
//        lastModified = System.currentTimeMillis();
//        this.injector = injector;
//    }
//
//    @Override
//    public synchronized void appendEntry(@NonNull LogEntry logEntry) throws BackupException {
//        records.add(logEntry);
//        lastModified = System.currentTimeMillis();
//    }
//
//    @Override
//    public synchronized void retrieveAllEntries(@NonNull RestoreHandler handler) throws BackupException {
//        handler.onPrepareRestore();
//        for (LogEntry record : records) {
//            handler.onEntry(record);
//        }
//        handler.onRestoreComplete();
//    }
//
//    @Override
//    public BackupStats stats() throws BackupException {
//        long size = 0;
//        for (LogEntry entry : records) {
//            size += entry.getSize();
//        }
//        return new BackupStats(size, lastModified);
//    }
//
//    @Override
//    public DependencyInjector getInjector() {
//        return injector;
//    }
}
