package com.backup;


import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by yaaminu on 5/22/17.
 */

class DriveFilesInputStream extends InputStream {

    private final List<DriveFile> files;
    private int currentIndex;

    @Nullable
    private InputStream currentStream;
    private final GoogleApiClient apiClient;

    public DriveFilesInputStream(GoogleApiClient apiClient, List<DriveFile> files) {
        this.files = files;
        this.apiClient = apiClient;
        currentIndex = 0;
        currentStream = null;
    }

    @Override
    public int read() throws IOException {
        int read = -1;
        while (currentStream != null && (read = currentStream.read()) == -1) {
            currentStream = getNextInputStream();
        }
        return read;
    }

    public InputStream getNextInputStream() {
        if (currentIndex == files.size()) {
            currentStream = null;
            return null;
        }
        if (currentStream == null) {
            currentStream =
                    files.get(currentIndex)
                            .open(apiClient, DriveFile.MODE_READ_ONLY, null)
                            .await().getDriveContents().getInputStream();

            if (!(currentStream instanceof BufferedInputStream)) {
                currentStream = new BufferedInputStream(currentStream);
            }
            //advance the cursor.
            currentIndex += 1;
        }
        return currentStream;
    }
}
