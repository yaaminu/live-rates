package com.backup;


import android.support.annotation.Nullable;

/**
 * Created by yaaminu on 5/17/17.
 */

public class BackupException extends Exception {

    public static final int ENOENT = 0x1, EEXIST = 0x2, EAGAIN = 0x3, EIOERROR = 0x4,
            EMALFORMEDINPUT = 0x5;
    private final int errCode;

    public BackupException(int errCode, @Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    public int getErrorCode() {
        return errCode;
    }
}
