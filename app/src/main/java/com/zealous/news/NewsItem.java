package com.zealous.news;

import android.support.annotation.NonNull;

import com.zealous.utils.GenericUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by yaaminu on 4/24/17.
 */

public class NewsItem extends RealmObject {
    @Required
    private String title;
    @PrimaryKey
    private String url;
    @Required
    private String thumbnailUrl;
    @Required
    private String description;

    public NewsItem() {
    }

    NewsItem(@NonNull String title, @NonNull String url, @NonNull String thumbnailUrl, @NonNull String description) {
        GenericUtils.ensureNotEmpty(title, url, thumbnailUrl, description);
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getUrl() {
        return url;
    }
}
