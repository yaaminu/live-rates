package com.zealous.news;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;
import com.zealous.R;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.ThreadUtils;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

import static com.zealous.utils.ViewUtils.hideViews;
import static com.zealous.utils.ViewUtils.showViews;

/**
 * Created by yaaminu on 4/24/17.
 */

public class NewsFragment extends BaseFragment implements NewsScreen {
    public static final String
            IS_FAVORITES = "isFavorites";
    @Bind(R.id.recycler_view)
    RecyclerView newsList;
    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.bt_try_again)
    View tryAgain;
    @Bind(R.id.empty_text_view)
    TextView emptyOrLoadingTextView;
    @Bind(R.id.progress)
    ProgressBar loadingProgress;
    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    List<NewsItem> newsItems = Collections.emptyList();
    @Inject
    NewsAdapter adapter;
    @Inject
    RecyclerView.LayoutManager layoutManager;
    @Inject
    NewsPresenter presenter;
    @Inject
    Picasso picasso;
    @Inject
    Cache cache;

    @Override
    protected int getLayout() {
        return R.layout.fragmet_news;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        GenericUtils.ensureNotNull(arguments);
        DaggerNewsFragmentComponent.builder()
                .newsFragmentProvider(new NewsFragmentProvider(this, arguments.getBoolean(IS_FAVORITES)))
                .build()
                .inject(this);
        presenter.onCreate(savedInstanceState, this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newsList.setLayoutManager(layoutManager);
        newsList.setAdapter(adapter);
        swipeRefresh.setColorSchemeResources(R.color.business_news_color_primary);
        if (presenter.isBookmarked()) {
            swipeRefresh.setRefreshing(false);
        } else {
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    presenter.loadNewsItems();
                }
            });
        }
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
    public void refreshDisplay(final List<NewsItem> dataSet, final boolean isFavorites) {
        if (ThreadUtils.isMainThread()) {
            doRefreshDisplay(dataSet, isFavorites);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doRefreshDisplay(dataSet, isFavorites);
                }
            });
        }
    }

    private void doRefreshDisplay(List<NewsItem> dataSet, boolean isFavorites) {
        this.newsItems = dataSet;
        if (newsItems.isEmpty()) {
            hideViews(newsList);
            showViews(emptyView);
            if (!isFavorites) {
                showViews(tryAgain);
                emptyOrLoadingTextView.setText(R.string.no_news);
            } else {
                hideViews(tryAgain, loadingProgress);
                emptyOrLoadingTextView.setText(R.string.no_favorites);
            }
        } else {
            showViews(newsList);
            hideViews(emptyView);
            adapter.notifyDataChanged("");
        }
    }

    @Override
    public void showLoading(final boolean loading) {
        if (ThreadUtils.isMainThread()) {
            doShowLoading(loading);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doShowLoading(loading);
                }
            });
        }
    }

    private void doShowLoading(boolean loading) {
        if (!isViewDestroyed() && !presenter.isBookmarked()) {
            swipeRefresh.setRefreshing(loading);
            if (loading && newsItems.isEmpty()) {
                showViews(emptyView, loadingProgress, emptyOrLoadingTextView);
            } else {
                hideViews(loadingProgress);
            }
        }
    }


    @Override
    public void onDestroyView() {
        swipeRefresh.setRefreshing(false);
        super.onDestroyView();
    }

    public static BaseFragment create(boolean isFavorites) {
        BaseFragment fragment = new NewsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(IS_FAVORITES, isFavorites);
        fragment.setArguments(bundle);
        return fragment;
    }

    @OnClick(R.id.bt_try_again)
    public void tryAgain() {
        if (!isViewDestroyed()) {
            presenter.loadNewsItems();
        }
    }
}
