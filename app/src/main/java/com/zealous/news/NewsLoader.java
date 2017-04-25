package com.zealous.news;

import android.os.SystemClock;

import com.zealous.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsLoader {

    public Observable<List<NewsItem>> loadNews() {
        return Observable.just(0)
                .flatMap(new Func1<Integer, Observable<List<NewsItem>>>() {
                    @Override
                    public Observable<List<NewsItem>> call(Integer integer) {
                        return doLoad();
                    }
                });
    }

    private Observable<List<NewsItem>> doLoad() {
        // TODO: 4/25/17 avoid unnecessary calls
        ThreadUtils.ensureNotMain();
        List<NewsItem> items = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            SystemClock.sleep(1000);
            items.add(new NewsItemBuilder()
                    .setTitle("News item title " + i)
                    .setUrl("https://example.com/newsItem" + i)
                    .setThumbnailUrl("https://example.com/newsitem/thumnail" + i)
                    .setDescription("some description")
                    .setDate(System.currentTimeMillis())
                    .createNewsItem());
        }
        return Observable.just(items);
    }
}
