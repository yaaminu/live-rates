package com.backup;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;

import static com.backup.BackupException.EAGAIN;
import static com.backup.BackupException.EEXIST;
import static com.backup.BackupException.EIOERROR;
import static com.backup.BackupException.ENOENT;

/**
 * An implementation of the {@link Logger} interface that persist it's backup
 * on the local filesystem. It does so incrementally with strong integrity checks.
 * All log entries are sha1 checksummed before storage
 * <p>
 * This logger uses {@link Serializer<LogEntry>} to convert POJOs to byte arrays and back
 * and then prepend a 10-byte header to the resulting byte array. The header keeps track of
 * the flags and size of a given blog of byte stream.
 * <p>
 * below is the format:
 * <p>
 * It has a header of 10 bytes. The first two bytes
 * are used for storing metadata like (encrypted,compressed, etc). the last 8
 * bytes is used to store the size of the payload.
 * <p>
 * The payload is a variable length byte array whose size is stored in the
 * second part of the header. Currently it's json representation of the
 * {@link LogEntry<Operation>} to be encoded.
 * <p>
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + header(10 bytes)                    | payload(variable length)           +
 * + ____________________________________|____________________________________+
 * + flags(2 bytes)| payloadSize(8 bytes)|{@link Serializer#serialize(Object)}+
 * +               |                     |                                    +
 * + ______________|_____________________|____________________________________+
 * + all reserved  | long integer        |                                    +
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * <p>
 * Created by yaaminu on 5/17/17.
 */

public class FileSystemLogger implements Logger {

    public static final String BACKUP_LOG_SUFFIX = "-backup.log";
    @NonNull
    private final File directory;
    private final DependencyInjector injector;
    private final Serializer<LogEntry<? extends Operation>> serializer;
    private final Semaphore lock;

    private FileSystemLogger(@NonNull File directory,
                             @NonNull DependencyInjector injector,
                             @NonNull Serializer<LogEntry<? extends Operation>> serializer) {
        this.directory = directory;
        this.injector = injector;
        this.serializer = serializer;
        lock = new Semaphore(1, true);
    }

