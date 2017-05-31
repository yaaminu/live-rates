package com.backup;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

import static android.content.Context.MODE_APPEND;

/**
 * Created by yaaminu on 5/20/17.
 */

public class GoogleDriveStorage implements Storage {

    public static final String APP_BACKUP_SUFFIX = ".app-backup";
    private static final String DRIVE_BACKUP_BACKUPLOG_DIRNAME = "drive_backup.waitig";
    private static final InputStream EMPTY_INPUT_STREAM = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };
    private final Semaphore lock;
    private final LocalFileSystemStorage storage;

    @Nullable
    private GoogleApiClient apiClient;
    private OutputStream outputStream;

    public GoogleDriveStorage(Context context) {
        lock = new Semaphore(1, true);
        this.storage = new LocalFileSystemStorage(
                context.getDir(DRIVE_BACKUP_BACKUPLOG_DIRNAME, MODE_APPEND));
    }

    public void initiaize(final Context context) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                lock.acquireUninterruptibly();
                try {
                    if (apiClient == null) {
                        apiClient = new GoogleApiClient.Builder(context)
                                .addApi(Drive.API)
                                .addScope(Drive.SCOPE_APPFOLDER)
                                .build();
                        ConnectionResult result = apiClient.blockingConnect();
                        if (result.getErrorCode() != ConnectionResult.SUCCESS) {
                            // TODO: 5/22/17 post this to the notification panel
                            throw new RuntimeException();
                        }
                    }
                } finally {
                    lock.release();
                }
            }
        };
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    @NonNull
    @Override
    public OutputStream newAppendableOutPutStream(String collectionName) throws IOException {
        lock.acquireUninterruptibly();
        try {
            if (outputStream == null) {
                outputStream = storage.newAppendableOutPutStream(collectionName);
            }

            //wrap the outputStream in another output stream so we
            //can play well with the lock.
            return new LockAwareOutPutStream(outputStream, lock);
        } catch (Throwable e) {
            lock.release();
            throw e;
        }
    }

    @NonNull
    @Override
    public InputStream newInputStream(String collectionName) throws IOException {
        MetadataBuffer metadataBuffer = null;
        try {
            lock.acquireUninterruptibly();
            if (apiClient == null) {
                throw new IllegalStateException("did you forget to init()");
            }
            DriveFolder folder = Drive.DriveApi.getAppFolder(apiClient);
            PendingResult<DriveApi.MetadataBufferResult> resultPendingResult =
                    folder.queryChildren(apiClient, new Query.Builder()
                            .addFilter(Filters.eq(SearchableField.TITLE, collectionName + APP_BACKUP_SUFFIX))
                            .build());

            DriveApi.MetadataBufferResult results = resultPendingResult.await();
            if (!results.getStatus().isSuccess()) {
                throw new IOException(results.getStatus().getStatusMessage());
            }

            metadataBuffer =
                    results.getMetadataBuffer();

            if (metadataBuffer.getCount() == 0) {
                //first time.../ return nothing
                return EMPTY_INPUT_STREAM;
            }
            DriveFile file = metadataBuffer.get(0).getDriveId().asDriveFile();
            DriveApi.DriveContentsResult driveContentResults = file.open(apiClient, DriveFile.MODE_READ_ONLY, null)
                    .await();
            if (!driveContentResults.getStatus().isSuccess()) {
                throw new IOException(driveContentResults.getStatus().getStatusMessage());
            }
            //wrap the InputStream with another input stream so we can play
            //well with the lock
            return new LockAwareInputStream(driveContentResults
                    .getDriveContents().getInputStream(), lock);
        } catch (Throwable e) { //catch all exceptions and release lock, rethrow after that
            lock.release();
            if (metadataBuffer != null) {
                metadataBuffer.release();
            }
            throw e;
        }
    }

    @Override
    public long size(String collectionName) throws IOException {
        MetadataBuffer metadataBuffer = null;
        try {
            lock.acquireUninterruptibly();
            DriveApi.MetadataBufferResult result = Drive.DriveApi.getAppFolder(apiClient)
                    .queryChildren(apiClient,
                            new Query.Builder()
                                    .addFilter(Filters.eq(SearchableField.TITLE,
                                            collectionName + APP_BACKUP_SUFFIX)).build()).await();
            throwIfNotSuccess(result.getStatus());

            metadataBuffer = result.getMetadataBuffer();
            if (metadataBuffer.getCount() == 0) {
                return 0;
            }
            return metadataBuffer.get(0).getFileSize();
        } finally {
            if (metadataBuffer != null) {
                metadataBuffer.release();
            }
            lock.release();
        }
    }

    @Override
    public long lastModified(String collectionName) throws IOException {
        try {
            lock.acquireUninterruptibly();
            DriveApi.MetadataBufferResult result = Drive.DriveApi.getAppFolder(apiClient)
                    .queryChildren(apiClient,
                            new Query.Builder()
                                    .addFilter(Filters.eq(SearchableField.TITLE,
                                            collectionName + APP_BACKUP_SUFFIX)).build()).await();
            MetadataBuffer metadataBuffer = result.getMetadataBuffer();
            try {
                throwIfNotSuccess(result.getStatus());
                if (metadataBuffer.getCount() == 0) {
                    return 0;
                }
                return metadataBuffer.get(0).getModifiedDate().getTime();
            } finally {
                metadataBuffer.release();
            }
        } finally {
            lock.release();
        }
    }

    public void sync(final String collectionName) throws BackupException {
        try {
            lock.acquireUninterruptibly();


            //TODO also check for the log of other devices, pull them and apply the changes locally.

            //we are dead sure that no one is modifying the temporary backup file now
            //since we hold the lock.
            File copy = storage.getBackupFile(collectionName);
            if (!copy.exists() || copy.length() == 0) { //we don't have a pending backup
                System.out.println("back up complete");
                return;
            }
            Filter filter = Filters.eq(SearchableField.TITLE, collectionName + APP_BACKUP_SUFFIX);
            DriveApi.MetadataBufferResult result = Drive.DriveApi.getAppFolder(apiClient)
                    .queryChildren(apiClient, new Query.Builder()
                            .addFilter(filter).build())
                    .await();
            throwIfNotSuccess(result.getStatus());
            MetadataBuffer metadataBuffer = result.getMetadataBuffer();
            try {
                if (metadataBuffer.getCount() == 0) {
                    //create the file
                    DriveApi.DriveContentsResult driveContentsResult = Drive.DriveApi.newDriveContents(apiClient).await();
                    throwIfNotSuccess(driveContentsResult.getStatus());

                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    OutputStream localOutStream = driveContents.getOutputStream();
                    InputStream in = new FileInputStream(copy);

                    copy(in, localOutStream);

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(collectionName + APP_BACKUP_SUFFIX).build();
                    DriveFolder.DriveFileResult driveFileResult = Drive.DriveApi.getAppFolder(apiClient)
                            .createFile(apiClient, changeSet
                                    , driveContents).await();
                    throwIfNotSuccess(driveFileResult.getStatus());

                } else { //file exist
                    DriveFile backupFile = metadataBuffer.get(0).getDriveId().asDriveFile();
                    DriveApi.DriveContentsResult contentsResult = backupFile.open(apiClient, DriveFile.MODE_WRITE_ONLY, null)
                            .await();
                    throwIfNotSuccess(contentsResult.getStatus());
                    OutputStream localOutStream = contentsResult.getDriveContents().getOutputStream();
                    copy(new FileInputStream(copy), localOutStream);
                }

                if (!copy.delete()) {
                    System.err.println("failed to delete backed up copy");
                }
            } finally {
                metadataBuffer.release();
            }
        } catch (IOException e) {
            throw new BackupException(BackupException.EIOERROR, e.getMessage(), e);
        } finally {
            lock.release();
        }
    }

    private void throwIfNotSuccess(Status status) throws IOException {
        if (!status.isSuccess()) {
            throw new IOException(status.getStatusMessage());
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buff = new byte[4096];
            int read;
            while ((read = in.read(buff)) != -1) {
                out.write(buff, 0, read);
            }
            out.flush();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}

