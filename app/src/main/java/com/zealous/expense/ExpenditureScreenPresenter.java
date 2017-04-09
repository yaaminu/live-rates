package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.R;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.zealous.expense.Expenditure.FIELD_TIME;

/**
 * Created by yaaminu on 3/26/17.
 */

public class ExpenditureScreenPresenter extends BasePresenter<ExpenseListScreen> {

    public static final int RANGE_TODAY = 0, RANGE_THIS_WEEK = 1,
            RANGE_THIS_MONTH = 2, RANGE_THIS_YEAR = 3, RANGE_CUSTOM = 4,
            RANGE_INVALID = -1;
    private static final String TAG = "ExpenditureScreenPresenter";
    private final ExpenditureDataSource expenditureDataSource;
    @NonNull
    private NonNullTuple<Long, Long> range;
    private int rangePos = RANGE_INVALID;
    @Nullable
    private ExpenseListScreen screen;
    private RealmResults<Expenditure> records;
    private final RealmChangeListener<RealmResults<Expenditure>> listener = new RealmChangeListener<RealmResults<Expenditure>>() {
        @Override
        public void onChange(RealmResults<Expenditure> element) {
            assert screen != null;
            screen.showExpenses(records);
        }
    };

    @Inject
    public ExpenditureScreenPresenter(@NonNull ExpenditureDataSource
                                              expenditureDataSource) {
        this.expenditureDataSource = expenditureDataSource;
        range = getRange(RANGE_TODAY);
    }

    public static Expenditure createDummyExpenditure() {
        return new ExpenditureBuilder()
                .setAmountSpent(Math.abs(new Random().nextInt()))
                .setCategory(new ExpenditureCategory("category", Math.abs(new Random().nextLong())))
                .setDescription("spending")
                .setLocation("location")
                .setTime(System.currentTimeMillis())
                .createExpenditure();
    }

    @Override
    public void onCreate(@Nullable Bundle bundle, @NonNull ExpenseListScreen screen) {
        //this will create a circular reference but it's ok since we free it in onDestroy
        this.screen = screen;
        // TODO: 4/9/17 restore state if possible
    }

    @Override
    public void saveState(@NonNull Bundle bundle) {
        // TODO: 4/9/17 save state
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDestroy() {
        screen = null;
        this.expenditureDataSource.close();
    }

    @Override
    public void onStart() {
        GenericUtils.ensureNotNull(screen, "screen == null");
        assert screen != null;
        updateRecords();
        this.records.addChangeListener(listener);
        screen.showExpenses(records);
    }

    @Override
    public boolean onMenuItemClicked(int itemId) {
        switch (itemId) {
            case R.id.action_clear:
                expenditureDataSource.clear();
                return true;
            case R.id.action_add:
                expenditureDataSource.addOrUpdateExpenditure(createDummyExpenditure());
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
        updateRecords();
        if (screen != null) {
            screen.showExpenses(records);
        }
    }

    private void updateRecords() {
        // TODO: 4/9/17 optimize away unnecessary calls
        final RealmQuery<Expenditure> query = expenditureDataSource.makeQuery();
        query.greaterThanOrEqualTo(FIELD_TIME, range.first)
                .lessThan(FIELD_TIME, range.second);
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

}
