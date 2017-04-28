package com.zealous.news;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Cache;
import com.squareup.picasso.RequestCreator;
import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.utils.GenericUtils;

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
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .addDefaultShareMenuItem()
                .setToolbarColor(ContextCompat.getColor(fragment.getContext(), R.color.business_news_color_primary))
                .setShowTitle(true)
                .setCloseButtonIcon(BitmapFactory.decodeResource(fragment.getResources(), R.drawable.ic_arrow_back_black_24dp))
                .setSecondaryToolbarColor(ContextCompat.getColor(fragment.getContext(),
                        R.color.business_news_color_primary_dark)).build();
        customTabsIntent.intent.setPackage("com.android.chrome");
        try {
            customTabsIntent.launchUrl(fragment.getContext(), Uri.parse(adapter.getItem(position).getUrl()));
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(adapter.getItem(position).getUrl()));
            fragment.getContext().startActivity(intent);
        }
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

    @Override
    public void bookmark(NewsItem item) {
        fragment.presenter.toggleBookmark(item);
        Toast.makeText(fragment.getContext(), item.isBookmarked() ? R.string.bookmark_success : R.string.bookmark_removed_succss, Toast.LENGTH_LONG).
                show();
    }

    @Override
    public void shareItem(NewsItem item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        StringBuilder text = new StringBuilder(item.getUrl().length() + item.getTitle().length() + 10)
                .append(item.getTitle())
                .append("\n\n\n")
                .append(GenericUtils.getString(R.string.follow_link))
                .append("\n")
                .append(item.getUrl());
        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        try {
            fragment.getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(fragment.getContext(), R.string.no_sharing_app, Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    @Override
    public Cache cache() {
        return fragment.cache;
    }

    @NonNull
    @Override
    public RequestCreator loadThumbnail(NewsItem item) {
        return fragment.picasso.load(item.getThumbnailUrl());
    }
}
