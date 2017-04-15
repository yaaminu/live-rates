package com.zealous.expense;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.zealous.adapter.BaseAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Created by yaaminu on 4/8/17.
 */

public class ExpenseAdapterDelegateImpl implements ExpenseAdapter.Delegate {

    private static final String TAG = "ExpenseAdapterDelegateImpl";
    private final Context context;
    @NonNull
    private final ExpenditureScreenPresenter presenter;
    @NonNull
    private List<Expenditure> dataSet;
    private int selectedItem = RecyclerView.NO_POSITION;

    public ExpenseAdapterDelegateImpl(@NonNull Context context, @NonNull ExpenditureScreenPresenter presenter) {
        this.dataSet = Collections.emptyList();
        this.context = context;
        this.presenter = presenter;
    }

    public void refreshDataSet(@NonNull List<Expenditure> dataSet, @NonNull ExpenseAdapter adapter) {
        this.dataSet = dataSet;
        adapter.notifyDataChanged("");
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public void onItemClick(BaseAdapter<ExpenseItemHolder, Expenditure> adapter, View view,
                            int position, long id) {
        if (selectedItem == position) { //this already selected so toggle
            selectedItem = RecyclerView.NO_POSITION;
            adapter.notifyItemChanged(position);
        } else {
            int previous = selectedItem;
            selectedItem = position;
            if (previous != RecyclerView.NO_POSITION) {
                adapter.notifyItemChanged(previous);
            }
            adapter.notifyItemChanged(selectedItem);
        }
    }

    @Override
    public boolean onItemLongClick(BaseAdapter<ExpenseItemHolder, Expenditure> adapter, View
            view, int position, long id) {

        return false;
    }

    @NonNull
    @Override
    public List<Expenditure> dataSet(String constrain) {
        return dataSet;
    }

    @Override
    public int getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void deleteItem(int position) {
        selectedItem = AdapterView.INVALID_POSITION;
        presenter.deleteItem(context, dataSet.get(position));
    }

    @Override
    public void editItem(int position) {
        selectedItem = AdapterView.INVALID_POSITION;
        presenter.editItem(context, dataSet.get(position));
    }
}
