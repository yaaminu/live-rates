package com.zealous.expense;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.backup.BackupException;
import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.inject.Inject;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by yaaminu on 4/17/17.
 */

public class BudgetFragmentPresenter extends BasePresenter<BudgetScreen> {

    private static final String TAG = "BudgetFragmentPresenter";
    private final ExpenditureDataSource dataSource;
    private RealmResults<ExpenditureCategory> budget;
    private BudgetScreen screen;

    private final RealmChangeListener<RealmResults<ExpenditureCategory>> realmRealmChangeListener =
            new RealmChangeListener<RealmResults<ExpenditureCategory>>() {
                @Override
                public void onChange(RealmResults<ExpenditureCategory> element) {
                    refreshDisplay();
                }
            };

    @Inject
    public BudgetFragmentPresenter(ExpenditureDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void onCreate(@Nullable Bundle savedState, @NonNull BudgetScreen screen) {
        GenericUtils.ensureNotNull(screen);
        this.screen = screen;
    }

    @Override
    public void onStart() {
        super.onStart();
        budget = dataSource.makeExpenditureCategoryQuery().findAllSortedAsync(ExpenditureCategory.FIELD_NAME);
        budget.addChangeListener(realmRealmChangeListener);
        refreshDisplay();
    }

    @Override
    public void onStop() {
        budget.removeChangeListener(realmRealmChangeListener);
        super.onStop();
    }

    private void refreshDisplay() {
        if (this.screen != null) {
            this.screen.refreshDisplay(budget);
        }
    }

    @Override
    public void onDestroy() {
        dataSource.close();
    }

    public BigDecimal getTotalExpenditure(ExpenditureCategory category) {
        // TODO: 4/17/17 optimize
        return category.getExpenditure(dataSource);
    }

    public String[] getDurations(Context context) {
        return context.getResources().getStringArray(R.array.duration_types);
    }

    void onAddCategory(FragmentManager fragmentManager, ExpenditureCategory category) {
        DialogFragment fragment = new AddNewCategoryDialogFragment();
        if (category != null) {
            Bundle bundle = new Bundle(3);
            bundle.putString(AddNewCategoryDialogFragment.CATEGORY_NAME, category.getName());
            bundle.putString(AddNewCategoryDialogFragment.CATEGORY_BUDGET,
                    ExchangeRate.FORMAT.format(BigDecimal.valueOf(category.getBudget())
                            .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128).longValue()));
            bundle.putInt(AddNewCategoryDialogFragment.CATEGORY_BUDGET_TYPE, category.getBudgetDuration());
            fragment.setArguments(bundle);
        }
        fragment.show(fragmentManager, "addCategory");
    }

    public void removeCategory(ExpenditureCategory category) {
        try {
            dataSource.removeCategory(category);
        } catch (ZealousException e) {
            PLog.d(TAG, e.getMessage(), e);
            screen.showValidationError(e.getMessage());
        } catch (BackupException e) {
            PLog.f(TAG, e.getMessage(), e);
            screen.showValidationError(GenericUtils.getString(R.string.failed_to_delete));
        }
    }
}
