package com.zealous.adapter;

import android.view.View;
import android.widget.TextView;

import com.zealous.R;

import butterknife.Bind;

/**
 * Created by yaaminu on 12/20/16.
 */
public class SimpleListItemHolder extends BaseAdapter.Holder {
    @Bind(R.id.tv_first)
    TextView first;
    @Bind(R.id.tv_second)
    TextView second;

    public SimpleListItemHolder(View view) {
        super(view);
    }
}
