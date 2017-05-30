package com.zealous.expense;

import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by yaaminu on 5/30/17.
 */
public class RemoveCategoryOperationTest {
    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void data() throws Exception {
        RemoveCategoryOperation operation = new RemoveCategoryOperation();
        operation.dataSource = mock(ExpenditureDataSource.class);
        assertEquals(operation.data(), new JsonObject());

        ExpenditureCategory expected = new ExpenditureCategory("name", 1, ExpenditureCategory.MONTHLY);
        operation = new RemoveCategoryOperation(expected);
        assertNotNull(operation.data());
        assertEquals(expected.toJson(), operation.data());

        expected = new ExpenditureCategory("name2", 2, ExpenditureCategory.DAILY);
        operation = new RemoveCategoryOperation(expected);
        assertEquals(expected.toJson(), operation.data());
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

        RemoveCategoryOperation operation = new RemoveCategoryOperation();
        Assert.assertEquals(operation.data(), new JsonObject());

        operation.dataSource = PowerMockito.mock(ExpenditureDataSource.class);
        operation.dataSource = PowerMockito.spy(operation.dataSource);
        operation.setData(expected.toJson());

        operation.replay();
        verify(operation.dataSource, times(1)).removeCategory(expected);

        expected = new ExpenditureCategory("name2", 3, ExpenditureCategory.DAILY);
        operation.setData(expected.toJson());
        operation.replay();
        verify(operation.dataSource, times(1)).removeCategory(expected);
    }
}