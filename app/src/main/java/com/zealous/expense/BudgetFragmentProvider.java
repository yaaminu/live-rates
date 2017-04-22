package com.zealous.expense;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/17/17.
 */

@Module
public class BudgetFragmentProvider {
    private final BudgetFragment fragment;

    public BudgetFragmentProvider(BudgetFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public BudgetAdapter adapter(BudgetAdapter.Delegate delegate) {
        return new BudgetAdapter(delegate);
    }

    @Provides
    public BudgetFragmentPresenter presenter(ExpenditureDataSource dataSource) {
        return new BudgetFragmentPresenter(dataSource);
    }

    @Provides
    public BudgetAdapter.Delegate delegate() {
        return new BudgetAdapterDelegateImpl(fragment);
    }

    @Provides
    public RecyclerView.LayoutManager manager() {
        return new LinearLayoutManager(fragment.getContext(), LinearLayoutManager.VERTICAL, false);
    }
}
