package com.zealous.news;

public class NewsItemBuilder {
    private String title;
    private String url;
    private String thumbnailUrl;
    private String description;

    public NewsItemBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public NewsItemBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public NewsItemBuilder setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public NewsItemBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public NewsItem createNewsItem() {
        return new NewsItem(title, url, thumbnailUrl, description);
    }
}