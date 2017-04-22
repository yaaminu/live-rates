package com.zealous.expense;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/17/17.
 */
public class BudgetFragment extends BaseFragment implements BudgetScreen {

    @Inject
    BudgetFragmentPresenter presenter;
    @Inject
    BudgetAdapter.Delegate delegate;
    @Inject
    BudgetAdapter adapter;
    @Inject
    RecyclerView.LayoutManager layoutManager;

    List<ExpenditureCategory> dataSet = Collections.emptyList();

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerBudgetFragmentComponent.builder()
                .budgetFragmentProvider(new BudgetFragmentProvider(this))
                .build().inject(this);
        presenter.onCreate(savedInstanceState, this);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_budget;
    }

    public String[] getDurationTypes() {
        return presenter.getDurations(getContext());
    }

    public List<ExpenditureCategory> dataSet() {
        return dataSet;
    }

    @Override
    public void refreshDisplay(List<ExpenditureCategory> budget) {
        this.dataSet = budget;
        adapter.notifyDataChanged("");
    }

    @Override
    public Activity getCurrentActivity() {
        return getActivity();
    }

    public BigDecimal getTotalExpenditure(ExpenditureCategory category) {
        return presenter.getTotalExpenditure(category);
    }

    @Nullable
    @Override
    protected BasePresenter<?> getBasePresenter() {
        return presenter;
    }

    @OnClick(R.id.fab)
    void onclick() {
        addCustomCategory(getFragmentManager(), null);
    }

    void addCustomCategory(@NonNull FragmentManager fm, @Nullable ExpenditureCategory category) {
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
        fragment.show(fm, "addCategory");
    }
}
