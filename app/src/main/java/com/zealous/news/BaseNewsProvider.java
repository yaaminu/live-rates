package com.zealous.news;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zealous.utils.Config;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

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
    public NewsDataSource createDataSource(Realm realm) {
        return new NewsDataSource(realm);
    }
}
