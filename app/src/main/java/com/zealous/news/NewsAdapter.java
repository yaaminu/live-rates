package com.zealous.news;

import android.text.Html;
import android.text.format.DateUtils;
import android.view.ViewGroup;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsAdapter extends BaseAdapter<NewsItemHolder, NewsItem> {
    final Delegate delegate;

    public NewsAdapter(Delegate delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    protected void doBindHolder(NewsItemHolder holder, int position) {
        NewsItem item = getItem(position);
        holder.title.setText(item.getTitle());
        holder.datPublished.setText(DateUtils.getRelativeDateTimeString(delegate.context(), item.getDate(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        holder.source.setText(item.getSource());
        holder.sourceBar.setBackgroundResource(item.getPublisherColor());
        holder.description.setText(Html.fromHtml(item.getDescription()));
    }

    @Override
    public NewsItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NewsItemHolder(inflater.inflate(R.layout.news_list_item, parent, false), this);
    }

    interface Delegate extends BaseAdapter.Delegate<NewsItemHolder, NewsItem> {
        void bookmark(NewsItem item);

        void shareItem(NewsItem item);
    }
}
