package com.zealous.expense;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.zealous.R;
import com.zealous.utils.GenericUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import static com.zealous.exchangeRates.ExchangeRate.FORMAT;

/**
 * Created by yaaminu on 3/26/17.
 */

public class ExpenditureCategory extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_BUDGET = "budget";
    public static final String FIELD_BUDGET_DURATION = "budgetDuration";
    public static final int DAILY = 0, WEEKLY = 1, MONTHLY = 2, YEARLY = 3;

    public static final ExpenditureCategory DUMMY_EXPENDITURE_CATEGORY =
            new ExpenditureCategory("Add", 100, MONTHLY);
    @PrimaryKey
    private String name;
    @BudgetDuration
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private int budgetDuration;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private long budget;

    public ExpenditureCategory(String name, long budget, @BudgetDuration int budgetDuration) {
        GenericUtils.ensureNotEmpty(name);
        GenericUtils.ensureConditionTrue(budget >= 0, "budget can't be negative");
        GenericUtils.ensureConditionTrue(budgetDuration >= 0 && budgetDuration <= 3, "budget can't be negative");
        this.name = name;
        this.budget = budget;
        this.budgetDuration = budgetDuration;
    }

    public ExpenditureCategory() {

    }

    public String getName() {
        return name;
    }

    long getBudget() {
        return budget;
    }

    int getBudgetDuration() {
        return budgetDuration;
    }

    /**
     * @param context context for retrieving resources
     * @return the resource identifier corresponding to the
     * drawable for this resources.
     */
    @DrawableRes
    public int getIconWhite(Context context) {
        String resName = this.name.replaceAll("[\\W]+", "_") + "_white";
        int drawable = context.getResources().getIdentifier(resName.toLowerCase(Locale.US), "drawable", context.getPackageName());
        if (drawable == 0) {
            drawable = R.drawable.expense_category_custom_icon;
        }
        return drawable;
    }

    /**
     * @param context context for retrieving resources
     * @return the resource identifier corresponding to the
     * drawable for this resources.
     */
    @DrawableRes
    public int getIconViolet(Context context) {
        String resName = this.name.replaceAll("[\\W]+", "_") + "_violet";
        int drawable = context.getResources().getIdentifier(resName.toLowerCase(Locale.US), "drawable", context.getPackageName());
        if (drawable == 0) {
            drawable = R.drawable.expense_category_custom_icon_violet;
        }
        return drawable;
    }

    public String getNormalizedBudget() {
        return FORMAT.format(BigDecimal.valueOf(getBudget()).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128));
    }

    public BigDecimal getExpenditure(ExpenditureDataSource dataSource) {
        return BigDecimal
                .valueOf(dataSource.makeQuery().equalTo(Expenditure.FIELD_CATEGORY + "." + FIELD_NAME, getName())
                        .sum(Expenditure.FIELD_AMOUNT).longValue()).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
    }

    @NonNull
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(FIELD_BUDGET, getBudget());
        jsonObject.addProperty(FIELD_BUDGET_DURATION, getBudgetDuration());
        jsonObject.addProperty(FIELD_NAME, getName());
        return jsonObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenditureCategory category = (ExpenditureCategory) o;

        if (budgetDuration != category.budgetDuration) return false;
        //noinspection SimplifiableIfStatement
        if (budget != category.budget) return false;
        return name != null ? name.equals(category.name) : category.name == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + budgetDuration;
        result = 31 * result + (int) (budget ^ (budget >>> 32));
        return result;
    }

    @NonNull
    public static ExpenditureCategory fromJson(JsonObject jsonObject) {
        String name = jsonObject.get(FIELD_NAME).getAsString();
        long budget = jsonObject.get(FIELD_BUDGET).getAsLong();
        int duration = jsonObject.get(FIELD_BUDGET_DURATION).getAsInt();
        switch (duration) {
            case DAILY:
            case WEEKLY:
            case MONTHLY:
            case YEARLY:
                //do nothing
                break;
            default:
                throw new IllegalArgumentException("unknown duration type");
        }
        //we've already checked
        //noinspection WrongConstant
        return new ExpenditureCategory(name, budget, duration);
    }

    @IntDef({DAILY, WEEKLY, MONTHLY, YEARLY})
    public @interface BudgetDuration {
    }
}
