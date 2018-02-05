package com.zealous.expense;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.BindView;


/**
 * Created by yaaminu on 4/17/17.
 */
public class BudgetViewHolder extends BaseAdapter.Holder {
    @BindView(R.id.category_name)
    TextView categoryName;
    @BindView(R.id.tv_budgeted)
    TextView budget;
    @BindView(R.id.tv_amount_spent)
    TextView expenditure;
    @BindView(R.id.tv_left)
    TextView budgetDeficit;
    @BindView(R.id.expense_meter)
    ProgressBar expenseMeter;

    public BudgetViewHolder(View v) {
        super(v);
    }
}
