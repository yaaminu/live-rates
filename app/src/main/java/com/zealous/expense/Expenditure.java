package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.utils.GenericUtils;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by yaaminu on 3/26/17.
 */

public class Expenditure extends RealmObject {


    public static final String FIELD_ID = "expenditureID";
    public static final String FIELD_TIME = "time";

    @Index
    private long amountSpent;
    @Required
    private String description;
    private ExpenditureCategory category;
    private long time;
    @Nullable
    private String location;
    @PrimaryKey
    private String expenditureID;

    public Expenditure() {

    }

    Expenditure(@NonNull String id, @NonNull String description, long amountSpent,
                ExpenditureCategory category, long time, @Nullable String location) {
        GenericUtils.ensureNotEmpty(description, id);
        GenericUtils.ensureNotNull(category);
        GenericUtils.ensureConditionTrue(amountSpent > 0, "amount must be greater than 0");
        GenericUtils.ensureConditionTrue(time > 0, "time is invalid");
        this.expenditureID = id;
        this.description = description;
        this.amountSpent = amountSpent;
        this.category = category;
        this.time = time;
        this.location = location;
    }
}
