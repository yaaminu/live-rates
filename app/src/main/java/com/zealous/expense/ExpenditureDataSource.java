package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.backup.BackupException;
import com.backup.BackupManager;
import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.io.Closeable;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * <p>
 * A simple data source implemented on top of {@linkplain Realm}.
 * The well known single thread rule for realm instances apply so
 * you cannot use instances of this class across multiple threads
 * </p>
 * Created by yaaminu on 3/26/17.
 */

public class ExpenditureDataSource implements Closeable {

    private static final String TAG = "ExpenditureDataSource";
    public static final String GROUP_EXPENSES = "expenses";
    private final Realm realm;
    private final BackupManager backupManager;

    /**
     * creates a new data source backed by {@code realm}
     *
     * @param realm the realm, may not be null
     * @throws IllegalArgumentException if {@code realm} is null
     * @throws IllegalStateException    if {@code realm} is closed
     */
    @Inject
    public ExpenditureDataSource(@NonNull Realm realm,
                                 @Nullable BackupManager backupManager) {
        GenericUtils.ensureNotNull(realm);
        this.realm = realm;
        this.backupManager = backupManager;
        ensureNotClosed();
    }


    /**
     * add new {@code expenditure} if no expenditure with same
     * id exist otherwise it updates an existing one
     *
     * @param expenditure the expenditure to add may not be null
     * @throws IllegalStateException    if this data sources is closed
     * @throws IllegalStateException    if call is used on a different thread
     * @throws IllegalArgumentException if expenditure is null
     * @throws BackupException          if an error occurs while recording the operation
     */
    public void addOrUpdateExpenditure(@NonNull Expenditure expenditure) throws BackupException {
        ensureNotClosed();
        GenericUtils.ensureNotNull(expenditure);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(expenditure);
        try {
            if (backupManager != null) {
                backupManager.log(new AddOrUpdateExpenditureOperation(expenditure),
                        System.currentTimeMillis());
            }
        } catch (BackupException e) {
            realm.cancelTransaction();
            PLog.f(TAG, e.getMessage(), e);
            throw e;
        }
        realm.commitTransaction();
    }

//    /**
//     * add/updates all expenditures stored in the backing realm
//     *
//     * @param expenditures the expenditures to add may not be null
//     * @throws IllegalStateException    if this data sources is closed
//     * @throws IllegalStateException    if call is used on a different thread
//     * @throws IllegalArgumentException if expenditure is null
//     */
//    public void addOrUpdateAll(@NonNull Collection<Expenditure> expenditures) {
//        GenericUtils.ensureNotNull(expenditures);
//        realm.beginTransaction();
//        realm.copyToRealmOrUpdate(expenditures);
//        realm.commitTransaction();
//    }

    /**
     * removes an expenditure with {@code id = expenditureId}
     *
     * @param expenditureId the id of the expenditure to delete.
     *                      may not be null.
     * @return true if an expenditure with id of {@code expenditureId} was
     * actually removed otherwise false
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     * @throws BackupException       when backup error occurs
     */
    public boolean removeExpenditure(@NonNull String expenditureId) throws BackupException {
        GenericUtils.ensureNotEmpty(expenditureId);
        ensureNotClosed();
        realm.beginTransaction();
        Expenditure expenditure =
                realm.where(Expenditure.class).equalTo(Expenditure.FIELD_ID, expenditureId).findFirst();
        if (expenditure != null) {
            expenditure.deleteFromRealm();
        }
        if (backupManager != null) {
            try {
                backupManager.log(new RemoveExpenditureOperation(expenditureId),
                        System.currentTimeMillis());
            } catch (BackupException e) {
                PLog.f(TAG, e.getMessage(), e);
                realm.cancelTransaction();
                throw e;
            }
        }
        realm.commitTransaction();
        return expenditure != null;
    }

    /**
     * cleans the all records stored inside of the
     * backing
     * <p>
     *
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     * @throws BackupException       when backup error occurs
     */
    public void clear() throws BackupException {
        ensureNotClosed();
        realm.beginTransaction();
        realm.deleteAll();
        if (backupManager != null) {
            try {
                backupManager.log(
                        new ClearOperation(),
                        System.currentTimeMillis());
            } catch (BackupException e) {
                PLog.f(TAG, e.getMessage(), e);
                realm.cancelTransaction();
                throw e;
            }
        }
        realm.commitTransaction();
    }

