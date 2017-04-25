package com.zealous.news;

import android.view.View;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/25/17.
 */

public class NewsItemHolder extends BaseAdapter.Holder {
    @Bind(R.id.title)
    TextView title;

    public NewsItemHolder(View itemView) {
        super(itemView);
    }
}
