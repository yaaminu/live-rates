package com.zealous.errors;

/**
 * Created by yaaminu on 4/15/17.
 */
public class ZealousException extends Exception {
    public ZealousException(String message) {
        super(message);
    }

    public ZealousException(Throwable cause) {
        super(cause);
    }
}
