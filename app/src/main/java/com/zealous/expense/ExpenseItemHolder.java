package com.zealous.expense;

import android.view.View;
import android.widget.ImageView;
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
    @Bind(R.id.tv_location)
    TextView expenseLocation;
    @Bind(R.id.category_icon)
    ImageView categoryIcon;
    @Bind(R.id.tv_expense_amount)
    TextView expenditureAmount;
    @Bind(R.id.expenditure_time)
    TextView expenditureTime;


    public ExpenseItemHolder(View v) {
        super(v);
    }
}
