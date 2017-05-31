package com.zealous.expense;

import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by yaaminu on 5/30/17.
 */
public class RemoveExpenditureOperationTest {
    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void data() throws Exception {
        RemoveExpenditureOperation operation = new RemoveExpenditureOperation();
        operation.dataSource = mock(ExpenditureDataSource.class);
        assertEquals(operation.data(), new JsonObject());
        Expenditure expected = new Expenditure("id", "description", 1,
                new ExpenditureCategory("name", 1, ExpenditureCategory.MONTHLY), 3, "location");
        operation = new RemoveExpenditureOperation(expected.getId());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Expenditure.FIELD_ID, expected.getId());
        assertEquals(jsonObject, operation.data());
    }

    @Test
    public void setData() throws Exception {
        RemoveExpenditureOperation operation = new RemoveExpenditureOperation();
        assertEquals(operation.data(), new JsonObject());
        JsonObject test = new JsonObject();
        test.addProperty("foo", "bar");
        operation.setData(test);
        assertEquals(test.toString(), operation.data().toString());
    }

    @Test
    public void replay() throws Exception {
        Expenditure expected = new Expenditure("id", "description", 1,
                new ExpenditureCategory("name", 1, ExpenditureCategory.MONTHLY), 3, "location");

        RemoveExpenditureOperation operation = new RemoveExpenditureOperation();
        Assert.assertEquals(operation.data(), new JsonObject());
        operation.dataSource = PowerMockito.mock(ExpenditureDataSource.class);
        operation.dataSource = PowerMockito.spy(operation.dataSource);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Expenditure.FIELD_ID, expected.getId());
        operation.setData(jsonObject);
        operation.replay();
        verify(operation.dataSource, times(1)).removeExpenditure(expected.getId());
    }


}