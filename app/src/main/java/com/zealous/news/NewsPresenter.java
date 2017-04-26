package com.zealous.news;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsPresenter extends BasePresenter<NewsScreen> {
    private static final String TAG = "NewsPresenter";

    @NonNull
    private List<NewsItem> dataSet;

    private NewsDataSource dataSource;

    @Nullable
    private Subscription loadNewsSubscription;

    @Inject
    public NewsPresenter(@NonNull NewsDataSource dataSource) {
        this.dataSource = dataSource;
        this.dataSet = Collections.emptyList();
    }

    @Nullable
    private NewsScreen screen;

    @Override
    public void onCreate(@Nullable Bundle savedState, @NonNull NewsScreen screen) {
        this.screen = screen;
        loadNews();
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: 4/25/17 use makeQuery() instead of findAll()
        dataSet = dataSource.makeQuery().findAllSortedAsync(NewsItem.FIELD_DATE);
        ((RealmResults<NewsItem>) dataSet).addChangeListener(changeListener);
        updateUi();
    }

    private void updateUi() {
        GenericUtils.ensureNotNull(screen);
        assert screen != null;
        screen.refreshDisplay(dataSet);
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
        if (true || loadNewsSubscription == null || loadNewsSubscription.isUnsubscribed()) {
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
}
