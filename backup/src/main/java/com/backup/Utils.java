package com.backup;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yaaminu on 5/20/17.
 */

public class Utils {

    public static String bytesToString(byte[] source) {
        String hashString = "";
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < source.length; i++) {
            hashString += Integer.toString((source[i] & 0xff) + 0x100, 16).substring(1);
        }
        return hashString;
    }

    static String sha1String(byte[] blob) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("sha1");
        digest.update(blob);
        return bytesToString(digest.digest());
    }
}
