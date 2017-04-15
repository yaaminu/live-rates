package com.zealous.expense;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/15/17.
 */
public class CategoryHolder extends BaseAdapter.Holder {
    @Bind(R.id.tv_category_name)
    TextView name;
    @Bind(R.id.ib_category_icon)
    ImageButton categoryIcon;

    public CategoryHolder(View v) {
        super(v);
    }
}
