package com.zealous.expense;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.utils.GenericUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by yaaminu on 4/8/17.
 */

public class ExpenseAdapterDelegateImpl implements ExpenseAdapter.Delegate {

    private static final String TAG = "ExpenseAdapterDelegateImpl";
    private final ExpenseFragment context;
    @NonNull
    private final ExpenditureScreenPresenter presenter;
    @NonNull
    private List<Expenditure> dataSet;
    private int selectedItem = RecyclerView.NO_POSITION;

    public ExpenseAdapterDelegateImpl(@NonNull ExpenseFragment context, @NonNull ExpenditureScreenPresenter presenter) {
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
        return context.getContext();
    }

    @Override
    public void onItemClick(BaseAdapter<ExpenseItemHolder, Expenditure> adapter, View view,
                            int position, long id) {
        if (selectedItem == position) { //this already selected so toggle
            selectedItem = RecyclerView.NO_POSITION;
            adapter.notifyItemChanged(position);
            context.showFab();
        } else {
            int previous = selectedItem;
            selectedItem = position;
            if (previous != RecyclerView.NO_POSITION) {
                adapter.notifyItemChanged(previous);
            }
            adapter.notifyItemChanged(selectedItem);
            context.hideFab();
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
        final Expenditure expenditure = dataSet.get(position);
        GenericUtils.showComfirmationDialog(context.getContext(), context.getString(R.string.delete_warning), new Runnable() {
            @Override
            public void run() {
                presenter.deleteItem(context.getContext(), expenditure);
            }
        });
    }

    @Override
    public void editItem(int position) {
        selectedItem = AdapterView.INVALID_POSITION;
        context.showFab();
        presenter.editItem(context.getContext(), dataSet.get(position));
    }
}
