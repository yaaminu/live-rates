package com.backup;

/**
 * Created by yaaminu on 5/17/17.
 */

public class BackupStats {
    private final long size;
    private final long lastModified;

    // TODO: 5/17/17 add more fields

    /**
     * @param size         the expected size of the backup file in bytes
     * @param lastModified the last time the backup file was updated.
     */
    BackupStats(long size, long lastModified) {
        this.size = size;
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackupStats stats = (BackupStats) o;

        return size == stats.size && lastModified == stats.lastModified;

    }

    @Override
    public int hashCode() {
        int result = (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        return result;
    }
}
