package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnItemSelected;

/**
 * Created by yaaminu on 4/8/17.
 */

public class ExpenseFragment extends BaseFragment implements ExpenseListScreen {

    @Inject
    ExpenditureScreenPresenter expenditureScreenPresenter;
    @Inject
    ExpenseAdapter adapter;
    @Inject
    ExpenseAdapterDelegateImpl delegate;
    @Inject
    RecyclerView.LayoutManager layoutManager;

    @Bind(R.id.recycler_view)
    RecyclerView expenseList;

    @Bind(R.id.total_expenditure)
    TextView totalExpenditure;
    @Bind(R.id.monthly_budget)
    TextView totalBudget;

    @Override
    protected int getLayout() {
        return R.layout.fragment_expenses;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DaggerExpenseFragmentComponent
                .builder()
                .expenseFragmentProvider(new ExpenseFragmentProvider(this))
                .build()
                .inject(this);
        expenditureScreenPresenter.onCreate(savedInstanceState, this);
    }

    @OnItemSelected(R.id.expenditure_range)
    void onItemSelected(int position) {
        expenditureScreenPresenter.onChangeExpenditureRange(position);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expenseList.setLayoutManager(layoutManager);
        expenseList.setAdapter(adapter);
    }

    @Nullable
    @Override
    protected BasePresenter<?> getBasePresenter() {
        return expenditureScreenPresenter;
    }

    @Override
    public void refreshDisplay(@NonNull List<Expenditure> expenditures, String totalExpenditure, String totalBudget) {
        GenericUtils.ensureNotNull(expenditures);
        delegate.refreshDataSet(expenditures, adapter);
        this.totalBudget.setText(getString(R.string.total_budget, totalBudget));
        this.totalExpenditure.setText(getString(R.string.total_expenditire, totalExpenditure));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return expenditureScreenPresenter.onMenuItemClicked(item.getItemId())
                || super.onOptionsItemSelected(item);
    }
}
