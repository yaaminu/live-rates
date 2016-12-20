package com.zealous.utils;

import android.widget.Toast;

/**
 * Created by yaaminu on 12/20/16.
 */
public class UiHelpers {
    public static void showToast(String toast) {
        Toast.makeText(Config.getApplicationContext(), toast, Toast.LENGTH_LONG).show();
    }
}
