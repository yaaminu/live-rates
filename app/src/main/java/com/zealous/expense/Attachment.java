package com.zealous.expense;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.google.gson.JsonObject;
import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.utils.FileUtils;
import com.zealous.utils.GenericUtils;

import io.realm.RealmObject;
import io.realm.annotations.Required;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.decode;

/**
 * Created by yaaminu on 4/22/17.
 */

public class Attachment extends RealmObject {
    static final String FIELD_TITLE = "title",
            FIELD_BLOB = "blob", FIELD_SHA1SUM = "sha1Sum", FIELD_MIME_TYPE = "mimeType";

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

    public Attachment(@NonNull String title, @NonNull byte[] blob, @NonNull String mimeType) throws ZealousException {
        //noinspection ConstantConditions
        if (title == null || title.trim().length() <= 0) {
            throw new IllegalArgumentException("title == null title.length() < 1");
        }
        //noinspection ConstantConditions
        if (mimeType == null || mimeType.trim().length() <= 0) {
            throw new IllegalArgumentException("mimetype == null mimeType.length() < 1");
        }

        //noinspection ConstantConditions
        if (blob == null || blob.length <= 0) {
            throw new IllegalArgumentException("blob is empty");
        }
        if (Expenditure.isTooLarge(blob)) {
            throw new ZealousException(GenericUtils.getString(R.string.attachment_too_large));
        }
        this.title = title.trim();
        this.blob = blob;

        this.mimeType = mimeType.trim();
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

    @DrawableRes
    public int getPlaceHolderIcon() {
        if (mimeType.startsWith("image/")) {
            return R.drawable.picture_preview;
        } else if (mimeType.equals("application/pdf")) {
            return R.drawable.pdf_preview;
        } else {
            return R.drawable.preview_unknown;
        }
    }

    @NonNull
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(FIELD_MIME_TYPE, getMimeType());
        jsonObject.addProperty(FIELD_TITLE, getTitle());
        jsonObject.addProperty(FIELD_SHA1SUM, getSha1Sum());
        //for compatibility, the json must contain the CRLF pair or else
        //it will break the parser
        jsonObject.addProperty(FIELD_BLOB, Base64.encodeToString(getBlob(), NO_WRAP));
        return jsonObject;
    }

    @NonNull
    public static Attachment fromJson(JsonObject jsonObject) {
        String mimeType = jsonObject.get(FIELD_MIME_TYPE).getAsString();
        String title = jsonObject.get(FIELD_TITLE).getAsString();
        String sha1sum = jsonObject.get(FIELD_SHA1SUM).getAsString();
        //for compatibility, the json must contain the CRLF pair or else
        //it will break the parser
        byte[] blob = decode(jsonObject.get(FIELD_BLOB).getAsString(), NO_WRAP);
        try {
            Attachment attachment = new Attachment(title, blob, mimeType);
            if (!sha1sum.equals(attachment.getSha1Sum())) {
                throw new IllegalStateException("checksum mismatch, attachment has been tampered with");
            }
            return attachment;
        } catch (ZealousException e) { //normally thrown when the blob is too large
            throw new RuntimeException(e);
        }
    }
}
