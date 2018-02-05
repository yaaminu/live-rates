package com.backup;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A backup manager is a simple utility for backing up files in an incremental
 * fashion instead of the rather inefficient whole database file backup
 * which is really wasteful and fragile.
 *
 * @see Logger
 * @see Operation
 * @see LogEntry
 */
@SuppressWarnings("JavaDoc")
public class BackupManager {


    @NonNull
    private final Logger logger;

    private static class RestoreHandlerImpl implements Logger.RestoreHandler {
        private final ProgressListener listener;
        private final long size;
        private final Logger logger;
        private long processed;

        RestoreHandlerImpl(Logger logger, long size, @NonNull ProgressListener listener) {
            this.listener = listener;
            this.size = size;
            this.processed = 0;
            this.logger = logger;
        }

        @Override
        public void onPrepareRestore() {
            listener.onStart(size);
        }

        @Override
        public void onEntry(@NonNull LogEntry logEntry) throws BackupException {
            //do nothing now
            Operation op = logEntry.getOp();
            logger.getInjector().inject(op);
            op.replay();
            listener.onProgress(size, processed += logEntry.getSize());
        }

        @Override
        public void onRestoreComplete() {
            listener.done(null);
        }

        @Override
        public void onRestoreError(BackupException e) {
            listener.done(e);
        }
    }

    private BackupManager(@NonNull Logger logger) {
        this.logger = logger;
    }


    /**
     * @return the {@link BackupManager}. never null
     */
    @NonNull
    public static synchronized <T extends Logger> BackupManager getInstance(@NonNull T logger) {
        return new BackupManager(logger);
    }


    /**
     * logs an operation to the backup log.
     *
     * @param operation the operation that happened
     * @throws BackupException
     */
    public synchronized void log(@NonNull Operation operation, long timestamp) throws BackupException {

        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp < 0");
        }
        logger.appendEntry(new LogEntry<>(logger.getCollectionName(), operation, timestamp));
    }

    /**
     * restores all backed up data. This is a potentially long
     * running operation
     */
    public synchronized void restore(@Nullable ProgressListener listener) {
        if (listener == null) {
            listener = DUMMY_LISTENER;
        }
        // FIXME: 5/18/17 use appropriate stats object
        RestoreHandlerImpl handler = null;
        try {
            handler = new RestoreHandlerImpl(logger, stats().getSize(), listener);
            logger.retrieveAllEntries(handler);
        } catch (BackupException e) {
            if (handler != null) {
                handler.onRestoreError(e);
            } else {
                listener.done(e);
            }
        }
    }

    /**
     * retrieves backup stats
     *
     * @return the stats
     * @throws BackupException
     */
    public BackupStats stats() throws BackupException {
        return logger.stats();
    }

    /**
     * an interface for those interested in the progress of
     * restore process.
     */
    public interface ProgressListener {
        /**
         * called when restore starts
         *
         * @param expected amount of bytes expected to be restored
         */
        void onStart(long expected);

        /**
         * @param expected amount of bytes expected to be restored
         * @param restored the amount of bytes currently retrieved
         */
        void onProgress(long expected, long restored);

        /**
         * restore complete
         */
        void done(BackupException e);

    }

    private static final ProgressListener DUMMY_LISTENER = new ProgressListener() {
        @Override
        public void onStart(long expected) {

        }

        @Override
        public void onProgress(long expected, long restored) {

        }

        @Override
        public void done(BackupException e) {

        }
    };
}
