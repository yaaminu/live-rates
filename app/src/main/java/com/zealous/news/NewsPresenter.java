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

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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
    private Subscription subscription;

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
        dataSet = dataSource.findAll();
        udpateUi();
    }

    private void udpateUi() {
        GenericUtils.ensureNotNull(screen);
        assert screen != null;
        screen.refreshDisplay(dataSet);
    }

    private void loadNews() {
        subscription = dataSource.loadNewsItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    @Override
    public void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        dataSource.close();
    }


    private final Subscriber<Boolean> subscriber = new Subscriber<Boolean>() {
        @Override
        public void onCompleted() {
            PLog.d(TAG, "news loading completed at %s", new Date());
        }

        @Override
        public void onError(Throwable e) {
            PLog.e(TAG, "loading news failed", e);
        }

        @Override
        public void onNext(Boolean aBoolean) {
            PLog.d(TAG, "next loading news");
        }
    };
}
