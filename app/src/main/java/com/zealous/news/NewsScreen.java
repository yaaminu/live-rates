package com.zealous.news;

import com.zealous.ui.Screen;

import java.util.List;

/**
 * Created by yaaminu on 4/25/17.
 */
public interface NewsScreen extends Screen {
    void refreshDisplay(List<NewsItem> dataSet);

    void showLoading(boolean loading);
}
