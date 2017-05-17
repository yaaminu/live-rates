package com.backup;

/**
 * Created by yaaminu on 5/17/17.
 */

public class BackupException extends Exception {

    private final int errCode;

    public BackupException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

}
