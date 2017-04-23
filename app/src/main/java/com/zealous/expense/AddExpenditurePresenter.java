package com.zealous.expense;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.zealous.utils.Config;
import com.zealous.utils.FileUtils;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by yaaminu on 4/14/17.
 */

public class AddExpenditurePresenter extends BasePresenter<AddExpenseFragment> {
    private static final String TAG = "AddExpenditurePresenter";
    public static final String KEY_OUTPUT_URI = "key_output_uri";
    private final ExpenditureDataSource dataSource;
    private AddExpenseFragment screen;
    private String location;
    private long time;
    private String currency, amount;
    private RealmResults<ExpenditureCategory> categories;
    private String expenditureCategoryName;

    @Nullable
    private Uri cameraOutputUri;

    @NonNull
    private List<Attachment> attachments;

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
        this.location = "";
        this.time = System.currentTimeMillis();
        currency = "GH₵";
        amount = "0.00";
        expenditureCategoryName = "";
        description = "";
        this.attachments = new ArrayList<>(1);
    }

    @Override
    public void onCreate(@Nullable Bundle savedState, @NonNull AddExpenseFragment screen) {
        this.screen = screen;
        updateState();
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: 4/15/17 use proper location
        updateUI();
        dataSource.listenForChanges(realmRealmChangeListener);
    }

    private void updateState() {
        if (!GenericUtils.isEmpty(expenditureID)) {
            Expenditure expenditure = dataSource.makeQuery().equalTo(Expenditure.FIELD_ID, expenditureID).findFirst();
            if (expenditure != null) {
                amount = expenditure.getNormalizedAmount();
                location = expenditure.getLocation();
                time = expenditure.getExpenditureTime().getTime();
                expenditureCategoryName = expenditure.getCategory().getName();
                description = expenditure.getDescription();
                List<Attachment> tmp = expenditure.getAttachments();
                for (Attachment attachment : tmp) {
                    if (!attachments.contains(attachment)) {
                        attachments.add(attachment);
                    }
                }

                Map<String, ?> state = getSavedState(screen.getContext());
                String s = (String) state.get(KEY_OUTPUT_URI);
                if (!GenericUtils.isEmpty(s)) {
                    cameraOutputUri = Uri.parse(s);
                }
                saveState(screen.getCurrentActivity(), Collections.singletonMap(KEY_OUTPUT_URI, ""));
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataSource.stopListeningForChanges(realmRealmChangeListener);
        if (cameraOutputUri != null) {
            saveState(screen.getCurrentActivity(),
                    Collections.singletonMap(KEY_OUTPUT_URI, cameraOutputUri.toString()));
        }
    }

    @Nullable
    public Uri getCameraOutputUri() {
        return cameraOutputUri;
    }

    private void updateUI() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                categories = dataSource.makeExpenditureCategoryQuery()
                        .findAllSorted(ExpenditureCategory.FIELD_NAME);
                AddExpenditurePresenter.this.screen.refreshDisplay(categories, time, location,
                        currency, amount, expenditureCategoryName, description, attachments);
            }
        };

        if (ThreadUtils.isMainThread()) {
            task.run();
        } else {
            TaskManager.executeOnMainThread(task);
        }
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
            description = "";
        }
        try {
            double tmp = Double.parseDouble(amount);
            long actualAmount = BigDecimal.valueOf(tmp).multiply(BigDecimal.valueOf(100), MathContext.DECIMAL128).longValue();
            if (actualAmount <= 0) {
                screen.showValidationError(GenericUtils.getString(R.string.invalid_amount));
                return false;
            } else {
                return doFinallyAdd(description, selectedItemPosition, actualAmount);
            }
        } catch (NumberFormatException e) {
            screen.showValidationError(GenericUtils.getString(R.string.invalid_amount));
            return false;
        }
    }

    private boolean doFinallyAdd(@NonNull final String description,
                                 final int selectedItemPosition, final long actualAmount) {
        final ExpenditureCategory category = categories.get(selectedItemPosition);
        final Expenditure expenditure = new ExpenditureBuilder()
                .setLocation(location).setTime(time)
                .setAmountSpent(actualAmount)
                .setCategory(category)
                .setDescription(description).createExpenditure();
        if (!GenericUtils.isEmpty(expenditureID)) {
            expenditure.setId(expenditureID);
        }
        for (Attachment attachment : attachments) {
            try {
                expenditure.addAttachment(attachment);
            } catch (ZealousException e) {
                PLog.e(TAG, e.getMessage(), e);
                screen.showValidationError(e.getMessage());
                return false;
            }
        }
        dataSource.addOrUpdateExpenditure(expenditure);
        return true;
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
        if (!GenericUtils.isEmpty(location)) {
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
        updateState();
    }

    public synchronized void addAttachment(@NonNull String path) {
        File file = new File(path);
        if (!file.exists()) {
            screen.showValidationError(screen.getCurrentActivity().getString(R.string.path_not_found, path));
        } else {
            try {
                attachments.add(
                        new Attachment(file.getName(),
                                org.apache.commons.io.FileUtils.readFileToByteArray(file),
                                FileUtils.getMimeType(file.getAbsolutePath())));
                updateUI();
            } catch (IOException | ZealousException e) {
                screen.showValidationError(e.getMessage());
            }
        }
    }

    public void setCameraOutputUri(@Nullable Uri uri) {
        cameraOutputUri = uri;
    }

    public void viewAttachment(final Attachment item) {
        screen.showProgressDialog(false);
        final String sha1Sum = item.getSha1Sum();
        final byte[] buffer = item.getBlob();
        final String mimeType = item.getMimeType();
        TaskManager.executeNow(new Runnable() {
            @Override
            public void run() {
                File dest = new File(Config.getTempDir(), sha1Sum);
                if (!dest.exists()) {
                    try {
                        org.apache.commons.io.FileUtils.writeByteArrayToFile(dest, buffer);
                    } catch (IOException e) {
                        screen.showValidationError(e.getMessage());
                        return;
                    }
                }
                doView(dest, mimeType);
                screen.dismissProgressDialog();
            }
        }, false);
    }

    private void doView(final File data, final String mimeType) {
        TaskManager.executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(data), mimeType);
                try {
                    screen.getCurrentActivity().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    screen.showValidationError(R.string.no_app_for_viewing_pdf_files);
                }
            }
        });
    }

    public void removeAttachment(Attachment item) {
        if (attachments.remove(item)) {
            File cachedFile = new File(Config.getTempDir(), item.getSha1Sum());
            if (cachedFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                if (cachedFile.delete()) {
                    PLog.d(TAG, "removed disk cache for attachment: %s", item);
                } else {
                    PLog.w(TAG, "failed to remove disk cache for %s", item);
                }
            }
            updateUI();
        }
    }
}
