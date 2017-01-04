package com.zealous.exchangeRates;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.zealous.R;
import com.zealous.ui.BaseZealousActivity;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.ViewUtils;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * Created by yaaminu on 1/3/17.
 */
public abstract class SearchActivity extends BaseZealousActivity {

    @Bind(R.id.search_et)
    EditText searchEt;
    @Bind(R.id.clear_search)
    ImageButton clearSearch;
    @Bind(R.id.search_view)
    View searchView;

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        ViewUtils.hideViews(searchView);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(!ViewUtils.isViewVisible(searchView));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                openSearch();
                return true;
            case android.R.id.home:
                if (ViewUtils.isViewVisible(searchView)) {
                    closeSearch();
                    return true;
                }//else fall through
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void openSearch() {
        // TODO: 1/3/17 show the keyboard
        searchEt.setText("");
        searchEt.requestFocus();
        ViewUtils.showViews(searchView);
        supportInvalidateOptionsMenu();
    }

    @OnFocusChange(R.id.search_et)
    void onFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @OnTextChanged(R.id.search_et)
    void onTextChanged(Editable text) {
        String processedText = text.toString().trim();
        if (!GenericUtils.isEmpty(processedText)) {
            ViewUtils.showViews(clearSearch);
        } else {
            ViewUtils.hideViews(clearSearch);
        }
        doSearch(processedText);
    }

    @OnClick(R.id.clear_search)
    void clearSearch() {
        searchEt.setText("");
    }

    @Override
    public void onBackPressed() {
        if (ViewUtils.isViewVisible(searchView)) {
            closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void closeSearch() {
        searchEt.setText("");
        ViewUtils.hideViews(searchView);
        supportInvalidateOptionsMenu();
    }

    protected abstract void doSearch(String constraint);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exchange_rate_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
