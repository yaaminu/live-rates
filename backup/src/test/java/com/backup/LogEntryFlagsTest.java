package com.backup;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by yaaminu on 5/19/17.
 */
public class LogEntryFlagsTest {
    @Test
    public void setEncrypted() throws Exception {
        FileSystemLogger.LogEntryFlags flags = new FileSystemLogger.LogEntryFlags();
        flags.setEncrypted();
        assertEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.ENCRYPTED, FileSystemLogger.LogEntryFlags.ENCRYPTED);
    }

    @Test
    public void setCompressed() throws Exception {
        FileSystemLogger.LogEntryFlags flags = new FileSystemLogger.LogEntryFlags();
        flags.setCompressed();
        assertEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.COMPRESSED, FileSystemLogger.LogEntryFlags.COMPRESSED);
    }

    @Test
    public void isEncrypted() throws Exception {
        FileSystemLogger.LogEntryFlags flags = new FileSystemLogger.LogEntryFlags();
        assertFalse(flags.isEncrypted());
        flags.setEncrypted();
        assertEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.ENCRYPTED, FileSystemLogger.LogEntryFlags.ENCRYPTED);
        assertTrue(flags.isEncrypted());
    }

    @Test
    public void isCompressed() throws Exception {
        FileSystemLogger.LogEntryFlags flags = new FileSystemLogger.LogEntryFlags();
        assertFalse(flags.isCompressed());
        flags.setCompressed();
        assertEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.COMPRESSED, FileSystemLogger.LogEntryFlags.COMPRESSED);
        assertTrue(flags.isCompressed());
    }

    @Test
    public void clearCompressed() throws Exception {
        FileSystemLogger.LogEntryFlags flags = new FileSystemLogger.LogEntryFlags();
        flags.setCompressed();
        assertEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.COMPRESSED, FileSystemLogger.LogEntryFlags.COMPRESSED);
        flags.clearCompressed();
        assertNotEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.COMPRESSED, FileSystemLogger.LogEntryFlags.COMPRESSED);
    }

    @Test
    public void clearEncrypted() throws Exception {
        FileSystemLogger.LogEntryFlags flags = new FileSystemLogger.LogEntryFlags();
        flags.setEncrypted();
        assertEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.ENCRYPTED, FileSystemLogger.LogEntryFlags.ENCRYPTED);
        flags.clearEncrypted();
        assertNotEquals(flags.getFlags() & FileSystemLogger.LogEntryFlags.ENCRYPTED, FileSystemLogger.LogEntryFlags.ENCRYPTED);
    }
}