package com.zealous.expense;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.zealous.R;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/9/17.
 */


@Module
public class ExpenseFragmentProvider {
    private final ExpenseFragment fragment;
    private final String[] rangeNames;

    public ExpenseFragmentProvider(@NonNull ExpenseFragment fragment) {
        this.fragment = fragment;
        rangeNames = fragment.getResources().getStringArray(R.array.expense_range);
    }


    @Provides
    @Singleton
    public ExpenditureScreenPresenter createPresenter(@NonNull ExpenditureDataSource dataSource) {
        return new ExpenditureScreenPresenter(dataSource, rangeNames);
    }

    @Provides
    @Singleton
    public RecyclerView.LayoutManager provideLayoutManager() {
        return new LinearLayoutManager(fragment.getContext());
    }

    @Provides
    @Singleton
    public ExpenseAdapterDelegateImpl getDelegate(@NonNull ExpenditureScreenPresenter presenter) {
        return new ExpenseAdapterDelegateImpl(fragment.getContext(), presenter);
    }

    @Provides
    @Singleton
    public ExpenseAdapter createExpenseAdapter(@NonNull ExpenseAdapterDelegateImpl delegate) {
        return new ExpenseAdapter(delegate);
    }
}
