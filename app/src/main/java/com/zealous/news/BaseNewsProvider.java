package com.zealous.news;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zealous.utils.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;

/**
 * Created by yaaminu on 4/25/17.
 */

@Module
public class BaseNewsProvider {

    @Provides
    public Realm getRealm(@NonNull RealmConfiguration configuration) {
        return Realm.getInstance(configuration);
    }

    @Provides
    @Singleton
    public RealmConfiguration getConfiguration() {
        return new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name("news.realm")
                .modules(new NewsRepo())
                .directory(Config.getApplicationContext().getDir("news.data", Context.MODE_PRIVATE)).build();
    }

    @Provides
    public NewsDataSource createDataSource(@NonNull Realm realm, @NonNull NewsLoader loader) {
        return new NewsDataSource(realm, loader);
    }

    @Singleton
    @Provides
    public NewsLoader loader(@NonNull OkHttpClient client, @NonNull Set<String> feedSources) {
        return new NewsLoader(client, feedSources);
    }

    @Singleton
    @Provides
    public Set<String> feedSources() {
        Set<String> sources = new HashSet<>(2);
        sources.add("https://rss.mordernghana.com/news.xml?cat_id=1&group_id=6");
        sources.add("http://feeds.reuters.com/reuters/businessNews");
        return sources;
    }

    @Provides
    public OkHttpClient client() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }
}
