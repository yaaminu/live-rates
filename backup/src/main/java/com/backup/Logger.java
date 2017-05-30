package com.backup;


import android.support.annotation.NonNull;

/**
 * Loggers take care of sending/retrieving log entries
 * to some sort of dataStore. A dataStore could be in-memory,
 * regular file or even a remote database for storage.
 * <p>
 * To correctly implement this interface, an implementation:
 * <ol>
 * <li>Must be threadsafe</li>
 * <li>Must be able to retrieve entries in reverse order</li>
 * <li>Must be able to provide the size of the backup log</li>
 * <li>Must never serialise(backup) a field that is marked with {@link com.backup.annotations.LoggerIgnore} </li>
 * <li>Must always call {@link DependencyInjector#inject(Operation)}
 * on  {@link LogEntry#op} field</li>
 * <li>Must be able to provide a unix timestamp of the last time the log was modified</li>
 * <li>Must ensure the integrity of the backup log is intact</li>
 * <li>
 * Must implement {@link Object#equals(Object)} and {@link Object#hashCode()}  such that
 * instances can be correctly stored in a {@link java.util.HashMap}. This is a strong requirement
 * for {@link BackupManager} to behave as it caches loggers in a HashMap. A collision would mean the wrong implementation
 * of a logger could  be returned in it's {@link BackupManager#getInstance(Logger)}
 * which can lead to extremely dire consequences.
 * <p>
 * typical example is an implementation that knows how to backup to a particular
 * regular file at (/tmp/backups/photos.log) on the local file system
 * which does not implement hashcode() such that it always return the same for the same type of
 * logger for the same file, it could collide with a different {@link Logger} which will make
 * {@link BackupManager#getInstance(Logger)} to wrongly retrieve a different {@link Logger} that maybe, logs to
 * a different file(/tmp/backups/user_settings.log on the same local file system or worse a completely different logger that logs to a  remote database.
 * It's obvious the the damage this could cause.
 * </li>
 * </ol>
 * <p>
 * <p>
 * <p>
 * Created by yaaminu on 5/17/17.
 *
 * @see BackupManager
 * @see Operation
 * @see LogEntry
 * @see com.backup.annotations.LoggerIgnore
 */
public interface Logger {
    /**
     * appends the given log entry to the backup log.
     *
     * @param logEntry the entry to append. must not be null
     * @throws BackupException when any implementation defined error occurs
     */
    void appendEntry(@NonNull LogEntry<? extends Operation> logEntry) throws BackupException;

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
     * The injector for {@link Operation} instances.
     * <p>
     * Typically, operations would require some dependencies
     * that it cannot persist.
     *
     * @return the injector
     */

    @NonNull
    DependencyInjector getInjector();

    /**
     * The {@link Logger} retrieves the logs one by one and pass it on to
     * {@link RestoreHandler} implementations to be replayed.
     * <p>
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
         * process a log entry as they come in.
         *
         * @param logEntry the current entry to process, never null
         * @throws BackupException if there's an error in the restore process
         */
        void onEntry(@NonNull LogEntry logEntry) throws BackupException;

        /**
         * a hook to notify watchers we are done retrieving all entries
         */
        void onRestoreComplete();

        /**
         * a hook for handling restore errors. When an error occurs,
         * on complete should be called
         *
         * @param e the error that occurred
         */
        void onRestoreError(BackupException e);
    }
}