    /**
     * registers a change listener to the underlying realm
     * <p>
     * Avoid using anonymous listeners as they stand a chance of being
     * garbage collected
     *
     * @param changeListener the listener, may not be null
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     */
    public void listenForChanges(@NonNull RealmChangeListener<Realm> changeListener) {
        ensureNotClosed();
        realm.addChangeListener(changeListener);
    }

    /**
     * stops listening for changes on the underlying realm
     *
     * @param changeListener the listener, may not be null
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     */
    public void stopListeningForChanges(@NonNull RealmChangeListener<Realm> changeListener) {
        ensureNotClosed();
        realm.removeChangeListener(changeListener);
    }

    /**
     * @return all categories for expenditures
     */
    public RealmResults<ExpenditureCategory> findCategories() {
        ensureNotClosed();
        return realm.where(ExpenditureCategory.class).findAllSorted(ExpenditureCategory.FIELD_NAME, Sort.ASCENDING);
    }

    public RealmQuery<ExpenditureCategory> makeExpenditureCategoryQuery() {
        ensureNotClosed();
        return realm.where(ExpenditureCategory.class);
    }

    /**
     * @return all expenditures in this realm
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     */
    public List<Expenditure> findAll() {
        ensureNotClosed();
        return realm.where(Expenditure.class).findAllSorted(Expenditure.FIELD_TIME, Sort.DESCENDING);
    }

    /**
     * @return a {@code RealmQuery} object that can be used
     * to retrieve records from this datasource
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     */
    public RealmQuery<Expenditure> makeQuery() {
        ensureNotClosed();
        return realm.where(Expenditure.class);
    }

    /**
     * closes this datasource freeing all resources.
     * It's important to remember that once closed, instances of this
     * datasource cannot be reused
     *
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     */
    @Override
    public void close() {
        realm.close();
    }

    private void ensureNotClosed() {
        GenericUtils.ensureConditionTrue(!realm.isClosed(), "can't use a closed datasource");
    }

    public void addOrUpdateCategory(@Nullable final String previousName, ExpenditureCategory category)
            throws BackupException {
        ensureNotClosed();
        realm.beginTransaction();
        category = realm.copyToRealmOrUpdate(category);
        if (!GenericUtils.isEmpty(previousName) && !category.getName().equals(previousName)) {
            //different categories, delete previous one to emulate overwriting it.
            RealmResults<Expenditure> matched = realm.where(Expenditure.class)
                    .equalTo(Expenditure.FIELD_CATEGORY + "." + ExpenditureCategory.FIELD_NAME, previousName).findAll();
            for (Expenditure expenditure : matched) {
                expenditure.setCategory(category);
            }
            ExpenditureCategory live = realm.where(ExpenditureCategory.class).equalTo(ExpenditureCategory.FIELD_NAME, previousName).findFirst();
            if (live != null) {
                live.deleteFromRealm();
            }
        }
        if (backupManager != null) {
            try {
                backupManager.log(
                        new AddOrUpdateCategoryOperation(previousName, category),
                        System.currentTimeMillis());
            } catch (BackupException e) {
                PLog.f(TAG, e.getMessage(), e);
                realm.cancelTransaction();
                throw e;
            }
        }
        realm.commitTransaction();
    }


    public void removeCategory(@NonNull ExpenditureCategory category) throws ZealousException, BackupException {
        GenericUtils.ensureNotNull(category);
        realm.beginTransaction();
        long matched = realm.where(Expenditure.class).equalTo(Expenditure.FIELD_CATEGORY + "." + ExpenditureCategory.FIELD_NAME, category.getName()).count();
        if (matched == 0) {
            if (category.isManaged()) {
                category.deleteFromRealm();
            } else {
                category = realm.where(ExpenditureCategory.class).equalTo(ExpenditureCategory.FIELD_NAME, category.getName()).findFirst();
                if (category != null) {
                    category.deleteFromRealm();
                }
            }
            if (backupManager != null) {
                try {
                    backupManager.log(
                            new RemoveCategoryOperation(category),
                            System.currentTimeMillis());
                } catch (BackupException e) {
                    PLog.f(TAG, e.getMessage(), e);
                    realm.cancelTransaction();
                    throw e;
                }
            }
        } else {
            realm.cancelTransaction();
            throw new ZealousException(GenericUtils.getString(R.string.category_already_attached));
        }
        realm.commitTransaction();
    }

    public <T extends RealmObject> T datach(T obj) {
        ensureNotClosed();
        return realm.copyFromRealm(obj);
    }

}
