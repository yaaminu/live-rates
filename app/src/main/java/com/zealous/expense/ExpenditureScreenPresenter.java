package com.zealous.expense;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.zealous.R;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.realm.Case;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.zealous.exchangeRates.ExchangeRate.FORMAT;
import static com.zealous.expense.Expenditure.FIELD_TIME;

/**
 * Created by yaaminu on 3/26/17.
 */

public class ExpenditureScreenPresenter extends BasePresenter<ExpenseListScreen> {

    public static final int RANGE_TODAY = 0;
    public static final int RANGE_THIS_WEEK = 1;
    public static final int RANGE_THIS_MONTH = 2;
    public static final int RANGE_THIS_YEAR = 3;
    public static final int RANGE_CUSTOM = 4;
    public static final int RANGE_INVALID = -1;
    public static final String STATE_RANGE_POS = "state_range_pos";
    private static final String TAG = "ExpenditureScreenPresenter";
    private final ExpenditureDataSource expenditureDataSource;
    private final String[] expenseRange;
    private NonNullTuple<Long, Long> range;
    private int rangePos = RANGE_INVALID;
    @Nullable
    private ExpenseListScreen screen;
    private RealmResults<Expenditure> records;
    private double totalBudget;
    private double totalExpenditure;
    private final RealmChangeListener<RealmResults<Expenditure>> listener = new RealmChangeListener<RealmResults<Expenditure>>() {
        @Override
        public void onChange(RealmResults<Expenditure> element) {
            totalBudget = calculateTotalBudget().divide(BigDecimal.valueOf(100), MathContext.DECIMAL128).doubleValue();
            totalExpenditure = BigDecimal.valueOf(records.sum(Expenditure.FIELD_AMOUNT).longValue()).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128).longValue();
            updateUi();
        }
    };

    @Inject
    public ExpenditureScreenPresenter(@NonNull ExpenditureDataSource
                                              expenditureDataSource, String[] expenseRange) {
        this.expenditureDataSource = expenditureDataSource;
        this.expenseRange = expenseRange;
    }

    private BigDecimal calculateTotalBudget() {
        BigDecimal tmp;

        Calendar calendar = Calendar.getInstance();

        tmp = BigDecimal.valueOf(expenditureDataSource.makeExpenditureCategoryQuery()
                .equalTo(ExpenditureCategory.FIELD_BUDGET_DURATION, ExpenditureCategory.DAILY)
                .sum(ExpenditureCategory.FIELD_BUDGET).longValue());

        tmp = tmp.add(BigDecimal.valueOf(expenditureDataSource.makeExpenditureCategoryQuery()
                .equalTo(ExpenditureCategory.FIELD_BUDGET_DURATION, ExpenditureCategory.WEEKLY)
                .sum(ExpenditureCategory.FIELD_BUDGET).longValue())
                .divide(BigDecimal.valueOf(7), MathContext.DECIMAL128));

        tmp = tmp.add(BigDecimal.valueOf(expenditureDataSource.makeExpenditureCategoryQuery()
                .equalTo(ExpenditureCategory.FIELD_BUDGET_DURATION, ExpenditureCategory.MONTHLY)
                .sum(ExpenditureCategory.FIELD_BUDGET).longValue())
                .divide(BigDecimal.valueOf(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)), MathContext.DECIMAL128));

        tmp = tmp.add(BigDecimal.valueOf(expenditureDataSource.makeExpenditureCategoryQuery()
                .equalTo(ExpenditureCategory.FIELD_BUDGET_DURATION, ExpenditureCategory.YEARLY)
                .sum(ExpenditureCategory.FIELD_BUDGET).longValue())
                .divide(BigDecimal.valueOf(calendar.getActualMaximum(Calendar.DAY_OF_YEAR)), MathContext.DECIMAL128));

        return tmp.multiply(BigDecimal.valueOf(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)), MathContext.DECIMAL128);
    }

    @Override
    public void onCreate(@Nullable Bundle bundle, @NonNull ExpenseListScreen screen) {
        //this will create a circular reference but it's ok since we free it in onDestroy
        this.screen = screen;
        final Map<String, ?> savedState = getSavedState(screen.getCurrentActivity());
        Object tmp = savedState.get(STATE_RANGE_POS);
        int tmpRange = tmp != null ? ((Integer) tmp) : RANGE_TODAY;
        range = getRange(tmpRange);
        rangePos = tmpRange;
    }

    @Override
    public void onDestroy() {
        assert screen != null;
        saveState(screen.getCurrentActivity(), Collections.singletonMap(STATE_RANGE_POS, rangePos));
        screen = null;
        this.expenditureDataSource.close();
    }

    @Override
    public void onStart() {
        GenericUtils.ensureNotNull(screen, "screen == null");
        assert screen != null;
        updateRecords("");
        this.records.addChangeListener(listener);
        updateUi();
    }

    @Override
    public boolean onMenuItemClicked(int itemId) {
        switch (itemId) {
            case R.id.action_view_budget:
                assert screen != null;
                final Activity currentActivity = screen.getCurrentActivity();
                currentActivity.startActivity(new Intent(currentActivity, BudgetActivity.class));
                return true;

        }
        return super.onMenuItemClicked(itemId);
    }

    @Override
    public void onStop() {
        this.records.removeAllChangeListeners();
    }

    public void onChangeExpenditureRange(int position) {
        range = getRange(position);
        updateRecords("");
        updateUi();
    }

    private void updateUi() {
        // TODO: 4/13/17 optimize away unnecessary calls
        if (screen != null) {
            screen.refreshDisplay(records, FORMAT.format(totalExpenditure), FORMAT.format(totalBudget), expenseRange[rangePos]);
        }
    }

    private void updateRecords(String filter) {
        // TODO: 4/9/17 optimize away unnecessary calls
        final RealmQuery<Expenditure> query = expenditureDataSource.makeQuery();
        query.greaterThanOrEqualTo(FIELD_TIME, range.first)
                .lessThan(FIELD_TIME, range.second)
                .beginGroup()
                .contains(Expenditure.FIELD_DESCRIPTION, filter, Case.INSENSITIVE)
                .or()
                .contains(Expenditure.FIELD_LOCATION, filter, Case.INSENSITIVE)
                .endGroup();
        records = query.findAllSortedAsync(FIELD_TIME, Sort.DESCENDING);
        records.addChangeListener(listener);
    }

    public NonNullTuple<Long, Long> getRange(int type) {
        if (rangePos == type) { //not changed
            PLog.d(TAG, "range position did not change returning cached version");
            return range;
        }
        rangePos = type;
        NonNullTuple<Long, Long> range;
        switch (type) {
            case RANGE_CUSTOM:
                PLog.w(TAG, "custom range not supported");
                //fall through
            case RANGE_TODAY:
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long lowerBound = calendar.getTimeInMillis();
                range = new NonNullTuple<>(lowerBound, calendar.getTimeInMillis() + (TimeUnit.HOURS.toMillis(24L) - 1));
                break;
            case RANGE_THIS_WEEK:
                calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                lowerBound = calendar.getTimeInMillis();

                calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
                calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);

                range = new NonNullTuple<>(lowerBound, calendar.getTimeInMillis());
                break;
            case RANGE_THIS_MONTH:
                calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                lowerBound = calendar.getTimeInMillis();

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                range = new NonNullTuple<>(lowerBound, calendar.getTimeInMillis());
                break;
            case RANGE_THIS_YEAR:
                calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                lowerBound = calendar.getTimeInMillis();

                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                range = new NonNullTuple<>(lowerBound, calendar.getTimeInMillis());
                break;
            default:
                throw new RuntimeException();
        }
        return range;
    }

    public void deleteItem(Context context, Expenditure expenditure) {
        if (expenditureDataSource.removeExpenditure(expenditure.getId())) {
            Toast.makeText(context, R.string.expenditure_removed_success, Toast.LENGTH_LONG).show();
        }
    }

    public void editItem(Context context, @Nullable Expenditure expenditure) {
        Intent intent = new Intent(context, AddExpenditureActivity.class);
        if (expenditure != null) {
            intent.putExtra(AddExpenditureActivity.EXTRA_EXPENDITURE_ID, expenditure.getId());
        }
        context.startActivity(intent);
    }

    public void onAddNewExpenditure(Context context) {
        editItem(context, null);
    }

    public String[] getRangNames() {
        return expenseRange;
    }

    public void search(String filter) {
        updateRecords(filter);
    }
}
