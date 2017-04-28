package com.zealous.news;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Cache;
import com.squareup.picasso.RequestCreator;
import com.zealous.R;
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
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(adapter.getItem(position).getUrl()));
        try {
            fragment.getCurrentActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(fragment.getCurrentActivity(), R.string.no_browser, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(fragment.getContext(), "bookmarking " + item.getTitle(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void shareItem(NewsItem item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, item.getUrl());
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
