package com.zealous.news;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static junit.framework.Assert.fail;

/**
 * Created by yaaminu on 5/30/17.
 */
public class NewsItemTest {
    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void toJson() throws Exception {
        NewsItem item = new NewsItem("title", "url", "thumnailurl", "des", 1, "sources", false);
        Gson gson = new Gson();
        Assert.assertEquals(gson.toJsonTree(item, NewsItem.class).getAsJsonObject(), item.toJson());

        item = new NewsItem("title", "link", "url", "descre", 1, "bbc", true);
        Assert.assertEquals(gson.toJsonTree(item, NewsItem.class).getAsJsonObject(), item.toJson());
    }

    @Test
    public void fromJson() throws Exception {
        NewsItem item = new NewsItem("title", "url", "thumnailurl", "des", 1, "sources", false);
        Gson gson = new Gson();
        JsonObject json = gson.toJsonTree(item, NewsItem.class).getAsJsonObject();
        Assert.assertEquals(item, NewsItem.fromJson(json));

        item = new NewsItem("title", "link", "url", "descre", 1, "bbc", true);
        json = gson.toJsonTree(item, NewsItem.class).getAsJsonObject();

        Assert.assertEquals(item, NewsItem.fromJson(json));

        NewsItem actual = NewsItem.fromJson(json);
        Assert.assertEquals(item.getTitle(), actual.getTitle());
        Assert.assertEquals(item.getDescription(), actual.getDescription());
        Assert.assertEquals(item.getDate(), actual.getDate());
        Assert.assertEquals(item.getUrl(), actual.getUrl());
        Assert.assertEquals(item.isBookmarked(), actual.isBookmarked());
        Assert.assertEquals(item.getSource(), actual.getSource());
        Assert.assertEquals(item.getThumbnailUrl(), actual.getThumbnailUrl());

    }

    @Test
    public void replay() throws Exception {
        AddNewsFavoriteOperation op = new AddNewsFavoriteOperation();
        try {
            op.dataSource = null;
            op.replay();
            fail("must throw illegalStateException when the dataSource is not injecetd");
        } catch (IllegalStateException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        op.dataSource = PowerMockito.mock(NewsDataSource.class);
        op.dataSource = PowerMockito.spy(op.dataSource);

        Mockito.verify(op.dataSource, Mockito.times(0)).update(Mockito.any(NewsItem.class));

        NewsItem item = new NewsItem("title", "link", "url", "descre", 1, "bbc", true);
        op.setData(item.toJson());
        op.replay();
        Mockito.verify(op.dataSource, Mockito.times(1)).update(item);
    }
}