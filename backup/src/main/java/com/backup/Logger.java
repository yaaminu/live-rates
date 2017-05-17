package com.backup;

import com.android.annotations.NonNull;

/**
 * Loggers take care of sending/retrieving log entries
 * to some sort of dataStore. A dataStore could be in-memory,
 * regular file or even a remote database for storage.
 * <p>
 * <p>
 * The equals/hashcode implementation for every single logger must be implemented such that
 * instance can be stored in a hash map. This is a strong requirement for {@link BackupManager}
 * to behave as it caches loggers in a HashMap. A collision would mean the wrong
 * implementation of a logger will likely be returned in it's {@link BackupManager#getInstance(Logger)}
 * which can lead an extremely unacceptable error.
 * <p>
 * Created by yaaminu on 5/17/17.
 *
 * @see BackupManager
 * @see Operation
 * @see LogEntry
 */
public interface Logger {
    /**
     * appends the given log entry to the backup log.
     *
     * @param logEntry the entry to append. must not be null
     * @throws BackupException when any implementation defined error occurs
     */
    void appendEntry(@NonNull LogEntry logEntry) throws BackupException;

    /**
     * Restore the backup log. Logger implementations are not required but
     * are strongly recommended to stream, the entries as they restore them
     * say from disk or over the network for memory usage reasons.
     * <p>
     * it's important to note that, the handler may be called on the same thread
     * as the retrieval so care must be taken to not block for too long.
     *
     * @param handler the handler for handling individual log entries as they are retrieved
     * @throws BackupException when any implementation defined error occurs
     */
    void retrieveAllEntries(@NonNull RestoreHandler handler) throws BackupException;


    /**
     * returns the stats about the backup.
     *
     * @return the stats
     * @throws BackupException when an error occurs
     * @see {@link BackupStats}
     */
    @NonNull
    BackupStats stats() throws BackupException;

    /**
     * The injector for Operation instances.
     * <p>
     * Typically, operations would require some dependencies
     * that it doesn't serialize.
     *
     * @return the injector
     */

    @NonNull
    DependencyInjector getInjector();

    /**
     * all hooks may be called on the processing thread so care must be
     * taken to not block for long.
     */
    interface RestoreHandler {
        /**
         * a hook to notify watchers that we are about to start
         * the restore process
         */
        void onPrepareRestore();

        /**
         * process a log entry as the come in.
         *
         * @param logEntry the current entry to process, never null
         * @throws BackupException if there's an error in the process
         */
        void onEntry(@NonNull LogEntry logEntry) throws BackupException;

        /**
         * a hook to notify watchers we are done retrieving all entries
         */
        void onRestoreComplete();
    }
}
