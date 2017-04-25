package com.zealous.news;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zealous.R;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/24/17.
 */

public class NewsFragment extends BaseFragment implements NewsScreen {
    @Bind(R.id.recycler_view)
    RecyclerView newsList;
    @Bind(R.id.empty_view)
    View emptyView;


    List<NewsItem> newsItems = Collections.emptyList();
    @Inject
    NewsAdapter adapter;
    @Inject
    RecyclerView.LayoutManager layoutManager;
    @Inject
    NewsPresenter presenter;

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
        presenter.onCreate(savedInstanceState, this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newsList.setLayoutManager(layoutManager);
        newsList.setAdapter(adapter);
    }

    @Override
    public Activity getCurrentActivity() {
        return getActivity();
    }

    @Nullable
    @Override
    protected BasePresenter<?> getBasePresenter() {
        return presenter;
    }

    @Override
    public void refreshDisplay(List<NewsItem> dataSet) {
        this.newsItems = dataSet;
        if (newsItems.isEmpty()) {
            com.zealous.utils.ViewUtils.hideViews(newsList);
            com.zealous.utils.ViewUtils.showViews(emptyView);
        } else {
            com.zealous.utils.ViewUtils.showViews(newsList);
            com.zealous.utils.ViewUtils.hideViews(emptyView);
            adapter.notifyDataChanged("");
        }
    }
}
