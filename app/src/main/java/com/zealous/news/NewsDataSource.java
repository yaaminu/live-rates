package com.zealous.news;

import android.support.annotation.NonNull;

import com.zealous.utils.GenericUtils;

import java.io.Closeable;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import rx.functions.Func1;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsDataSource implements Closeable {

    @NonNull
    private final Realm realm;
    private final NewsLoader newsLoader;

    @Inject
    public NewsDataSource(@NonNull Realm realm, NewsLoader newsLoader) {
        GenericUtils.ensureNotNull(realm, newsLoader);
        this.realm = realm;
        ensureNotClosed();
        this.newsLoader = newsLoader;
    }


    public RealmQuery<NewsItem> makeQuery() {
        ensureNotClosed();
        return realm.where(NewsItem.class);
    }

    public rx.Observable<Boolean> loadNewsItems() {
        return newsLoader.loadNews().map(new Func1<List<NewsItem>, Boolean>() {
            @Override
            public Boolean call(List<NewsItem> newsItems) {
                RealmConfiguration configuration = realm.getConfiguration();
                Realm realm = Realm.getInstance(configuration);
                try {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(newsItems);
                    realm.commitTransaction();
                } finally {
                    realm.close();
                }
                return true;
            }
        });
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
}
