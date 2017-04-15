package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import com.zealous.ui.BaseFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/14/17.
 */

@Module
public class AddExpenseFragmentProvider extends BaseExpenditureProvider {
    private final BaseFragment fragment;
    private final ExpenditureCategoryAdapter.Delegate delegate;

    public AddExpenseFragmentProvider(@NonNull BaseFragment fragment, ExpenditureCategoryAdapter.Delegate delegate) {
        this.fragment = fragment;
        this.delegate = delegate;
    }

    @Provides
    @Singleton
    public AddExpenditurePresenter getPresenter(@NonNull ExpenditureDataSource dataSource) {
        return new AddExpenditurePresenter(dataSource);
    }

    @Provides
    @Singleton
    public GridLayoutManager getLayoutManager() {
        return new GridLayoutManager(fragment.getContext(), 3, LinearLayoutManager.VERTICAL, false);
    }

    @Provides
    @Singleton
    public ExpenditureCategoryAdapter getExpenditureCategoryAdapter() {
        return new ExpenditureCategoryAdapter(delegate);
    }
}
