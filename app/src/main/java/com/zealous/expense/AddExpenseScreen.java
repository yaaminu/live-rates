package com.zealous.expense;

import com.zealous.ui.Screen;

import java.util.List;

/**
 * Created by yaaminu on 4/14/17.
 */

public interface AddExpenseScreen extends Screen {
    void refreshDisplay(List<ExpenditureCategory> categories, long time,
                        String location, String currency, String amount, String categoryName, String description);

    void showValidationError(String errorMessage);
}
