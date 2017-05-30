package com.zealous.news;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
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
    public static final String FIELD_SOURCE = "source";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_BOOKMARKED = "bookmarked";
    public static final String FIELD_URL = "url";
    private static String FIELD_THUMBNAIL_URI = "thumbnailUrl";

    @Required
    private String title;
    @PrimaryKey
    private String url;
    @Required
    private String thumbnailUrl;
    @Required
    private String description;
    private boolean bookmarked;
    @SuppressWarnings("unused")
    @Index
    private long date;
    private String source;

    public NewsItem() {
    }

    NewsItem(@NonNull String title, @NonNull String url, @NonNull String thumbnailUrl,
             @NonNull String description,
             long date, String source, boolean bookmarked) {
        GenericUtils.ensureNotEmpty(title, url, thumbnailUrl, description, source);
        GenericUtils.ensureConditionTrue(date > 0, "invalid date");
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.date = date;
        this.source = source;
        this.bookmarked = bookmarked;
    }

    void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
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

    public boolean isBookmarked() {
        return bookmarked;
    }


    @NonNull
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(FIELD_TITLE, getTitle());
        jsonObject.addProperty(FIELD_DESCRIPTION, getDescription());
        jsonObject.addProperty(FIELD_URL, getUrl());
        jsonObject.addProperty(FIELD_SOURCE, getSource());
        jsonObject.addProperty(FIELD_DATE, getDate());
        jsonObject.addProperty(FIELD_BOOKMARKED, isBookmarked());
        jsonObject.addProperty(FIELD_THUMBNAIL_URI, getThumbnailUrl());
        return jsonObject;
    }

    @NonNull
    public static NewsItem fromJson(JsonObject jsonObject) {
        String title = jsonObject.get(FIELD_TITLE).getAsString(),
                description = jsonObject.get(FIELD_DESCRIPTION).getAsString(),
                url = jsonObject.get(FIELD_URL).getAsString(),
                source = jsonObject.get(FIELD_SOURCE).getAsString(),
                thumbnailUrl = jsonObject.get(FIELD_THUMBNAIL_URI).getAsString();

        long date = jsonObject.get(FIELD_DATE).getAsLong();
        boolean isBookmarked = jsonObject.get(FIELD_BOOKMARKED).getAsBoolean();
        return new NewsItem(title, url, thumbnailUrl, description, date, source, isBookmarked);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewsItem item = (NewsItem) o;

        //noinspection ConstantConditions
        return getUrl() != null ? getUrl().equals(item.getUrl()) : item.getUrl() == null;

    }

    @Override
    public int hashCode() {
        //noinspection ConstantConditions
        return getUrl() != null ? getUrl().hashCode() : 0;
    }
}
