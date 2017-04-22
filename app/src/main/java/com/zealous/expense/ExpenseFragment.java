package com.zealous.expense;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRateListActivity;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * @author by yaaminu on 4/8/17.
 */

public class ExpenseFragment extends BaseFragment implements ExpenseListScreen {

    private static final String TAG = "ExpenseFragment";

    @Inject
    ExpenditureScreenPresenter expenditureScreenPresenter;
    @Inject
    ExpenseAdapter adapter;
    @Inject
    ExpenseAdapterDelegateImpl delegate;
    @Inject
    RecyclerView.LayoutManager layoutManager;

    @Inject
    EventBus eventBus;

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
    @Bind(R.id.year)
    TextView year;

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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        PLog.d(TAG, "received event %s", event);
        if (event instanceof Map) {
            String value = ((Map<String, String>) event).get(ExchangeRateListActivity.SEARCH);
            if (value != null) {
                expenditureScreenPresenter.search(value);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expense_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expenseList.setLayoutManager(layoutManager);
        expenseList.setAdapter(adapter);
        year.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        todaysDate.setText(DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
    }

    @OnClick(R.id.expenditure_range)
    void changeRange() {
        new AlertDialog.Builder(getContext())
                .setItems(expenditureScreenPresenter.getRangNames(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expenditureScreenPresenter.onChangeExpenditureRange(which);
                    }
                }).create().show();
    }

    @OnClick(R.id.fab)
    void addExpenditure() {
        expenditureScreenPresenter.onAddNewExpenditure(getContext());
    }

    @Nullable
    @Override
    protected BasePresenter<?> getBasePresenter() {
        return expenditureScreenPresenter;
    }

    @Override
    public void refreshDisplay(@NonNull List<Expenditure> expenditures,
                               String totalExpenditure, String totalBudget, String rangeName) {
        GenericUtils.ensureNotNull(expenditures);
        delegate.refreshDataSet(expenditures, adapter);
        this.totalBudget.setText(getString(R.string.total_budget, totalBudget));
        this.totalExpenditure.setText(getString(R.string.total_expenditire, totalExpenditure));
        rangeText.setText(rangeName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return expenditureScreenPresenter.onMenuItemClicked(item.getItemId())
                || super.onOptionsItemSelected(item);
    }

    @Override
    public Activity getCurrentActivity() {
        return getActivity();
    }
}
