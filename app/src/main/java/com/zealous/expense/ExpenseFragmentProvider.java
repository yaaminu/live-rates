package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/9/17.
 */


@Module
public class ExpenseFragmentProvider {
    private final ExpenseFragment fragment;

    public ExpenseFragmentProvider(@NonNull ExpenseFragment fragment) {
        this.fragment = fragment;
    }


    @Provides
    @Singleton
    public ExpenditureScreenPresenter createPresenter(@NonNull ExpenditureDataSource dataSource) {
        return new ExpenditureScreenPresenter(dataSource);
    }

    @Provides
    @Singleton
    public RecyclerView.LayoutManager provideLayoutManager() {
        return new LinearLayoutManager(fragment.getContext());
    }

    @Provides
    @Singleton
    public ExpenseAdapterDelegateImpl getDelegate() {
        return new ExpenseAdapterDelegateImpl(fragment.getContext());
    }

    @Provides
    @Singleton
    public ExpenseAdapter createExpenseAdapter(@NonNull ExpenseAdapterDelegateImpl delegate) {
        return new ExpenseAdapter(delegate);
    }
}
