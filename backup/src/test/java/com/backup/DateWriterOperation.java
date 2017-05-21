package com.backup;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

/**
 * Created by yaaminu on 5/20/17.
 */
class DateWriterOperation implements Operation {

    @Nullable
    private JsonObject data;

    public DateWriterOperation() {
    }

    public DateWriterOperation(File file, byte[] blob) {
        this.data = new JsonObject();
        this.data.addProperty("path", file.getAbsolutePath());
        this.data.addProperty("lastModified", file.lastModified());
        this.data.addProperty("blob", org.apache.commons.codec.binary.Base64.encodeBase64String(blob));
    }

    @NonNull
    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public void setData(@NonNull JsonObject object) {
        this.data = object;
    }

    @Override
    public void replay() throws BackupException {
        if (data == null) {
            throw new IllegalStateException("replay invoked while data is not yet available");
        }
        File file = new File(data.get("path").getAsString());
        byte[] blob = org.apache.commons.codec.binary.Base64.decodeBase64(data().get("blob").getAsString());
        try {
            writeByteArrayToFile(file, blob, true);
            if (!file.setLastModified(data.get("lastModified").getAsLong())) {
                throw new BackupException(BackupException.EIOERROR, "failed to update last modified of the file", null);
            }
        } catch (IOException e) {
            throw new BackupException(BackupException.EIOERROR, e.getMessage(), e);
        }
    }
}
