package com.zealous.news;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.squareup.picasso.Cache;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by yaaminu on 4/28/17.
 */
public class CacheImpl implements Cache {

    private final LruCache<String, Bitmap> lruCache;

    public CacheImpl() {
        lruCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 8));
    }

    @Override
    public Bitmap get(String key) {
        return lruCache.get(key);
    }

    @Override
    public void set(String key, Bitmap bitmap) {
        lruCache.put(key, bitmap);
    }

    @Override
    public int size() {
        return lruCache.size();
    }

    @Override
    public int maxSize() {
        return lruCache.maxSize();
    }

    @Override
    public void clear() {
        lruCache.evictAll();
    }

    @Override
    public void clearKeyUri(String keyPrefix) {
        //The fact that we are only synchronizing here makes this cache implementation prone
        //to race conditions but that should not be that detrimental so we trade consistency
        // with performance
        synchronized (lruCache) {
            Map<String, Bitmap> snapshot = lruCache.snapshot();
            List<String> keys = new LinkedList<>();
            for (String key : snapshot.keySet()) {
                if (key.startsWith(keyPrefix)) {
                    keys.add(key);
                }
            }
            for (String key : keys) {
                lruCache.remove(key);
            }
        }
    }
}
