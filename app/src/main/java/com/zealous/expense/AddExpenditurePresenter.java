package com.zealous.expense;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.zealous.R;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.TaskManager;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.zealous.utils.GenericUtils.getString;

/**
 * Created by yaaminu on 4/14/17.
 */

public class AddExpenditurePresenter extends BasePresenter<AddExpenseFragment> {
    private final ExpenditureDataSource dataSource;
    private AddExpenseFragment screen;
    private String location;
    private long time;
    private String currency;
    private RealmResults<ExpenditureCategory> categories;
    private final RealmChangeListener<Realm> realmRealmChangeListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm element) {
            updateUI();
        }
    };

    @Inject

    public AddExpenditurePresenter(ExpenditureDataSource dataSource) {
        this.dataSource = dataSource;
        this.location = getString(R.string.unknown);
        this.time = System.currentTimeMillis();
        currency = "GH₵";
    }

    @Override
    public void onCreate(@Nullable Bundle savedState, @NonNull AddExpenseFragment screen) {
        this.screen = screen;
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: 4/15/17 use proper location
        updateUI();
        dataSource.listenForChanges(realmRealmChangeListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        dataSource.stopListeningForChanges(realmRealmChangeListener);
    }

    private void updateUI() {
        categories = dataSource.makeExpenditureCategoryQuery()
                .findAllSorted(ExpenditureCategory.FIELD_NAME);
        this.screen.refreshDisplay(categories, time, location, currency);
    }

    @Override
    public void onDestroy() {
        this.screen = null;
        this.dataSource.close();
    }

    public boolean onAddExpenditure(@NonNull String amount, @NonNull String description, int selectedItemPosition) {
        if (selectedItemPosition < 0 || selectedItemPosition >= categories.size()) {
            screen.showValidationError(GenericUtils.getString(R.string.no_category_error));
            return false;
        }
        if (GenericUtils.isEmpty(description)) {
            description = GenericUtils.getString(R.string.no_description);
        }
        try {
            double tmp = Double.parseDouble(amount);
            long actualAmount = BigDecimal.valueOf(tmp).multiply(BigDecimal.valueOf(100), MathContext.DECIMAL128).longValue();
            dataSource.addOrUpdateExpenditure(new ExpenditureBuilder()
                    .setLocation(location).setTime(time)
                    .setAmountSpent(actualAmount)
                    .setCategory(categories.get(selectedItemPosition))
                    .setDescription(description).createExpenditure());
            return true;
        } catch (NumberFormatException e) {
            screen.showValidationError(GenericUtils.getString(R.string.invalid_amount));
            return false;
        }
    }

    public void onAddCustomCategory(FragmentManager fm) {
//        DialogFragment dialogFragment = new Dial
        DialogFragment fragment = new AddNewCategoryDialogFragment();
        fragment.show(fm, "addCategory");
    }

    public void editDate(FragmentManager fm) {
        final EditDateFragment fragment = new EditDateFragment();
        Bundle bundle = new Bundle(1);
        bundle.putLong(EditDateFragment.DATE, time);
        fragment.setArguments(bundle);
        fragment.show(fm, "editDate");
        TaskManager.executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                fragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Date date = fragment.getTime();
                        if (date != null) {
                            if (date.getTime() > System.currentTimeMillis()) {
                                Toast.makeText(fragment.getContext(), R.string.date_in_future, Toast.LENGTH_SHORT).show();
                            } else {
                                AddExpenditurePresenter.this.time = date.getTime();
                                updateUI();
                            }
                        }
                    }
                });
            }
        });
    }
}