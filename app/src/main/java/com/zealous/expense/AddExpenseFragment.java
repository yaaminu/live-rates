package com.zealous.expense;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/14/17.
 */

public class AddExpenseFragment extends BaseFragment implements AddExpenseScreen {
    public static final int INVALID_POSITION = -1;
    @Inject
    AddExpenditurePresenter addExpenditurePresenter;
    @Inject
    ExpenditureCategoryAdapter adapter;
    @Inject
    GridLayoutManager layoutManager;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.tv_date)
    TextView date;
    @Bind(R.id.tv_location)
    TextView location;
    @Bind(R.id.et_description)
    EditText note;
    @Bind(R.id.et_amount)
    EditText amount;
    @Bind(R.id.currency)
    TextView currency;

    int selectedItem = INVALID_POSITION;
    private List<ExpenditureCategory> categories = Collections.emptyList();
    private ExpenditureCategoryAdapter.Delegate delegate = new ExpenditureCategoryAdapter.Delegate() {
        @Override
        public Context context() {
            return getContext();
        }

        @Override
        public void onItemClick(BaseAdapter<CategoryHolder, ExpenditureCategory> adapter, View view, int position, long id) {
            //whatever be the case, clear the current selection
            if (selectedItem != INVALID_POSITION) {
                adapter.notifyItemChanged(selectedItem);
            }
            if (adapter.getItem(position) == ExpenditureCategory.DUMMY_EXPENDITURE_CATEGORY) {
                addExpenditurePresenter.onAddCustomCategory(getFragmentManager());
            } else {
                selectedItem = position;
                adapter.notifyItemChanged(position);
            }
        }

        @Override
        public boolean onItemLongClick(BaseAdapter<CategoryHolder, ExpenditureCategory> adapter, View view, int position, long id) {
            return false;
        }

        @NonNull
        @Override
        public List<ExpenditureCategory> dataSet(String constrain) {
            return categories;
        }

        @Override
        public int getSelectedItemPosition() {
            return selectedItem;
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.layout_add_expense;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DaggerAddExpenditureComponent.builder()
                .addExpenseFragmentProvider(new AddExpenseFragmentProvider(this, delegate))
                .build().inject(this);
        addExpenditurePresenter.onCreate(savedInstanceState, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_expenditure_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                addNewExpenditure();
                break;
            case R.id.action_attach:
                //fall through
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @OnClick({R.id.edit_date, R.id.edit_location})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_date:
                addExpenditurePresenter.editDate(getFragmentManager());
                break;
            case R.id.edit_location:
                addExpenditurePresenter.editLocation(getFragmentManager());
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public void refreshDisplay(List<ExpenditureCategory> categories, long time, String location, String currency) {
        this.categories = categories;
        // TODO: 4/15/17 set date and location appropriately
        date.setText(DateUtils.formatDateTime(getContext(), time,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR));
        this.location.setText(location);
        adapter.notifyDataChanged("");
        this.currency.setText(currency);
    }

    @Override
    public void showValidationError(String errorMessage) {
        new AlertDialog.Builder(getContext())
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.error)
                .create().show();
    }

    void addNewExpenditure() {
        // TODO: 4/15/17 validate input
        if (addExpenditurePresenter
                .onAddExpenditure(amount.getText().toString(),
                        note.getText().toString(), selectedItem)) {
            getActivity().finish();
        }
    }

    @Nullable
    @Override
    protected BasePresenter<?> getBasePresenter() {
        return addExpenditurePresenter;
    }
}
