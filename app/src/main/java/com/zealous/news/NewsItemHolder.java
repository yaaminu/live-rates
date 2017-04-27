package com.zealous.news;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsItemHolder extends BaseAdapter.Holder {
    private final NewsAdapter adapter;
    @Bind(R.id.tv_feed_title)
    TextView title;
    @Bind(R.id.tv_date_published)
    TextView datPublished;
    @Bind(R.id.iv_feed_thumbnail)
    ImageView thumbnail;
    @Bind(R.id.tv_source)
    TextView source;
    @Bind(R.id.source_bar)
    View sourceBar;
    @Bind(R.id.tv_description)
    TextView description;

    public NewsItemHolder(View itemView, NewsAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
    }

    @OnClick({R.id.ib_bookmark, R.id.ib_share})
    void handleClick(View view) {
        switch (view.getId()) {
            case R.id.ib_bookmark:
                adapter.delegate.bookmark(adapter.getItem(getAdapterPosition()));
                break;
            case R.id.ib_share:
                adapter.delegate.shareItem(adapter.getItem(getAdapterPosition()));
                break;
            default:
                throw new AssertionError("unknown view");
        }
    }
}
