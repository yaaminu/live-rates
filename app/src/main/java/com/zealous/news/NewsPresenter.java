package com.zealous.news;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.backup.BackupException;
import com.zealous.R;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.realm.Case;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import static com.zealous.exchangeRates.ExchangeRateListActivity.SEARCH;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsPresenter extends BasePresenter<NewsScreen> {
    private static final String TAG = "NewsPresenter";
    private final EventBus bus;
    private final boolean bookmarked;

    @NonNull
    private List<NewsItem> dataSet;

    private NewsDataSource dataSource;

    @Nullable
    private Subscription loadNewsSubscription;

    @Inject
    public NewsPresenter(@NonNull NewsDataSource dataSource, @NonNull EventBus bus, boolean bookmarked) {
        this.dataSource = dataSource;
        this.dataSet = Collections.emptyList();
        this.bus = bus;
        this.bookmarked = bookmarked;
    }

    @Nullable
    private NewsScreen screen;

    @Override
    public void onCreate(@Nullable Bundle savedState, @NonNull NewsScreen screen) {
        this.screen = screen;
        bus.register(this);
        loadNews();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if (event instanceof Map) {
            if (((Map) event).containsKey(SEARCH)) {
                String constraint = ((String) ((Map) event).get(SEARCH));
                GenericUtils.ensureNotNull(constraint);
                updateRecords(constraint);
                return;
            }
        }
        PLog.w(TAG, "unknown event %s", event);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateRecords("");
        updateUi();
    }

    private void updateRecords(String constraint) {
        RealmQuery<NewsItem> query = dataSource.makeQuery();
        if (bookmarked) {
            query.equalTo(NewsItem.FIELD_BOOKMARKED, true);
        }
        query.beginGroup()
                .beginsWith(NewsItem.FIELD_SOURCE, constraint, Case.INSENSITIVE)
                .or()
                .contains(NewsItem.FIELD_TITLE, constraint, Case.INSENSITIVE)
                .or()
                .contains(NewsItem.FIELD_DESCRIPTION, constraint, Case.INSENSITIVE)
                .endGroup();

        dataSet = query.findAllSortedAsync(NewsItem.FIELD_DATE, Sort.DESCENDING);
        ((RealmResults<NewsItem>) dataSet).addChangeListener(changeListener);
    }

    private void updateUi() {
        GenericUtils.ensureNotNull(screen);
        assert screen != null;
        screen.refreshDisplay(dataSet, bookmarked);
    }

    private void loadNews() {
        loadNewsSubscription = dataSource.loadNewsItems()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        PLog.d(TAG, "loading");
                        if (screen != null) {
                            screen.showLoading(true);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    @Override
    public void onDestroy() {
        if (loadNewsSubscription != null && !loadNewsSubscription.isUnsubscribed()) {
            loadNewsSubscription.unsubscribe();
        }
        dataSource.close();
        bus.unregister(this);
    }


    private final Subscriber<Boolean> subscriber = new Subscriber<Boolean>() {
        @Override
        public void onCompleted() {
            // the dataSet will actually notify us that it has changed
            // in the change listener. So we wait till that notification and update the UI
            PLog.d(TAG, "news loading completed at %s", new Date());
            if (screen != null) {
                screen.showLoading(false);
            }
            // TODO: 4/25/17 uncomment this when we start loading news from online
            updateUi();
        }

        @Override
        public void onError(Throwable e) {
            PLog.e(TAG, "loading news failed", e);
            if (screen != null) {
                screen.showLoading(false);
            }
        }

        @Override
        public void onNext(Boolean aBoolean) {
            if (aBoolean) {
                updateUi();
                PLog.d(TAG, "next loading news succeeded");
            } else {
                PLog.w(TAG, "loading news failed");
            }
        }
    };

    public void loadNewsItems() {
        if (!bookmarked && (loadNewsSubscription == null || loadNewsSubscription.isUnsubscribed())) {
            loadNews();
        } else {
            assert screen != null;
            screen.showLoading(false);
            PLog.d(TAG, "already loading");
        }
    }


    private final RealmChangeListener<RealmResults<NewsItem>> changeListener = new RealmChangeListener<RealmResults<NewsItem>>() {
        @Override
        public void onChange(RealmResults<NewsItem> element) {
            updateUi();
        }
    };

    public void toggleBookmark(NewsItem item) {
        ThreadUtils.ensureMain();
        item = new NewsItem(item.getTitle(), item.getUrl(), item.getThumbnailUrl(), item.getDescription(),
                item.getDate(), item.getSource(), !item.isBookmarked());
        try {
            dataSource.update(item);
        } catch (BackupException e) {
            if (screen != null) {
                screen.showDialogMessage(R.string.failed_to_add_favorite);
            }
        }
    }

    public boolean isBookmarked() {
        return bookmarked;
    }
}
