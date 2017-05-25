package com.zealous.news;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zealous.R;
import com.zealous.utils.Config;

import java.util.HashMap;
import java.util.Map;
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
@Singleton
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
    public NewsLoader loader(@NonNull OkHttpClient client, @NonNull Map<String, Integer> feedSources) {
        return new NewsLoader(client, feedSources);
    }

    @Singleton
    @Provides
    public Map<String, Integer> feedSources() {
        Map<String, Integer> sources = new HashMap<>(2);
        sources.put("https://rss.modernghana.com/news.xml?cat_id=1&group_id=6", R.color.modern_ghana_color);
        sources.put("http://feeds.reuters.com/reuters/businessNews", R.color.reuters_color);
        sources.put("http://www.myjoyonline.com/pages/rss/site_business.xml", R.color.my_joy_online_color);
        sources.put("http://rss.cnn.com/rss/money_news_international.rss", R.color.bbc_color);
        sources.put("http://feeds.bbci.co.uk/news/business/rss.xml", R.color.bbc_color);
        sources.put("http://www.economist.com/sections/business-finance/rss.xml", R.color.orange);
        sources.put("http://www.economist.com/sections/economics/rss.xml", R.color.orange);
        sources.put("http://www.economist.com/topics/banking/index.xml", R.color.orange);
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
