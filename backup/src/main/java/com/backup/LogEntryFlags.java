package com.backup;

/**
 * Created by yaaminu on 5/20/17.
 */
class LogEntryFlags {
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
