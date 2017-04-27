package com.zealous.news;

import android.support.annotation.ColorRes;
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
    private boolean read;
    @SuppressWarnings("unused")
    @Index
    private long date;
    private String source;
    private int publisherColor;

    public NewsItem() {
    }

    NewsItem(@NonNull String title, @NonNull String url, @NonNull String thumbnailUrl,
             @NonNull String description,
             long date, String source,
             int publisherColor) {
        GenericUtils.ensureNotEmpty(title, url, thumbnailUrl, description, source);
        GenericUtils.ensureConditionTrue(date > 0, "invalid date");
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.date = date;
        this.publisherColor = publisherColor;
        this.source = source;
    }

    void setRead(boolean read) {
        this.read = read;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public long getDate() {
        return date;
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

    public String getSource() {
        return source;
    }

    @ColorRes
    public int getPublisherColor() {
        return publisherColor;
    }
}
