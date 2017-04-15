package com.zealous.expense;

import com.zealous.R;
import com.zealous.ui.BaseZealousActivity;

/**
 * Created by yaaminu on 4/14/17.
 */

public class AddExpenditureActivity extends BaseZealousActivity {

    @Override
    protected int getLayout() {
        return R.layout.actvity_add_expense;
    }

    @Override
    protected boolean hasParent() {
        return true;
    }
}
