package com.zealous.news;

import com.squareup.picasso.Cache;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.zealous.BuildConfig;
import com.zealous.utils.Config;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/28/17.
 */

@Singleton
@Module
public class PicasoProvider {

    @Provides
    @Singleton
    public Picasso picasso(Cache cache) {
        return new Picasso.Builder(Config.getApplicationContext())
                .loggingEnabled(BuildConfig.DEBUG)
                .memoryCache(cache)
                .build();
    }

    @Provides
    @Singleton
    public Cache cache() {
        return new LruCache(Config.getApplicationContext());
    }
}
