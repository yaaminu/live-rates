package com.zealous.news;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.utils.ConnectionUtils;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.ThreadUtils;

import org.apache.commons.io.IOUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsLoader {

    private static final String TAG = "NewsLoader";
    public static final String BROKEN_THUMNAIL = "broken_thumnail";
    private final Map<String, Integer> feedSources;
    private final OkHttpClient client;
    private final Map<String, Long> lastUpdated;
    private static final long MAX_AGE = TimeUnit.MINUTES.toMillis(10);

    @Inject
    public NewsLoader(@NonNull OkHttpClient client,
                      @NonNull Map<String, Integer> feedSources) {
        this.feedSources = feedSources;
        this.client = client;
        lastUpdated = new ConcurrentHashMap<>(feedSources.size());
    }

    public Observable<List<NewsItem>> loadNews() {
        PLog.d(TAG, "load news");
        if (!ConnectionUtils.isConnected()) {
            PLog.d(TAG, "not connected to the internet");
            return Observable.error(new ZealousException(GenericUtils.getString(R.string.no_internet_connection)));
        }
        return Observable.from(feedSources.keySet())
                .filter(new Func1<String, Boolean>() { // filter out some sites
                    @Override
                    public Boolean call(String s) {
                        Long updated = lastUpdated.get(s);
                        return updated == null || updated > MAX_AGE;
                    }
                })
                .flatMap(new Func1<String, Observable<List<NewsItem>>>() {
                    @Override
                    public Observable<List<NewsItem>> call(String feedSource) {
                        return doLoad(feedSource);
                    }
                });
    }

    private Observable<List<NewsItem>> doLoad(@NonNull final String feedSource) {
        // TODO: 4/25/17 avoid unnecessary calls
        ThreadUtils.ensureNotMain();
        Request request = new Request.Builder()
                .url(feedSource).build();
        Call call = client.newCall(request);
        InputStream is = null;
        try {
            is = call.execute().body().byteStream();
            SyndFeed syndFeed = new SyndFeedInput().build(new XmlReader(is));
            final String source = new URL(syndFeed.getLink()).getHost();
            return Observable.just(syndFeed)
                    .map(new Func1<SyndFeed, List<NewsItem>>() {
                        @Override
                        public List<NewsItem> call(SyndFeed syndFeed) {
                            PLog.d(TAG, syndFeed.getTitle());
                            return feedToNewsItem(source, feedSources.get(feedSource), syndFeed.getEntries());
                        }
                    }).doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            lastUpdated.put(feedSource, SystemClock.uptimeMillis());
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
    private List<NewsItem> feedToNewsItem(String source, int publisherColor, List<SyndEntry> entries) {
        List<NewsItem> items = new ArrayList<>(entries.size());
        for (SyndEntry syndEntry : entries) {
            PLog.d(TAG, "mapping %s to news Items", syndEntry.getLink());
            Document jsoup = Jsoup.parse(syndEntry.getDescription().getValue());
            jsoup.select("img").remove();
            jsoup.select("a").remove();
            NewsItem newsItem = new NewsItemBuilder()
                    .setDate(syndEntry.getPublishedDate().getTime())
                    .setDescription(jsoup.toString().replaceAll("\\n+", ""))
                    .setTitle(syndEntry.getTitle())
                    .setUrl(syndEntry.getLink())
                    .setPublisherColor(publisherColor)
                    .setSource(source)
                    .setThumbnailUrl(getThumbnail(syndEntry)).createNewsItem();
            items.add(newsItem);
        }
        return items;
    }

    private String getThumbnail(SyndEntry syndEntry) {
        List<Element> foreignMarkup = syndEntry.getForeignMarkup();
        for (Element element : foreignMarkup) {
            Attribute attribute = element.getAttribute("url");
            if (attribute != null) {
                try {
                    String value = attribute.getValue();
                    URL url = new URL(value);
                    PLog.d(TAG, "thumbnail for %s is %s", syndEntry.getTitle(), url);
                    return url.toExternalForm();
                } catch (MalformedURLException e) {
                    PLog.d(TAG, e.getMessage(), e);
                }
            }
        }
        Elements imgs = Jsoup.parse(syndEntry.getDescription().getValue()).select("img");
        if (imgs.size() == 1) {
            String src = imgs.get(0).attr("src");
            PLog.d(TAG, "thumbnail for %s is %s", syndEntry.getTitle(), src);
            return src;
        }
        return BROKEN_THUMNAIL;
    }


}
