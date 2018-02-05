package com.zealous.adapter;

import android.view.View;
import android.widget.TextView;

import com.zealous.R;

import butterknife.BindView;


/**
 * Created by yaaminu on 12/20/16.
 */
public class SimpleListItemHolder extends BaseAdapter.Holder {
    @BindView(R.id.tv_first)
    public TextView first;
    @BindView(R.id.tv_second)
    public TextView second;

    public SimpleListItemHolder(View view) {
        super(view);
    }
}
