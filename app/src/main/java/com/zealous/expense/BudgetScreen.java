package com.zealous.expense;

import com.zealous.ui.Screen;

import java.util.List;

/**
 * Created by yaaminu on 4/17/17.
 */
public interface BudgetScreen extends Screen {
    void refreshDisplay(List<ExpenditureCategory> budget);
}
