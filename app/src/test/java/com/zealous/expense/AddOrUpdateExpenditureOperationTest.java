package com.zealous.expense;

import com.google.gson.JsonObject;
import com.zealous.utils.PLog;

import org.junit.Rule;
import org.junit.Test;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by yaaminu on 5/30/17.
 */
public class AddOrUpdateExpenditureOperationTest {
    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void data() throws Exception {
        AddOrUpdateExpenditureOperation operation = new AddOrUpdateExpenditureOperation();
        operation.dataSource = mock(ExpenditureDataSource.class);
        assertEquals(operation.data(), new JsonObject());
        Expenditure expected = new Expenditure("id", "description", 1,
                new ExpenditureCategory("name", 1, ExpenditureCategory.MONTHLY), 3, "location");
        operation = new AddOrUpdateExpenditureOperation(expected);
        assertEquals(expected.toJson(), operation.data());
        assertNotNull(operation.data());
    }

    @Test
    public void setData() throws Exception {
        AddOrUpdateExpenditureOperation operation = new AddOrUpdateExpenditureOperation();
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
        AddOrUpdateExpenditureOperation operation = new AddOrUpdateExpenditureOperation();
        operation.dataSource = mock(ExpenditureDataSource.class);
        operation.dataSource = spy(operation.dataSource);
        operation.setData(expected.toJson());
        operation.replay();
        verify(operation.dataSource, times(1)).addOrUpdateExpenditure(expected);

        ExpenditureDataSource tmp = operation.dataSource;
        operation.dataSource = null;
        try {
            operation.replay();
            fail("must throw illegalstate exception when the data source is not injected");
        } catch (IllegalStateException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        operation.dataSource = tmp;
        //noinspection ConstantConditions
        operation.setData(null);
        try {
            operation.replay();
            fail("must throw illegalstate exception when the data source is not injected");
        } catch (IllegalStateException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
    }

}