package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.io.Closeable;
import java.util.Collection;
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
    private final Realm realm;

    /**
     * creates a new data source backed by {@code realm}
     *
     * @param realm the realm, may not be null
     * @throws IllegalArgumentException if {@code realm} is null
     * @throws IllegalStateException    if {@code realm} is closed
     */
    @Inject
    public ExpenditureDataSource(@NonNull Realm realm) {
        GenericUtils.ensureNotNull(realm);
        this.realm = realm;
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
     */
    public void addOrUpdateExpenditure(@NonNull Expenditure expenditure) {
        ensureNotClosed();
        GenericUtils.ensureNotNull(expenditure);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(expenditure);
        realm.commitTransaction();
    }

    /**
     * add/updates all expenditures stored in the backing realm
     *
     * @param expenditures the expenditures to add may not be null
     * @throws IllegalStateException    if this data sources is closed
     * @throws IllegalStateException    if call is used on a different thread
     * @throws IllegalArgumentException if expenditure is null
     */
    public void addOrUpdateAll(@NonNull Collection<Expenditure> expenditures) {
        GenericUtils.ensureNotNull(expenditures);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(expenditures);
        realm.commitTransaction();
    }

    /**
     * removes an expenditure with {@code id = expenditureId}
     *
     * @param expenditureId the id of the expenditure to delete.
     *                      may not be null.
     * @return true if an expenditure with id of {@code expenditureId} was
     * actually removed otherwise false
     * @throws IllegalStateException if this data sources is closed
     * @throws IllegalStateException if call is used on a different thread
     */
    public boolean removeExpenditure(@NonNull String expenditureId) {
        GenericUtils.ensureNotEmpty(expenditureId);
        ensureNotClosed();
        realm.beginTransaction();
        Expenditure expenditure =
                realm.where(Expenditure.class).equalTo(Expenditure.FIELD_ID, expenditureId).findFirst();
        if (expenditure != null) {
            expenditure.deleteFromRealm();
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
     */
    public void clear() {
        ensureNotClosed();
        realm.beginTransaction();
        realm.deleteAll();
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

    public void addOrUpdateCategory(@Nullable final String previousName, ExpenditureCategory category) {
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
        realm.commitTransaction();
    }


    public void removeCategory(@NonNull ExpenditureCategory category) throws ZealousException {
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
