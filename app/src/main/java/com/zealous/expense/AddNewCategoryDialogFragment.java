package com.zealous.expense;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.zealous.R;
import com.zealous.utils.GenericUtils;

import java.math.BigDecimal;
import java.math.MathContext;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/15/17.
 */

public class AddNewCategoryDialogFragment extends BottomSheetDialogFragment {
    public static final String CATEGORY_NAME = "name", CATEGORY_BUDGET = "budget",
            CATEGORY_BUDGET_TYPE = "budgetType";
    @Nullable
    public String originalName;
    @Bind(R.id.et_category_name)
    EditText categoryName;
    @Bind(R.id.et_budget)
    EditText budget;
    @Bind(R.id.sp_budget_type)
    Spinner budgetType;
    @Bind(R.id.bt_add)
    Button btAdd;
    ExpenditureDataSource dataSource;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_add_new_category, container, false);
        ButterKnife.bind(this, v);
        BaseExpenditureProvider provider = new BaseExpenditureProvider();
        dataSource = provider.createDataSource(provider.getExpenditureRealm(provider.getConfiguration()));
        setCancelable(true);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            budget.setText(args.getString(CATEGORY_BUDGET));
            budget.setSelection(budget.getText().length());
            budgetType.setSelection(args.getInt(CATEGORY_BUDGET_TYPE, 2));
            categoryName.setText(args.getString(CATEGORY_NAME));
            categoryName.setSelection(categoryName.getText().length());
            originalName = args.getString(CATEGORY_NAME);
            GenericUtils.ensureNotNull(originalName);
            btAdd.setText(R.string.update);
        }
    }

    @OnClick(R.id.bt_add)
    void add() {
        //validate
        String name = categoryName.getText().toString().trim();
        String budget = this.budget.getText().toString();
        int budgetType = this.budgetType.getSelectedItemPosition();
        if (name.length() <= 2) {
            categoryName.setError(getString(R.string.name_too_short));
        }
        double actualBudget = 0;
        try {
            actualBudget = Math.abs(Double.parseDouble(budget.replaceAll("[^\\d\\.]+", "")));
            if (actualBudget == 0) {
                warnBudgetAndContinue(name, budgetType);
            } else {
                doAddNewCategory(name, actualBudget, budgetType);
            }
        } catch (NumberFormatException e) {
            warnBudgetAndContinue(name, budgetType);
        }
    }

    private void warnBudgetAndContinue(final String name, final int budgetType) {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.no_budget_warning)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doAddNewCategory(name, 0.0, budgetType);
                    }
                }).setNegativeButton(android.R.string.no, null)
                .create().show();
    }

    private void doAddNewCategory(String name, double budget, int budgetType) {
        dataSource.addOrUpdateCategory(originalName, new ExpenditureCategory(name,
                BigDecimal.valueOf(budget).multiply(BigDecimal.valueOf(100), MathContext.DECIMAL128).longValue(), budgetType));
        getDialog().dismiss();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        dataSource.close();
        super.onDestroy();
    }
}

