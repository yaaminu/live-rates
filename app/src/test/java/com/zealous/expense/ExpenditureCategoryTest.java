package com.zealous.expense;

import com.google.gson.JsonObject;

import org.junit.Test;

import static com.zealous.expense.ExpenditureCategory.FIELD_BUDGET;
import static com.zealous.expense.ExpenditureCategory.FIELD_BUDGET_DURATION;
import static com.zealous.expense.ExpenditureCategory.FIELD_NAME;
import static org.junit.Assert.assertEquals;

/**
 * Created by yaaminu on 5/30/17.
 */
public class ExpenditureCategoryTest {
    @Test
    public void toJson() throws Exception {
        ExpenditureCategory category = new ExpenditureCategory("name", 22, ExpenditureCategory.DAILY);
        JsonObject jsonObject = category.toJson();
        assertEquals(category.getBudgetDuration(), jsonObject.get(FIELD_BUDGET_DURATION).getAsInt());
        assertEquals(category.getBudget(), jsonObject.get(FIELD_BUDGET).getAsLong());
        assertEquals(category.getName(), jsonObject.get(FIELD_NAME).getAsString());


        category = new ExpenditureCategory("name2", 2992, ExpenditureCategory.MONTHLY);
        jsonObject = category.toJson();
        assertEquals(category.getBudgetDuration(), jsonObject.get(FIELD_BUDGET_DURATION).getAsInt());
        assertEquals(category.getBudget(), jsonObject.get(FIELD_BUDGET).getAsLong());
        assertEquals(category.getName(), jsonObject.get(FIELD_NAME).getAsString());
    }

    @Test
    public void fromJson() throws Exception {
        ExpenditureCategory category = new ExpenditureCategory("name", 22, ExpenditureCategory.DAILY);
        JsonObject jsonObject = category.toJson();
        jsonObject.addProperty(FIELD_BUDGET, category.getBudget());
        jsonObject.addProperty(FIELD_BUDGET_DURATION, category.getBudgetDuration());
        jsonObject.addProperty(FIELD_NAME, category.getName());


        ExpenditureCategory actual = ExpenditureCategory.fromJson(jsonObject);
        assertEquals(category, actual);

        assertEquals(category.getBudgetDuration(), actual.getBudgetDuration());
        assertEquals(category.getBudget(), actual.getBudget());
        assertEquals(category.getName(), actual.getName());
    }

}