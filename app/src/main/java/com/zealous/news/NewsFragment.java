package com.zealous.news;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zealous.R;
import com.zealous.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/24/17.
 */

public class NewsFragment extends BaseFragment {
    @Bind(R.id.recycler_view)
    RecyclerView newsList;
    final List<NewsItem> newsItems = createDummyItems();

    @Inject
    NewsAdapter adapter;
    @Inject
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected int getLayout() {
        return R.layout.fragmet_news;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerNewsFragmentComponent.builder()
                .newsFragmentProvider(new NewsFragmentProvider(this))
                .build()
                .inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newsList.setLayoutManager(layoutManager);
        newsList.setAdapter(adapter);
    }

    public static List<NewsItem> createDummyItems() {
        List<NewsItem> items = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            items.add(new NewsItemBuilder()
                    .setTitle("News item title " + i)
                    .setUrl("https://example.com/newsItem" + i)
                    .setThumbnailUrl("https://example.com/newsitem/thumnail" + i)
                    .setDescription("some description")
                    .createNewsItem());
        }
        return items;
    }
}
