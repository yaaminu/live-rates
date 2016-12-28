package com.zealous.exchangeRates;

import com.zealous.utils.Config;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yaaminu on 12/23/16.
 */

class ExchangeRateLoader {


    public static JSONObject loadRates() throws IOException {
        try {
            // FIXME: 12/23/16 load rates from the network;
            InputStream in = Config.getApplicationContext().getAssets().open("latest.json");
            return new JSONObject(IOUtils.toString(in));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
