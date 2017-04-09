package com.zealous.expense;

import android.content.Context;
import android.support.annotation.DrawableRes;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaaminu on 3/26/17.
 */

public class ExpenditureCategory extends RealmObject {

    @PrimaryKey
    private String name;

    private long budget;

    public ExpenditureCategory(String name, long budget) {
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
    public int getDrawableResource(Context context) {
        String resName = this.name.replaceAll("[^A-Za-z]*", "_");
        return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
    }
}
