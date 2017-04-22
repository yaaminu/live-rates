package com.zealous.expense;

import android.view.View;
import android.widget.ProgressBar;
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
    @Bind(R.id.tv_budgeted)
    TextView budget;
    @Bind(R.id.tv_amount_spent)
    TextView expenditure;
    @Bind(R.id.tv_left)
    TextView budgetDeficit;
    @Bind(R.id.expense_meter)
    ProgressBar expenseMeter;

    public BudgetViewHolder(View v) {
        super(v);
    }
}
