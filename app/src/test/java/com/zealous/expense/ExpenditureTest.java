package com.zealous.expense;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static com.zealous.expense.Expenditure.FEILD_ATTACHMENTS;
import static com.zealous.expense.Expenditure.FIELD_AMOUNT;
import static com.zealous.expense.Expenditure.FIELD_CATEGORY;
import static com.zealous.expense.Expenditure.FIELD_DESCRIPTION;
import static com.zealous.expense.Expenditure.FIELD_ID;
import static com.zealous.expense.Expenditure.FIELD_LOCATION;
import static com.zealous.expense.Expenditure.FIELD_TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by yaaminu on 5/30/17.
 */

public class ExpenditureTest {
    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Test
    public void toJson() throws Exception {
        int categoryBudget = 100;
        int amountSpent = 1023;
        long time = System.currentTimeMillis();
        ExpenditureCategory category = new ExpenditureCategory("name", categoryBudget, ExpenditureCategory.MONTHLY);
        Expenditure expenditure = new Expenditure("id", "description", amountSpent, category, time, "location");

        JsonObject expected = new JsonObject();
        expected.addProperty(FIELD_ID, "id");
        expected.addProperty(FIELD_AMOUNT, String.valueOf(BigDecimal.valueOf(amountSpent).divide(BigDecimal.valueOf(100),
                MathContext.DECIMAL128).doubleValue()));
        expected.addProperty(FIELD_DESCRIPTION, "description");
        expected.addProperty(FIELD_LOCATION, "location");
        expected.addProperty(FIELD_TIME, time);
        expected.add(FIELD_CATEGORY, category.toJson());

        JsonArray attArray = new JsonArray();

        Attachment att = new Attachment("hello", "blob".getBytes(), "text/plain");
        expenditure.addAttachment(att);
        attArray.add(att.toJson());

        att = new Attachment("hello2", "blob2".getBytes(), "text/plain");
        expenditure.addAttachment(att);
        attArray.add(att.toJson());

        att = new Attachment("hello3", "blob4".getBytes(), "text/plain");
        expenditure.addAttachment(att);
        attArray.add(att.toJson());

        expected.add(FEILD_ATTACHMENTS, attArray);

        assertEquals(expected, expenditure.toJson());
    }

    @Test
    public void fromJson() throws Exception {
        Expenditure expenditure = new Expenditure("id", "des", 1, new ExpenditureCategory("name", 1, ExpenditureCategory.MONTHLY),
                2, "loc");
        for (int i = 0; i < 4; i++) {
            expenditure.addAttachment(new Attachment("att" + i, ("att1" + i).getBytes(), "text/plain"));
        }
        JsonObject jsonObject = expenditure.toJson();
        Expenditure actual = Expenditure.fromJson(jsonObject);
        assertEquals(expenditure, actual);
        assertEquals(expenditure.getLocation(), actual.getLocation());
        assertEquals(expenditure.getDescription(), actual.getDescription());
        assertEquals(expenditure.getExpenditureTime(), actual.getExpenditureTime());
        assertEquals(expenditure.getNormalizedAmount(), actual.getNormalizedAmount());
        assertEquals(expenditure.getCategory(), actual.getCategory());
        assertEquals(expenditure.getAttachments().size(), 4);
        for (int i = 0; i < expenditure.getAttachments().size(); i++) {
            assertNotNull(expenditure.getAttachments().get(i));
            assertEquals(expenditure.getAttachments().get(i), actual.getAttachments().get(i));
        }
    }

}