    @NonNull
    public static FileSystemLogger create(@NonNull File directory,
                                          @NonNull DependencyInjector injector,
                                          @NonNull Serializer<LogEntry<? extends Operation>> serializer) {
        if (directory == null) {
            throw new IllegalArgumentException("directory == null");
        }
        if (injector == null) {
            throw new IllegalArgumentException("injector == null");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("serializer == null");
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IllegalArgumentException("failed to create directory");
            }
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("backup  dir already exists and it's not a directory");
        }
        return new FileSystemLogger(directory, injector, serializer);
    }

    @Override
    public void appendEntry(@NonNull LogEntry<? extends Operation> logEntry) throws BackupException {
        try {
            lock.acquire();
            ensureDirExists();
            byte[] blob = encodeLogEntry(logEntry, new LogEntryFlags());
            // TODO: 5/18/17 encrypt before writing to file
            String collectionName = logEntry.getCollectionName();
            File file = new File(directory, makeFileName(collectionName));
            FileUtils.writeByteArrayToFile(file, blob, true);
        } catch (FileNotFoundException e) {
            throw new BackupException(ENOENT, e.getMessage(), e);
        } catch (InterruptedIOException | InterruptedException e) {
            throw new BackupException(EAGAIN, e.getMessage(), e);
        } catch (IOException e) {
            throw new BackupException(EIOERROR, e.getMessage(), e);
        } finally {
            lock.release();
        }
    }

    private byte[] encodeLogEntry(@NonNull LogEntry<? extends Operation> logEntry, @Nullable LogEntryFlags flags) throws BackupException {
        byte[] payload = serializer.serialize(logEntry);
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // + header(10 bytes)                    | payload(variable length)+
        // + ____________________________________|_________________________+
        // + flags(2 bytes)| payloadSize(8 bytes)|                         +
        // + ____________________________________|_________________________+
        // + all reserved  | long integer        |                         +
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        ByteBuffer blob = ByteBuffer.allocate(payload.length + 10); //the 10 is the header.
        blob.order(ByteOrder.BIG_ENDIAN);
        blob.put((byte) 0).put((byte) 0)
                .putLong(payload.length)
                .put(payload);
        return blob.array();
    }

    private String makeFileName(String collectionName) {
        return "."/*hidden*/ + collectionName + BACKUP_LOG_SUFFIX;
    }

    private boolean isBackupFile(String filename) {
        return filename.endsWith(BACKUP_LOG_SUFFIX);
    }

    private void ensureDirExists() throws BackupException {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new BackupException(ENOENT, "failed to create backup dir", null);
            }
        }
        if (!directory.isDirectory()) {
            throw new BackupException(EEXIST, "backup directory exists but it's a file", null);
        }
    }

    /**
     * for testing purposes
     *
     * @param collectionName the name of collection
     * @return the backup file for  this collection.
     */
    File getBackupFile(@NonNull String collectionName) {
        return new File(directory, makeFileName(collectionName));
    }

    @Override
    public void retrieveAllEntries(@NonNull RestoreHandler handler) {
        if (handler == null) throw new IllegalArgumentException("handler == null");
        try {
            lock.acquire();
            // TODO: 5/19/17 use rxJava to stream stuffs
            handler.onPrepareRestore();
            File[] files = findBackupFiles();
            for (File file : files) {
                restoreBackup(file, handler);
            }
            handler.onRestoreComplete();
        } catch (BackupException e) {
            handler.onRestoreError(e);
        } catch (InterruptedException e) {
            handler.onRestoreError(new BackupException(EAGAIN, e.getMessage(), e));
        } finally {
            lock.release();
        }
    }

    private void restoreBackup(File file, RestoreHandler handler) throws BackupException {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(0L);
            int read = 0;
            long entrySize = 0;
            byte[] buffer = new byte[1024];
            do {
                char flags = randomAccessFile.readChar();
                entrySize = randomAccessFile.readLong();
                if (buffer.length < entrySize) { //only reuse if we wont fit
                    buffer = new byte[(int) entrySize];
                }
                randomAccessFile.readFully(buffer, 0, (int) entrySize);

                LogEntryFlags logEntryFlags = new LogEntryFlags(flags);
                // TODO: 5/19/17 do something (like decompress) to the payload based on the flags
                LogEntry<? extends Operation> entry = serializer.deserialize(buffer, 0, (int) entrySize);
                handler.onEntry(entry);

                read += (entrySize + 10/*sizeOf(char) + sizeOf(long)*/);

            } while (read < file.length());
        } catch (FileNotFoundException e) {
            handler.onRestoreError(new BackupException(ENOENT, e.getMessage(), e));
        } catch (IOException e) {
            handler.onRestoreError(new BackupException(EIOERROR, e.getMessage(), e));
        } finally {
            IOUtils.closeQuietly(randomAccessFile);
        }
    }

    private File[] findBackupFiles() throws BackupException {
        File[] files = directory.listFiles(BACKUP_FILES_FILTER);
        if (files == null) {
            throw new BackupException(ENOENT, "No backup for exist", null);
        }

        return files;
    }

    private final FileFilter BACKUP_FILES_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return isBackupFile(file.getAbsolutePath());
        }
    };

    @Override
    public BackupStats stats() throws BackupException {
        try {
            lock.acquire();
            File[] files = findBackupFiles();
            long finalSize = 0,
                    latest = 0;
            for (File file : files) {
                finalSize += file.length();
                long tmpLastMod = file.lastModified();
                if (tmpLastMod > latest) {
                    latest = tmpLastMod;
                }
            }
            return new BackupStats(finalSize, latest);
        } catch (InterruptedException e) {
            throw new BackupException(EAGAIN, e.getMessage(), e);
        } finally {
            lock.release();
        }
    }

    @Override
    @NonNull
    public DependencyInjector getInjector() {
        return injector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileSystemLogger logger = (FileSystemLogger) o;

        return directory.getAbsolutePath().equals(logger.directory.getAbsolutePath());

    }

    @Override
    public int hashCode() {
        return directory.getAbsolutePath().hashCode();
    }

    static class LogEntryFlags {
        static final char ENCRYPTED = 0x1, COMPRESSED = 0x2;
        private char flags;

        public LogEntryFlags() {
            this((char) 0);
        }

        public LogEntryFlags(char flags) {
            this.flags = flags;
        }

        public char getFlags() {
            return flags;
        }

        int setEncrypted() {
            flags |= ENCRYPTED;
            return flags;
        }

        int clearEncrypted() {
            flags &= ~ENCRYPTED;
            return flags;
        }

        int setCompressed() {
            flags |= COMPRESSED;
            return flags;
        }

        int clearCompressed() {
            flags &= ~COMPRESSED;
            return flags;
        }

        boolean isEncrypted() {
            return (flags & ENCRYPTED) == ENCRYPTED;
        }

        boolean isCompressed() {
            return (flags & COMPRESSED) == COMPRESSED;
        }
    }
}
