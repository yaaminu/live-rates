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
public class ClearOperationTest {
    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void data() throws Exception {
        ClearOperation operation = new ClearOperation();
        operation.dataSource = mock(ExpenditureDataSource.class);
        assertEquals(new JsonObject(), operation.data);
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

        ClearOperation operation = new ClearOperation();
        Assert.assertEquals(operation.data(), new JsonObject());
        operation.dataSource = PowerMockito.mock(ExpenditureDataSource.class);
        operation.dataSource = PowerMockito.spy(operation.dataSource);
        operation.replay();
        verify(operation.dataSource, times(1)).clear();
    }

}