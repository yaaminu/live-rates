package com.backup;

import org.junit.Assert;
import org.junit.Test;

import java.security.MessageDigest;

/**
 * Created by yaaminu on 5/20/17.
 */
public class UtilsTest {

    @Test
    public void testByteToString() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("sha1");
        byte[] hash = digest.digest();
        String test = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
        Assert.assertEquals("must correctly convert bytes string to hex", test, Utils.bytesToString(hash));

        Assert.assertEquals("0f057f", Utils.bytesToString(new byte[]{15, 5, 127}));
        Assert.assertEquals("0d0a7f", Utils.bytesToString(new byte[]{13, 10, 127}));
    }

    @Test
    public void testHash() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("sha1");
        byte[] tmp = digest.digest();
        String hash;
        hash = byteToString(tmp);
        Assert.assertEquals(hash, Utils.sha1String(new byte[0]));
        digest = MessageDigest.getInstance("sha1");
        digest.update((byte) 15);
        tmp = digest.digest();
        Assert.assertEquals(byteToString(tmp), Utils.sha1String(new byte[]{15}));
    }

    private String byteToString(byte[] tmp) {
        String hash = "";
        for (int i = 0; i < tmp.length; i++) {
            hash += Integer.toString((tmp[i] & 0xff) + 0x100, 16).substring(1);
        }
        return hash;
    }
}