package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.utils.GenericUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by yaaminu on 3/26/17.
 */

public class Expenditure extends RealmObject {


    public static final String FIELD_ID = "expenditureID";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_AMOUNT = "amountSpent";
    public static final String FIELD_CATEGORY = "category";
    @Index
    private long amountSpent;
    @Required
    private String description;
    private ExpenditureCategory category;

    @Index
    private long time;
    @Nullable
    private String location;
    @SuppressWarnings("unused")
    @PrimaryKey
    private String expenditureID;

    @Nullable
    @Ignore
    private String normalizedAmount;

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

    @NonNull
    public String getNormalizedAmount() {
        if (normalizedAmount == null) {
            normalizedAmount = ExchangeRate.FORMAT.format(BigDecimal.valueOf(getAmountSpent())
                    .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128));
        }
        return normalizedAmount;
    }

    private long getAmountSpent() {
        return amountSpent;
    }

    public Date getExpenditureTime() {
        return new Date(time);
    }

    public ExpenditureCategory getCategory() {
        return category;
    }

    void setCategory(@NonNull ExpenditureCategory category) {
        GenericUtils.ensureNotNull(category);
        this.category = category;
    }

    @NonNull
    public String getLocation() {
        return location == null ? "" : location;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return expenditureID;
    }

    void setId(@NonNull String id) {
        GenericUtils.ensureNotEmpty(id);
        GenericUtils.ensureConditionTrue(id.length() > 10, "id too short");
        this.expenditureID = id;
    }
}
