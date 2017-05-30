package com.zealous.news;

import com.backup.BackupException;
import com.backup.Operation;
import com.google.gson.JsonObject;

/**
 * Created by yaaminu on 5/30/17.
 */

public class AddNewsFavoriteOperation implements Operation {

    //will be injected
    @android.support.annotation.Nullable
    NewsDataSource dataSource;

    @android.support.annotation.NonNull
    private JsonObject data;

    public AddNewsFavoriteOperation() {
        this.data = new JsonObject();
    }

    public AddNewsFavoriteOperation(NewsItem newsItem) {
        this.data = newsItem.toJson();
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public void setData(@android.support.annotation.NonNull JsonObject object) {
        this.data = object;
    }

    @Override
    public void replay() throws BackupException {
        if (dataSource == null) {
            throw new IllegalStateException("did you forget to inject the deps?");
        }
        dataSource.update(NewsItem.fromJson(data));
    }
}
