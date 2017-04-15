package com.zealous.expense;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.exchangeRates.ExchangeRate;
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
    private String currency, amount;
    private RealmResults<ExpenditureCategory> categories;
    private String expenditureCategoryName;

    @Nullable
    private String expenditureID;
    @Nullable
    private String description;


    private final RealmChangeListener<Realm> realmRealmChangeListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm element) {
            updateUI();
        }
    };

    @Inject
    public AddExpenditurePresenter(ExpenditureDataSource dataSource) {
        this.dataSource = dataSource;
        this.location = getString(R.string.unspecified_location);
        this.time = System.currentTimeMillis();
        currency = "GH₵";
        amount = "0.00";
        expenditureCategoryName = "";
        description = "";
    }

    @Override
    public void onCreate(@Nullable Bundle savedState, @NonNull AddExpenseFragment screen) {
        this.screen = screen;
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: 4/15/17 use proper location
        if (!GenericUtils.isEmpty(expenditureID)) {
            Expenditure expenditure = dataSource.makeQuery().equalTo(Expenditure.FIELD_ID, expenditureID).findFirst();
            if (expenditure != null) {
                amount = expenditure.getNormalizedAmount();
                location = expenditure.getLocation();
                time = expenditure.getExpenditureTime().getTime();
                expenditureCategoryName = expenditure.getCategory().getName();
                description = expenditure.getDescription();
            }
        }
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
        this.screen.refreshDisplay(categories, time, location,
                currency, amount, expenditureCategoryName, description);
    }

    public void updateData(String amount, String expenditureCategoryName, String description) {
        this.amount = amount;
        this.expenditureCategoryName = expenditureCategoryName;
        this.description = description;
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
            if (actualAmount <= 0) {
                screen.showValidationError(GenericUtils.getString(R.string.invalid_amount));
                return false;
            } else {
                final Expenditure expenditure = new ExpenditureBuilder()
                        .setLocation(location).setTime(time)
                        .setAmountSpent(actualAmount)
                        .setCategory(categories.get(selectedItemPosition))
                        .setDescription(description).createExpenditure();
                if (!GenericUtils.isEmpty(expenditureID)) {
                    expenditure.setId(expenditureID);
                }
                dataSource.addOrUpdateExpenditure(expenditure);
                return true;
            }
        } catch (NumberFormatException e) {
            screen.showValidationError(GenericUtils.getString(R.string.invalid_amount));
            return false;
        }
    }

    public void onAddCustomCategory(@NonNull FragmentManager fm) {
        doAddCustomCategory(fm, null);
    }

    private void doAddCustomCategory(@NonNull FragmentManager fm, @Nullable ExpenditureCategory category) {
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

    public void editLocation(FragmentManager fm) {
        final EditLocationFragment fragment = new EditLocationFragment();
        if (!location.equals(getString(R.string.unspecified_location))) {
            Bundle bundle = new Bundle(1);
            bundle.putString(EditLocationFragment.LOCATION, location);
            fragment.setArguments(bundle);
        }
        fragment.show(fm, "editLocation");
        TaskManager.executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                fragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        String location = fragment.getLocation();
                        if (GenericUtils.isEmpty(location)) {
                            Toast.makeText(fragment.getContext(), R.string.no_location_provided, Toast.LENGTH_SHORT).show();
                        } else {
                            AddExpenditurePresenter.this.location = location;
                            updateUI();
                        }
                    }
                });
            }
        });
    }

    public void onCategoryContextMenuAction(FragmentManager manager, ExpenditureCategory category, int position) {
        switch (position) {
            case 0:
                doAddCustomCategory(manager, category);
                break;
            case 1:
                try {
                    dataSource.removeCategory(category);
                } catch (ZealousException e) {
                    screen.showValidationError(e.getMessage());
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    public void startWith(@NonNull String expenditureID) {
        this.expenditureID = expenditureID;
    }
}
