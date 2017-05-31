package com.zealous.news;

import android.support.annotation.StringRes;

import com.zealous.ui.Screen;

import java.util.List;

/**
 * Created by yaaminu on 4/25/17.
 */
public interface NewsScreen extends Screen {
    void refreshDisplay(List<NewsItem> dataSet, boolean isFavorites);

    void showLoading(boolean loading);

    void showDialogMessage(CharSequence message);

    void showDialogMessage(@StringRes int message);
}
