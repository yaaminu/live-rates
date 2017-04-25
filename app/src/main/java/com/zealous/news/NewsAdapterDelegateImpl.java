package com.zealous.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.zealous.adapter.BaseAdapter;

import java.util.List;

/**
 * Created by yaaminu on 4/25/17.
 */
public class NewsAdapterDelegateImpl implements NewsAdapter.Delegate {


    private final NewsFragment fragment;

    public NewsAdapterDelegateImpl(NewsFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public Context context() {
        return fragment.getContext();
    }

    @Override
    public void onItemClick(BaseAdapter<NewsItemHolder, NewsItem> adapter, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(BaseAdapter<NewsItemHolder, NewsItem> adapter, View view, int position, long id) {
        return false;
    }

    @NonNull
    @Override
    public List<NewsItem> dataSet(String constrain) {
        return fragment.newsItems;
    }
}
