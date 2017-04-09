package com.zealous.expense;

import android.view.View;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/8/17.
 */
public class ExpenseItemHolder extends BaseAdapter.Holder {
    @Bind(R.id.expense_description)
    TextView expeditureDescription;

    public ExpenseItemHolder(View v) {
        super(v);
    }
}
