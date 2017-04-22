package com.zealous.expense;

import android.support.annotation.NonNull;

import com.zealous.utils.FileUtils;
import com.zealous.utils.GenericUtils;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by yaaminu on 4/22/17.
 */

public class Attachment extends RealmObject {
    @Required
    private String title;
    @Required
    private byte[] blob;

    @Required
    private String sha1Sum;

    @Required
    private String mimeType;

    public Attachment() {
        title = "";
        blob = new byte[]{};
    }

    public Attachment(@NonNull String title, @NonNull byte[] blob, String mimeType) {
        GenericUtils.ensureNotEmpty(title, mimeType);
        //noinspection ConstantConditions
        GenericUtils.ensureConditionTrue(blob != null && blob.length > 0, "invalid data");
        this.title = title;
        this.blob = blob;

        this.mimeType = mimeType;
        this.sha1Sum = FileUtils.sha1(blob);
    }

    @NonNull
    public byte[] getBlob() {
        return blob;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public String getMimeType() {
        return mimeType;
    }

    @NonNull
    public String getSha1Sum() {
        return sha1Sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attachment that = (Attachment) o;

        return sha1Sum.equals(that.sha1Sum);

    }

    @Override
    public int hashCode() {
        return sha1Sum.hashCode();
    }
}
