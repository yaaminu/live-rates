package com.zealous.expense;

import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static com.zealous.expense.AddOrUpdateCategoryOperation.CATEGORY;
import static com.zealous.expense.AddOrUpdateCategoryOperation.PREVIOUS_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by yaaminu on 5/30/17.
 */
public class AddOrUpdateCategoryOperationTest {
    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void data() throws Exception {
        AddOrUpdateCategoryOperation operation = new AddOrUpdateCategoryOperation();
        operation.dataSource = mock(ExpenditureDataSource.class);
        assertEquals(operation.data(), new JsonObject());

        ExpenditureCategory expected = new ExpenditureCategory("name", 1, ExpenditureCategory.MONTHLY);
        operation = new AddOrUpdateCategoryOperation(null, expected);
        assertNotNull(operation.data());
        JsonObject expectedJson = new JsonObject();
        expectedJson.add(CATEGORY, expected.toJson());
        assertEquals(expectedJson, operation.data());

        operation = new AddOrUpdateCategoryOperation("name", expected);
        expectedJson = new JsonObject();
        expectedJson.add(CATEGORY, expected.toJson());
        expectedJson.addProperty(PREVIOUS_NAME, "name");
        assertEquals(expectedJson, operation.data());
    }

    @Test
    public void setData() throws Exception {
        AddOrUpdateCategoryOperation operation = new AddOrUpdateCategoryOperation();
        assertEquals(operation.data(), new JsonObject());
        JsonObject test = new JsonObject();
        test.addProperty("foo", "bar");
        operation.setData(test);
        assertEquals(test.toString(), operation.data().toString());
    }

    @Test
    public void replay() throws Exception {
        ExpenditureCategory expected = new ExpenditureCategory("name", 1, ExpenditureCategory.MONTHLY);

        JsonObject expectedJson = new JsonObject();
        expectedJson.add(CATEGORY, expected.toJson());
        expectedJson.addProperty(PREVIOUS_NAME, "name");

        AddOrUpdateCategoryOperation operation = new AddOrUpdateCategoryOperation();
        Assert.assertEquals(operation.data(), new JsonObject());

        operation.dataSource = PowerMockito.mock(ExpenditureDataSource.class);
        operation.dataSource = PowerMockito.spy(operation.dataSource);

        operation.setData(expectedJson);
        operation.replay();
        verify(operation.dataSource, times(1)).addOrUpdateCategory("name", expected);

        expectedJson.remove(PREVIOUS_NAME);

        operation.replay();
        verify(operation.dataSource, times(1)).addOrUpdateCategory(null, expected);
    }

}