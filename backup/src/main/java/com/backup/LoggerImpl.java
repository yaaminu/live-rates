package com.backup;

import com.android.annotations.NonNull;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

import static com.backup.BackupException.EAGAIN;
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

public class LoggerImpl implements Logger {

    private final DependencyInjector injector;
    private final Serializer<LogEntry<? extends Operation>> serializer;
    private final Semaphore lock;
    private final Storage storage;
    private final String collectionName;

    LoggerImpl(@NonNull String collectionName, @NonNull DependencyInjector injector,
               @NonNull Serializer<LogEntry<? extends Operation>> serializer, @NonNull Storage storage) {
        checkArgs(collectionName, injector, serializer, storage);
        this.collectionName = collectionName;
        this.storage = storage;
        this.injector = injector;
        this.serializer = serializer;
        lock = new Semaphore(1, true);
    }

    private void checkArgs(@NonNull String collectionName, @NonNull DependencyInjector injector,
                           @NonNull Serializer<LogEntry<? extends Operation>> serializer,
                           @NonNull Storage storage) {
        if (collectionName == null) {
            throw new IllegalArgumentException("collection name is null");
        }
        if (collectionName.trim().length() == 0) {
            throw new IllegalArgumentException("collection name is empty");
        }
        if (injector == null) {
            throw new IllegalArgumentException("injector == null");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("serializer == null");
        }
        if (storage == null) {
            throw new IllegalArgumentException("storage == null");
        }
    }


    @Override
    public void appendEntry(@NonNull LogEntry<? extends Operation> logEntry) throws BackupException {
        OutputStream stream = null;
        try {
            lock.acquire();
            byte[] blob = Parser.encode(serializer, logEntry);
            // TODO: 5/18/17 encrypt and compress before writing
            stream = storage.newAppendableOutPutStream(collectionName);
            IOUtils.write(blob, stream);
        } catch (FileNotFoundException e) {
            throw new BackupException(ENOENT, e.getMessage(), e);
        } catch (InterruptedIOException | InterruptedException e) {
            throw new BackupException(EAGAIN, e.getMessage(), e);
        } catch (IOException e) {
            throw new BackupException(EIOERROR, e.getMessage(), e);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
            lock.release();
        }
    }

    @Override
    public void retrieveAllEntries(@NonNull RestoreHandler handler) {
        if (handler == null) throw new IllegalArgumentException("handler == null");
        try {
            lock.acquire();
            // TODO: 5/19/17 use rxJava to stream stuffs
            handler.onPrepareRestore();
            restoreBackup(handler);
            handler.onRestoreComplete();
        } catch (BackupException e) {
            handler.onRestoreError(e);
        } catch (InterruptedException e) {
            handler.onRestoreError(new BackupException(EAGAIN, e.getMessage(), e));
        } finally {
            lock.release();
        }
    }

    private void restoreBackup(RestoreHandler handler) throws BackupException {
        Parser parser = null;

        try {
            parser = new Parser(storage.newInputStream(collectionName), serializer);
            LogEntry<? extends Operation> entry;
            while ((entry = parser.next()) != null) {
                handler.onEntry(entry);
            }
        } catch (FileNotFoundException e) {
            handler.onRestoreError(new BackupException(ENOENT, e.getMessage(), e));
        } catch (IOException e) {
            handler.onRestoreError(new BackupException(EIOERROR, e.getMessage(), e));
        } finally {
            IOUtils.closeQuietly(parser);
        }
    }

    @Override
    public final BackupStats stats() throws BackupException {
        try {
            lock.acquire();
            return new BackupStats(storage.size(collectionName), storage.lastModified(collectionName));
        } catch (InterruptedException e) {
            throw new BackupException(EAGAIN, e.getMessage(), e);
        } catch (IOException e) {
            throw new BackupException(EIOERROR, e.getMessage(), e);
        } finally {
            lock.release();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoggerImpl logger = (LoggerImpl) o;

        return collectionName.equals(logger.collectionName);

    }

    @Override
    public int hashCode() {
        return collectionName.hashCode();
    }

    @Override
    @NonNull
    public final DependencyInjector getInjector() {
        return injector;
    }

}
