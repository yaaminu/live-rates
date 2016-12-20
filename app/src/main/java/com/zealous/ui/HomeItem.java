package com.zealous.ui;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

/**
 * @author by yaaminu on 12/20/16.
 */

public class HomeItem {
    public final String title;
    public final int icon;
    public final int titleColor;

    public HomeItem(String title, @DrawableRes int icon, @ColorRes int titleColor) {
        this.title = title;
        this.icon = icon;
        this.titleColor = titleColor;
    }
}
