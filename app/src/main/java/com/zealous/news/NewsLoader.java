package com.zealous.news;

import android.support.annotation.NonNull;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.zealous.utils.PLog;
import com.zealous.utils.ThreadUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsLoader {

    private static final String TAG = "NewsLoader";
    private final Set<String> feedSources;
    private final OkHttpClient client;

    @Inject
    public NewsLoader(@NonNull OkHttpClient client, @NonNull Set<String> feedSources) {
        this.feedSources = feedSources;
        this.client = client;
    }

    public Observable<List<NewsItem>> loadNews() {
        PLog.d(TAG, "load news");
        return Observable.from(feedSources)
                .flatMap(new Func1<String, Observable<List<NewsItem>>>() {
                    @Override
                    public Observable<List<NewsItem>> call(String feedSource) {
                        return doLoad(feedSource);
                    }
                });
    }

    private Observable<List<NewsItem>> doLoad(@NonNull String feedSource) {
        // TODO: 4/25/17 avoid unnecessary calls
        ThreadUtils.ensureNotMain();
        Request request = new Request.Builder()
                .url(feedSource).build();
        Call call = client.newCall(request);
        InputStream is = null;
        try {
            is = call.execute().body().byteStream();
            SyndFeed syndFeed = new SyndFeedInput().build(new XmlReader(is));
            return Observable.just(syndFeed)
                    .map(new Func1<SyndFeed, List<NewsItem>>() {
                        @Override
                        public List<NewsItem> call(SyndFeed syndFeed) {
                            PLog.d(TAG, syndFeed.getTitle());
                            return feedToNewsItem(syndFeed.getEntries());
                        }
                    });
        } catch (FeedException e) {
            PLog.d(TAG, "error while reading feeds from %s. The feed seems to be malformed", feedSource);
            PLog.d(TAG, e.getMessage(), e);
        } catch (IOException e) {
            PLog.d(TAG, "io exception while fetching feeds from %s", feedSource);
            PLog.d(TAG, e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return Observable.just(Collections.<NewsItem>emptyList());
    }

    @NonNull
    private List<NewsItem> feedToNewsItem(List<SyndEntry> entries) {
        List<NewsItem> items = new ArrayList<>(entries.size());
        for (SyndEntry syndEntry : entries) {
            PLog.d(TAG, "mapping %s to news Items", syndEntry);
            NewsItem newsItem = new NewsItemBuilder()
                    .setDate(syndEntry.getPublishedDate().getTime())
                    .setDescription(syndEntry.getDescription().getValue())
                    .setTitle(syndEntry.getTitle())
                    .setUrl(syndEntry.getUri())
                    .setThumbnailUrl(syndEntry.getUri()).createNewsItem();
            items.add(newsItem);
        }
        return items;
    }


}
