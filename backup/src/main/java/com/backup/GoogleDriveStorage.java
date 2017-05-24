package com.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import com.android.annotations.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yaaminu on 5/20/17.
 */

public class GoogleDriveStorage implements Storage {

    private static final String DRIVE_BACKUP_BACKUPLOG_DIRNAME = "drive_backup.waitig";
    private static final String PREF_DRIVE_BACKUP = "drive_backup";
    private static final String KEY_LOCAL_BACKUP_CURSOR_PREFIX = "backup.pending.cursor.";
    private static final Filter BACKUP_FOLDER_FILTER = Filters.eq(SearchableField.TITLE, "app-backup");
    private static final String BACKUP_CURSOR_PREFIX = "backup.cursor.";
    private static final InputStream EMPTY_INPUT_STREAM = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };
    private final Semaphore lock;
    private final SharedPreferences preferences;
    private final LocalFileSystemStorage storage;

    @Nullable
    private GoogleApiClient apiClient;
    private OutputStream outputStream;

    public GoogleDriveStorage(Context context) {
        lock = new Semaphore(1, true);
        preferences = context.getSharedPreferences(PREF_DRIVE_BACKUP, MODE_PRIVATE);
        this.storage = new LocalFileSystemStorage(
                context.getDir(DRIVE_BACKUP_BACKUPLOG_DIRNAME, MODE_APPEND));
    }

    private void initiaize(Context context) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new IllegalStateException("can't make this call on the main thread");
        }

        apiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
        ConnectionResult result = apiClient.blockingConnect();
        if (result.getErrorCode() != ConnectionResult.SUCCESS) {
            // TODO: 5/22/17 post this to the notification pane
            throw new RuntimeException();
        }
    }

    @Override
    public OutputStream newAppendableOutPutStream(String collectionName) throws IOException {
        lock.acquireUninterruptibly();
        if (outputStream == null) {
            outputStream = storage.newAppendableOutPutStream(collectionName);
        }
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void close() throws IOException {
                if (outputStream != null) {
                    outputStream.close();
                    outputStream = null;
                }
                lock.release(); //release the lock  here
            }
        };
    }

    @Override
    public InputStream newInputStream(String collectionName) throws IOException {
        try {
            lock.acquireUninterruptibly();
            if (apiClient == null) {
                throw new IllegalStateException("did you forget to init()");
            }
            DriveFolder folder = Drive.DriveApi.getAppFolder(apiClient);
            PendingResult<DriveApi.MetadataBufferResult> resultPendingResult =
                    folder.queryChildren(apiClient, new Query.Builder()
                            .addFilter(BACKUP_FOLDER_FILTER)
                            .build());
            MetadataBuffer result =
                    resultPendingResult.await().getMetadataBuffer();
            if (result.getCount() == 0) {
                //first time.../ return nothing
                return EMPTY_INPUT_STREAM;
            }
            List<DriveFile> files = new ArrayList<>(result.getCount());

            for (Metadata metadata : result) {
                DriveFile file = metadata.getDriveId().asDriveFile();
                long cursor = preferences.getLong(BACKUP_CURSOR_PREFIX + file.getDriveId(), 0);

                if (cursor < metadata.getFileSize()) { //we have something to backup
                    files.add(file);
                }
            }
            if (files.isEmpty()) {
                return EMPTY_INPUT_STREAM;
            }
            return new DriveFilesInputStream(apiClient, files);
        } finally {
            lock.release();
        }
    }

    @Override
    public long size(String collectionName) throws IOException {
        try {
            lock.acquireUninterruptibly();
            return 0;
        } finally {
            lock.release();
        }
    }

    @Override
    public long lastModified(String collectionName) {
        try {
            lock.acquireUninterruptibly();
            return 0;
        } finally {
            lock.release();
        }
    }

    public void sync(final String collectionName) throws BackupException {
        try {
            lock.acquireUninterruptibly();
            long cursor = preferences.getLong(KEY_LOCAL_BACKUP_CURSOR_PREFIX + collectionName, -1);
            if (cursor == -1) {
                //got nothing to backup
                return;
            }
            //backup the temporary log, delete it and  update the cursor to -1.
            //also check for the log of other devices, pull them and apply the changes locally.

            //we are dead sure that no one is modifying the temporary backup file now
            //se we have the lock.
            File file = storage.getBackupFile(collectionName);
            final File copy = new File(file.getParentFile(), file.getName() + "-copy~");
            FileUtils.copyFile(file, copy);
            // TODO: 5/22/17 push the content of copy to google drive
            // TODO: 5/22/17 update the cursor
            throw new UnsupportedOperationException();
        } catch (IOException e) {
            throw new BackupException(BackupException.EIOERROR, e.getMessage(), e);
        } finally {
            lock.release();
        }
    }
}

