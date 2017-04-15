package com.zealous.expense;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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
import butterknife.OnClick;

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
    @Bind(R.id.expenditure_range_text)
    TextView rangeText;
    @Bind(R.id.today_s_date)
    TextView todaysDate;

    private String[] expenseRange;

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
        expenseRange = getResources().getStringArray(R.array.expense_range);
        expenditureScreenPresenter.onCreate(savedInstanceState, this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expenseList.setLayoutManager(layoutManager);
        expenseList.setAdapter(adapter);
        rangeText.setText(expenseRange[0]);
        todaysDate.setText(DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
    }

    @OnClick(R.id.expenditure_range)
    void changeRange() {
        new AlertDialog.Builder(getContext())
                .setItems(expenseRange, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rangeText.setText(expenseRange[which]);
                        expenditureScreenPresenter.onChangeExpenditureRange(which);
                    }
                }).create().show();
    }

    @OnClick(R.id.options)
    void onMenuClicked(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.inflate(R.menu.expense_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return expenditureScreenPresenter.onMenuItemClicked(item.getItemId());
            }
        });
        popupMenu.show();
    }


    @OnClick(R.id.fab)
    void addExpenditure() {
        Intent intent = new Intent(getContext(), AddExpenditureActivity.class);
        startActivity(intent);
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
