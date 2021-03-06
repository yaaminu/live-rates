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
import android.widget.Toast;

import com.backup.BackupException;
import com.backup.DependencyInjector;
import com.backup.Operation;
import com.zealous.R;
import com.zealous.Zealous;
import com.zealous.utils.GenericUtils;

import java.math.BigDecimal;
import java.math.MathContext;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by yaaminu on 4/15/17.
 */

public class AddNewCategoryDialogFragment extends BottomSheetDialogFragment {
    public static final String CATEGORY_NAME = "name", CATEGORY_BUDGET = "budget",
            CATEGORY_BUDGET_TYPE = "budgetType";
    @Nullable
    public String originalName;
    @BindView(R.id.et_category_name)
    EditText categoryName;
    @BindView(R.id.et_budget)
    EditText budget;
    @BindView(R.id.sp_budget_type)
    Spinner budgetType;
    @BindView(R.id.bt_add)
    Button btAdd;
    ExpenditureDataSource dataSource;

    DependencyInjector injector = new DependencyInjector() {
        @Override
        public void inject(Operation operation) {
            if (operation instanceof BaseExpenditureOperation) {
                ((BaseExpenditureOperation) operation).dataSource
                        = dataSource;
            }
        }
    };
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_add_new_category, container, false);
        unbinder = ButterKnife.bind(this, v);
        BaseExpenditureProvider provider =
                new BaseExpenditureProvider(
                        ((Zealous) getActivity().getApplication()).getExpenseBackupManager());
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
        try {
            dataSource.addOrUpdateCategory(originalName, new ExpenditureCategory(name,
                    BigDecimal.valueOf(budget).multiply(BigDecimal.valueOf(100),
                            MathContext.DECIMAL128).longValue(), budgetType));
            getDialog().dismiss();
        } catch (BackupException e) {
            Toast.makeText(getDialog().getContext(), R.string.failed_to_add_category, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        dataSource.close();
        super.onDestroy();
    }
}

