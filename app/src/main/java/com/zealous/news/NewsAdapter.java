package com.zealous.news;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.view.ViewGroup;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsAdapter extends BaseAdapter<NewsItemHolder, NewsItem> {
    final Delegate delegate;
    private final int maxDescriptionLength;

    private final Html.ImageGetter imageGetter;

    public NewsAdapter(Delegate delegate) {
        super(delegate);
        this.delegate = delegate;
        maxDescriptionLength = delegate.context().getResources().getInteger(R.integer.max_description_length);
        imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                return NewsAdapter.this.delegate.context().getResources().getDrawable(R.drawable.empty);
            }
        };
    }

    @Override
    protected void doBindHolder(NewsItemHolder holder, int position) {
        NewsItem item = getItem(position);
        holder.title.setText(item.getTitle());
        holder.datPublished.setText(DateUtils.getRelativeDateTimeString(delegate.context(), item.getDate(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        holder.source.setText(item.getSource());
        holder.sourceBar.setBackgroundResource(item.getPublisherColor());
        Spanned formattedDescription = Html.fromHtml(item.getDescription(), imageGetter, null);
        if (formattedDescription.length() > maxDescriptionLength) {
            holder.description.setText(formattedDescription.subSequence(0, maxDescriptionLength - 3));
            holder.description.append("...");
        } else {
            holder.description.setText(formattedDescription);
        }
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
