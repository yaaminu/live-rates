package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.utils.Config;
import com.zealous.utils.FileUtils;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import static com.zealous.utils.GenericUtils.getString;

/**
 * Created by yaaminu on 3/26/17.
 */

public class Expenditure extends RealmObject {

    private static final String TAG = "Expenditure";
    public static final String FIELD_ID = "expenditureID";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_AMOUNT = "amountSpent";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_LOCATION = "location";
    public static final String FEILD_ATTACHMENTS = "attachments";
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

    private boolean backedUp;

    @Nullable
    RealmList<Attachment> attachments;

    @Nullable
    @Ignore
    private String normalizedAmount;

    public Expenditure() {

    }

    Expenditure(@NonNull String id, @NonNull String description, long amountSpent,
                ExpenditureCategory category, long time, @Nullable String location) {
        GenericUtils.ensureNotEmpty(id);
        GenericUtils.ensureNotNull(category, description);
        GenericUtils.ensureConditionTrue(amountSpent > 0, "amount must be greater than 0");
        GenericUtils.ensureConditionTrue(time > 0, "time is invalid");
        this.expenditureID = id;
        this.description = description;
        this.amountSpent = amountSpent;
        this.category = category;
        this.time = time;
        this.location = location;
        this.backedUp = false;
    }

    void setBackedUp(boolean backedUp) {
        this.backedUp = backedUp;
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
        return location == null ? getString(R.string.unspecified_location) : location;
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

    void addAttachment(@NonNull Attachment attachment) throws ZealousException {
        GenericUtils.ensureNotNull(attachment);
        if (attachments == null) {
            attachments = new RealmList<>();
        }
        if (attachments.size() >= 5) {
            throw new ZealousException(GenericUtils.getString(R.string.too_many_attachments));
        }
        if (isTooLarge(attachment.getBlob())) {
            throw new ZealousException(GenericUtils.getString(R.string.attachment_too_large));
        }
        attachments.add(attachment);
    }

    static boolean isTooLarge(byte[] blob) {
        return blob.length > FileUtils.ONE_MB * 3;
    }

    public boolean removeAttachment(Attachment attachment) {
        if (attachment == null) return false;
        if (attachments == null || attachments.isEmpty()) return false;

        for (int i = 0; i < attachments.size(); i++) {
            Attachment toRemove = attachments.get(i);
            if (attachment.equals(toRemove)) {
                return attachments.remove(i) != null;
            }
        }
        return false;
    }

    public List<Attachment> getAttachments() {
        // TODO: 4/22/17 disallow ui thread calls
        if (attachments == null) {
            return Collections.emptyList();
        }
        return attachments;
    }
}
