package com.zealous.news;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.view.ViewGroup;

import com.squareup.picasso.Cache;
import com.squareup.picasso.RequestCreator;
import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.utils.ViewUtils;

import javax.inject.Inject;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsAdapter extends BaseAdapter<NewsItemHolder, NewsItem> {
    final Delegate delegate;
    private final int maxDescriptionLength;

    private final int width, height;

    @Inject
    public NewsAdapter(@NonNull Delegate delegate) {
        super(delegate);
        this.delegate = delegate;
        maxDescriptionLength = delegate.context().getResources().getInteger(R.integer.max_description_length);
        this.width = delegate.context().getResources().getDimensionPixelSize(R.dimen.news_item_thumbnail_width);
        this.height = delegate.context().getResources().getDimensionPixelSize(R.dimen.news_item_thumbnail_height);
    }

    @Override
    protected void doBindHolder(final NewsItemHolder holder, int position) {
        NewsItem item = getItem(position);
        holder.title.setText(item.getTitle());
        holder.datPublished.setText(DateUtils.getRelativeDateTimeString(delegate.context(), item.getDate(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        holder.source.setText(item.getSource());
        Spanned formattedDescription = Html.fromHtml(item.getDescription());
        if (formattedDescription.length() > maxDescriptionLength) {
            holder.description.setText(formattedDescription.subSequence(0, maxDescriptionLength - 3));
            holder.description.append("...");
        } else {
            holder.description.setText(formattedDescription);
        }
        if (!item.getThumbnailUrl().equals(NewsLoader.BROKEN_THUMNAIL)) {
            ViewUtils.showViews(holder.thumbnail);
            Bitmap bitmap = delegate.cache().get(item.getThumbnailUrl());
            if (bitmap != null) {
                holder.thumbnail.setImageBitmap(bitmap);
            } else {
                holder.thumbnail.setImageBitmap(null);
                delegate.loadThumbnail(item)
                        .resize(width, height)
                        .centerCrop()
                        .into(new WeakTarget(holder.thumbnail));
            }
        } else {
            ViewUtils.hideViews(holder.thumbnail);
        }
    }

    @Override
    public NewsItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NewsItemHolder(inflater.inflate(R.layout.news_list_item, parent, false), this);
    }

    interface Delegate extends BaseAdapter.Delegate<NewsItemHolder, NewsItem> {
        void bookmark(NewsItem item);

        void shareItem(NewsItem item);

        @NonNull
        Cache cache();

        @NonNull
        RequestCreator loadThumbnail(NewsItem item);
    }
}
