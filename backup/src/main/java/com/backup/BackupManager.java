package com.backup;


import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A backup manager is a simple utility for backing up files in an incremental
 * fashion instead of the rather inefficient whole database file backup
 * which is really wasteful and fragile.
 *
 * @see Logger
 * @see Operation
 * @see LogEntry
 */
public class BackupManager {

    private static Map<Logger, BackupManager> INSTANCES;

    @NonNull
    private final Logger logger;

    private static class RestoreHandlerImpl implements Logger.RestoreHandler {
        private final ProgressListener listener;
        private final long size;
        private final Logger logger;
        private long processed;

        public RestoreHandlerImpl(Logger logger, long size, @NonNull ProgressListener listener) {
            if (listener == null) throw new IllegalArgumentException();
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
        if (logger == null) throw new IllegalArgumentException("logger==null");
        this.logger = logger;
    }


    /**
     * Retrieves the singleton {@link BackupManager} instance for the logger of type T
     * or creates a new one if it does not exist. This means that  any {@link Logger}
     * implementation can only be used with only one instance of Backup manager. Essentially
     * this makes BackupManager a partial singleton
     * <p>
     * <p>
     * Backup manager attempts to check poorly implemented  hashcode()/equals() and
     * throws if it detects one. This is only a fail-fast mechanism so clients must never rely on
     * this check since a "clever" buggy code can outwit this check. It's your responsibility
     * to implement them well to ensure good behaviour of backup manager
     *
     * @return the {@link BackupManager}. never null
     * @throws RuntimeException when a it detects poor equals() and hashcode()
     *                          implementations in a {@link Logger}.
     */
    @NonNull
    public static synchronized <T extends Logger> BackupManager getInstance(@NonNull T logger) {
        if (logger == null) throw new IllegalArgumentException("logger==null");

        if (INSTANCES == null) {
            INSTANCES = new HashMap<>(2);
        }

        BackupManager instance = INSTANCES.get(logger);
        if (instance == null) {
            instance = new BackupManager(logger);
            INSTANCES.put(logger, instance);
        } else {
            checkPoorHashcodeEqualsImplementation(instance, logger);
        }
        return instance;
    }

    private static <T extends Logger> void checkPoorHashcodeEqualsImplementation(BackupManager instance, T logger) {
        if (instance.logger.getClass() != logger.getClass()) {
            throw new RuntimeException("improper hashcode/equals implementation: " + logger.getClass().getName()
                    + " and " + instance.logger.getClass().getName() + " hash colliding hashcode/equals");
        }
    }

    /**
     * logs an operation to the backup log.
     *
     * @param group     the group(collection) this operation is associated with
     * @param operation the operation that happened
     * @throws BackupException
     */
    public synchronized void log(@NonNull String group, @NonNull Operation operation, long timestamp) throws BackupException {
        if (operation == null) throw new IllegalArgumentException("operation is null");
        if (group == null || group.length() == 0) {
            throw new IllegalArgumentException("group canot be an empty string");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp < 0");
        }
        logger.appendEntry(new LogEntry<>(group, operation, timestamp));
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
        RestoreHandlerImpl handler = new RestoreHandlerImpl(logger, 10000, listener);
        try {
            logger.retrieveAllEntries(handler);
        } catch (BackupException e) {
            handler.onRestoreError(e);
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

    static final ProgressListener DUMMY_LISTENER = new ProgressListener() {
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
