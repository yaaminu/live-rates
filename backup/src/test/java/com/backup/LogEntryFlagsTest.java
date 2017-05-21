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
       LogEntryFlags flags = new LogEntryFlags();
        flags.setEncrypted();
        assertEquals(flags.getFlags() &LogEntryFlags.ENCRYPTED,LogEntryFlags.ENCRYPTED);
    }

    @Test
    public void setCompressed() throws Exception {
       LogEntryFlags flags = new LogEntryFlags();
        flags.setCompressed();
        assertEquals(flags.getFlags() &LogEntryFlags.COMPRESSED,LogEntryFlags.COMPRESSED);
    }

    @Test
    public void isEncrypted() throws Exception {
       LogEntryFlags flags = new LogEntryFlags();
        assertFalse(flags.isEncrypted());
        flags.setEncrypted();
        assertEquals(flags.getFlags() &LogEntryFlags.ENCRYPTED,LogEntryFlags.ENCRYPTED);
        assertTrue(flags.isEncrypted());
    }

    @Test
    public void isCompressed() throws Exception {
       LogEntryFlags flags = new LogEntryFlags();
        assertFalse(flags.isCompressed());
        flags.setCompressed();
        assertEquals(flags.getFlags() &LogEntryFlags.COMPRESSED,LogEntryFlags.COMPRESSED);
        assertTrue(flags.isCompressed());
    }

    @Test
    public void clearCompressed() throws Exception {
        LogEntryFlags flags = new LogEntryFlags();
        flags.setCompressed();
        assertEquals(flags.getFlags() &LogEntryFlags.COMPRESSED,LogEntryFlags.COMPRESSED);
        flags.clearCompressed();
        assertNotEquals(flags.getFlags() &LogEntryFlags.COMPRESSED,LogEntryFlags.COMPRESSED);
    }

    @Test
    public void clearEncrypted() throws Exception {
       LogEntryFlags flags = new LogEntryFlags();
        flags.setEncrypted();
        assertEquals(flags.getFlags() &LogEntryFlags.ENCRYPTED,LogEntryFlags.ENCRYPTED);
        flags.clearEncrypted();
        assertNotEquals(flags.getFlags() &LogEntryFlags.ENCRYPTED,LogEntryFlags.ENCRYPTED);
    }
}