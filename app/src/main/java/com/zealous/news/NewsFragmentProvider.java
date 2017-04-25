package com.zealous.news;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/25/17.
 */

@Module
public class NewsFragmentProvider {

    private final NewsFragment fragment;

    public NewsFragmentProvider(NewsFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @Singleton
    public NewsAdapter getNewsAdapter(NewsAdapter.Delegate delegate) {
        return new NewsAdapter(delegate);
    }

    @Provides
    @Singleton
    public NewsAdapter.Delegate getDelegate() {
        return new NewsAdapterDelegateImpl(fragment);
    }

    @Provides
    @Singleton
    public RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(fragment.getContext(), LinearLayoutManager.VERTICAL, false);
    }

    @Provides
    @Singleton
    public NewsPresenter getPresenter(@NonNull NewsDataSource dataSource) {
        return new NewsPresenter(dataSource);
    }
}
