package com.zealous.news;

import android.view.ViewGroup;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsAdapter extends BaseAdapter<NewsItemHolder, NewsItem> {
    public NewsAdapter(Delegate delegate) {
        super(delegate);
    }

    @Override
    protected void doBindHolder(NewsItemHolder holder, int position) {
        holder.title.setText(getItem(position).getTitle());
    }

    @Override
    public NewsItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NewsItemHolder(inflater.inflate(R.layout.news_list_item, parent, false));
    }

    interface Delegate extends BaseAdapter.Delegate<NewsItemHolder, NewsItem> {
    }
}
