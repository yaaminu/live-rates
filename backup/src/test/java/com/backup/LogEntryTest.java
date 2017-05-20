package com.backup;

import org.junit.Assert;
import org.junit.Test;

import java.security.MessageDigest;

import static java.lang.System.currentTimeMillis;

/**
 * Created by yaaminu on 5/20/17.
 */
public class LogEntryTest {
    @Test
    public void getHashSum() throws Exception {
        DummyOp dummyOp = new DummyOp();
        long dateLogged = currentTimeMillis();
        String collection = "collection";
        LogEntry<?> logEntry =
                new LogEntry<>(collection, dummyOp, dateLogged);
        MessageDigest digest = MessageDigest.getInstance("sha1");
        digest.update(collection.getBytes());
        byte[] opData = dummyOp.data().toString().getBytes();
        digest.update(String.valueOf(collection.getBytes().length +
                opData.length + 8/*size*/ + 8/*dateLogged*/ + 40/*sha1length*/).getBytes());
        digest.update(opData);
        digest.update(String.valueOf(dateLogged).getBytes());
        String hashSum = Utils.bytesToString(digest.digest());

        Assert.assertEquals("must produce correct sha1 checksum", hashSum, logEntry.getHashSum());
    }

    @Test
    public void getSize() throws Exception {
        DummyOp dummyOp = new DummyOp();
        long dateLogged = currentTimeMillis();
        String collection = "collection";
        LogEntry<?> logEntry =
                new LogEntry<>(collection, dummyOp, dateLogged);
        Assert.assertEquals(dummyOp.data().toString()
                .getBytes().length + 8 + 8 + 40 + collection.getBytes().length, logEntry.getSize());
    }

}