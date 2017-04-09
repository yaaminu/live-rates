package com.zealous.expense;

import com.zealous.ui.Screen;

import java.util.List;

/**
 * Created by yaaminu on 4/8/17.
 */

public interface ExpenseListScreen extends Screen {
    void showExpenses(List<Expenditure> expenditures);
}
