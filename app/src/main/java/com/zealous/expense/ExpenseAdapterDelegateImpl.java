package com.zealous.expense;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

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
    private List<Expenditure> dataSet;


    public ExpenseAdapterDelegateImpl(Context context) {
        this.dataSet = Collections.emptyList();
        this.context = context;
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
        Toast.makeText(context(), "clicked item " + position, Toast.LENGTH_SHORT).show();
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
}
