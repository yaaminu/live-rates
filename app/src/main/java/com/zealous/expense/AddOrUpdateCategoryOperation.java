package com.zealous.expense;

import com.backup.BackupException;
import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

/**
 * Created by yaaminu on 5/30/17.
 */

public class AddOrUpdateCategoryOperation extends BaseExpenditureOperation {
    private static final String TAG = "AddOrUpdateCategoryOper";

    static final String CATEGORY = "category";
    static final String PREVIOUS_NAME = "previousName";

    public AddOrUpdateCategoryOperation() {
    } //required

    public AddOrUpdateCategoryOperation(String previousName, ExpenditureCategory category) {
        this.data = new JsonObject();
        this.data.add(CATEGORY, category.toJson());
        if (previousName != null) {
            this.data.addProperty(PREVIOUS_NAME, previousName);
        }
    }

    @Override
    protected void doReplay() throws BackupException {
        PLog.d(TAG, "replaying operation: %s", getClass().getName());
        ExpenditureCategory category =
                ExpenditureCategory.fromJson(this.data.get(CATEGORY).getAsJsonObject());
        String previousName = null;
        if (data.has(PREVIOUS_NAME)) {
            previousName = data.get(PREVIOUS_NAME).getAsString();
        }
        assert dataSource != null;
        dataSource.addOrUpdateCategory(previousName, category);
    }
}
