package com.zealous.news;

import android.support.annotation.NonNull;

import com.zealous.utils.GenericUtils;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by yaaminu on 4/24/17.
 */

@SuppressWarnings("FieldCanBeLocal")
public class NewsItem extends RealmObject {
    public static final String FIELD_DATE = "date";
    @Required
    private String title;
    @PrimaryKey
    private String url;
    @Required
    private String thumbnailUrl;
    @Required
    private String description;

    @SuppressWarnings("unused")
    @Index
    private long date;

    public NewsItem() {
    }

    NewsItem(@NonNull String title, @NonNull String url, @NonNull String thumbnailUrl, @NonNull String description, long date) {
        GenericUtils.ensureNotEmpty(title, url, thumbnailUrl, description);
        GenericUtils.ensureConditionTrue(date > 0, "invalid date");
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.date = date;
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
