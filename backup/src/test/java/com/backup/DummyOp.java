package com.backup;

import com.google.gson.JsonObject;

import java.security.SecureRandom;

/**
 * Created by yaaminu on 5/18/17.
 */
public class DummyOp implements Operation {

    private final int randomNum;
    private JsonObject data;

    public DummyOp() {
        this(0);
    }

    public DummyOp(int randomNum) {
        this.randomNum = randomNum;
        this.data = new JsonObject();
        SecureRandom secureRandom = new SecureRandom();

        data.addProperty("field" + randomNum + "" + secureRandom.nextLong(),
                "value" + randomNum + "" + secureRandom.nextInt());
    }


    @Override
    public void setData(JsonObject data) {
        this.data = data;
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public void replay() throws BackupException {
        System.out.println("replaying the operation");
        System.out.println("data is :  \n" + data.toString());
    }
}
