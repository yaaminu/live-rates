package com.zealous.news;

import android.support.annotation.NonNull;

import com.zealous.utils.GenericUtils;
import com.zealous.utils.TaskManager;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsDataSource implements Closeable {

    @NonNull
    private final Realm realm;

    @Inject
    public NewsDataSource(@NonNull Realm realm) {
        GenericUtils.ensureNotNull(realm);
        this.realm = realm;
        ensureNotClosed();
    }

    public List<NewsItem> findAll() {
        ensureNotClosed();
        // FIXME: 4/25/17 use real data
        return createDummyItems();
//        return makeQuery().findAllSorted(NewsItem.FIELD_DATE);
    }

    public RealmQuery<NewsItem> makeQuery() {
        ensureNotClosed();
        return realm.where(NewsItem.class);
    }

    public rx.Observable<Boolean> loadNewsItems() {
        return rx.Observable.from(TaskManager.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                // TODO: 4/25/17 kick of a task to load news from the internet
                return true;
            }
        }, true));
    }

    /**
     * closes this datasource freeing all resources.
     * It's important to remember that once closed, instances of this
     * datasource cannot be reused
     *
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     */
    @Override
    public void close() {
        realm.close();
    }

    private void ensureNotClosed() {
        //noinspection ConstantConditions
        GenericUtils.ensureConditionTrue(!realm.isClosed(), "can't use a closed datasource");
    }

    public static List<NewsItem> createDummyItems() {
        List<NewsItem> items = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            items.add(new NewsItemBuilder()
                    .setTitle("News item title " + i)
                    .setUrl("https://example.com/newsItem" + i)
                    .setThumbnailUrl("https://example.com/newsitem/thumnail" + i)
                    .setDescription("some description")
                    .setDate(System.currentTimeMillis())
                    .createNewsItem());
        }
        return items;
    }
}
