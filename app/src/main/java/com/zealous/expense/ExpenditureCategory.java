package com.zealous.expense;

import android.content.Context;
import android.support.annotation.DrawableRes;

import com.zealous.R;
import com.zealous.utils.GenericUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaaminu on 3/26/17.
 */

public class ExpenditureCategory extends RealmObject {

    public static final String FEILD_NAME = "name";
    public static final String FEILD_BUDGET = "budget";
    @PrimaryKey
    private String name;

    @SuppressWarnings("FieldCanBeLocal")
    private long budget;

    public ExpenditureCategory(String name, long budget) {
        GenericUtils.ensureNotEmpty(name);
        GenericUtils.ensureConditionTrue(budget >= 0, "budget can't be negative");
        this.name = name;
        this.budget = budget;
    }

    public ExpenditureCategory() {

    }

    public String getName() {
        return name;
    }

    /**
     * @param context context for retrieving resources
     * @return the resource identifier corresponding to the
     * drawable for this resources.
     */
    @DrawableRes
    public int getIcon(Context context) {
        String resName = this.name.replaceAll("[^A-Za-z]*", "_");
        int drawable = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        if (drawable == 0) {
            drawable = R.drawable.expense_category_custom_icon;
        }
        return drawable;
    }
}
