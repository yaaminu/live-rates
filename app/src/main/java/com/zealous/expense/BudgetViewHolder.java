package com.zealous.expense;

import android.view.View;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/17/17.
 */
public class BudgetViewHolder extends BaseAdapter.Holder {
    @Bind(R.id.category_name)
    TextView categoryName;
    @Bind(R.id.budget)
    TextView budget;
    @Bind(R.id.expenditure)
    TextView expenditure;
    @Bind(R.id.deficit)
    TextView budgetDeficit;
    @Bind(R.id.duration_type)
    TextView durationType;

    public BudgetViewHolder(View v) {
        super(v);
    }
}
