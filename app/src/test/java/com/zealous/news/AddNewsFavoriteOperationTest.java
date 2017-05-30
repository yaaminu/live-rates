package com.zealous.news;

import com.google.gson.JsonObject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by yaaminu on 5/30/17.
 */
public class AddNewsFavoriteOperationTest {
    @Test
    public void data() throws Exception {
        AddNewsFavoriteOperation operation = new AddNewsFavoriteOperation();
        operation.dataSource = mock(NewsDataSource.class);
        assertEquals(operation.data(), new JsonObject());

        NewsItem expected = new NewsItem("title", "link", "url", "descre", 1, "bbc", true);

        operation = new AddNewsFavoriteOperation(expected);
        assertNotNull(operation.data());
        assertEquals(expected.toJson(), operation.data());

        operation = new AddNewsFavoriteOperation(expected);
        assertEquals(expected.toJson(), operation.data());
    }

    @Test
    public void setData() throws Exception {
        AddNewsFavoriteOperation operation = new AddNewsFavoriteOperation();
        assertEquals(operation.data(), new JsonObject());
        JsonObject test = new JsonObject();
        test.addProperty("foo", "bar");
        operation.setData(test);
        assertEquals(test.toString(), operation.data().toString());
    }

    @Test
    public void replay() throws Exception {

    }

}