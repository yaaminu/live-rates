package com.zealous.expense;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.R;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;

import java.math.BigDecimal;

import javax.inject.Inject;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by yaaminu on 4/17/17.
 */

public class BudgetFragmentPresenter extends BasePresenter<BudgetScreen> {

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
}
