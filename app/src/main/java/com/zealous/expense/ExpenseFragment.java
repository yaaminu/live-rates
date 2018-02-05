package com.zealous.expense;

import android.animation.Animator;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.backup.BackupManager;
import com.backup.DependencyInjector;
import com.backup.Operation;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.zealous.R;
import com.zealous.Zealous;
import com.zealous.exchangeRates.ExchangeRateListActivity;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;
import static com.zealous.utils.ViewUtils.showByFlag;

/**
 * @author by yaaminu on 4/8/17.
 */

public class ExpenseFragment extends BaseFragment implements ExpenseListScreen {

    private static final String TAG = "ExpenseFragment";

    @Inject
    ExpenditureScreenPresenter expenditureScreenPresenter;
    @Inject
    ExpenseAdapter adapter;
    @Inject
    ExpenseAdapterDelegateImpl delegate;
    @Inject
    RecyclerView.LayoutManager layoutManager;

    @Inject
    EventBus eventBus;

    @BindView(R.id.recycler_view)
    RecyclerView expenseList;
    @BindView(R.id.empty_view)
    TextView emptyView;

    @BindView(R.id.total_expenditure)
    TextView totalExpenditure;
    @BindView(R.id.monthly_budget)
    TextView totalBudget;
    @BindView(R.id.expenditure_range_text)
    TextView rangeText;
    @BindView(R.id.today_s_date)
    TextView todaysDate;
    @BindView(R.id.year)
    TextView year;

    @BindView(R.id.header)
    View totalView;
    @BindView(R.id.fab)
    FloatingActionsMenu fab;
    private CustomScrollListener listener;
    private FloatingActionButton addExpenditureFab;
    private FloatingActionButton viewBudgetFab;

    private final DependencyInjector injector = new DependencyInjector() {
        @Override
        public void inject(Operation operation) {
            if (operation instanceof BaseExpenditureOperation) {
                ((BaseExpenditureOperation) operation).dataSource
                        = expenditureScreenPresenter.getDataSource();
            }
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.fragment_expenses;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        BackupManager backupManager = ((Zealous) getActivity().getApplication()).getExpenseBackupManager();

        DaggerExpenseFragmentComponent
                .builder()
                .baseExpenditureProvider(new BaseExpenditureProvider(backupManager))
                .expenseFragmentProvider(new ExpenseFragmentProvider(this))
                .build()
                .inject(this);
        expenditureScreenPresenter.onCreate(savedInstanceState, this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        PLog.d(TAG, "received event %s", event);
        if (event instanceof Map) {
            String value = ((Map<String, String>) event).get(ExchangeRateListActivity.SEARCH);
            if (value != null) {
                expenditureScreenPresenter.search(value);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expense_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        expenseList.addOnScrollListener(listener);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        expenseList.removeOnScrollListener(listener);
        super.onPause();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expenseList.setLayoutManager(layoutManager);
        expenseList.setAdapter(adapter);
        listener = new CustomScrollListener(this);
        year.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        todaysDate.setText(DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));


        LayoutInflater inflater = LayoutInflater.from(getContext());
        addExpenditureFab = (FloatingActionButton) inflater.inflate(R.layout.layout, null);
//        addExpenditureFab.setTag(R.id.fab_label, inflater.inflate(R.layout.lable_text, null));
        addExpenditureFab.setTitle(getString(R.string.add_expenditure));
        addExpenditureFab.setIcon(R.drawable.ic_add_expense);
        addExpenditureFab.setColorNormalResId(R.color.white);
        addExpenditureFab.setColorPressedResId(R.color.faint_voilet);
        addExpenditureFab.setId(R.id.add_new_expenditure);
        this.fab.addButton(addExpenditureFab);

        viewBudgetFab = (FloatingActionButton) inflater.inflate(R.layout.layout, null);
//        addExpenditureFab.setTag(R.id.fab_label, inflater.inflate(R.layout.lable_text, null));
        viewBudgetFab.setTitle(getString(R.string.view_budget));
        viewBudgetFab.setIcon(R.drawable.budget_icon);
        viewBudgetFab.setColorNormalResId(R.color.white);
        viewBudgetFab.setColorPressedResId(R.color.faint_voilet);
        viewBudgetFab.setId(R.id.action_view_budget);
        this.fab.addButton(viewBudgetFab);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                switch (v.getId()) {
                    case R.id.add_new_expenditure:
                        expenditureScreenPresenter.onAddNewExpenditure(getContext());
                        break;
                    case R.id.action_view_budget:
                        expenditureScreenPresenter.onMenuItemClicked(R.id.action_view_budget);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        };
        this.addExpenditureFab.setOnClickListener(clickListener);
        this.viewBudgetFab.setOnClickListener(clickListener);
    }

    @OnClick(R.id.expenditure_range)
    void changeRange() {
        new AlertDialog.Builder(getContext())
                .setItems(expenditureScreenPresenter.getRangNames(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expenditureScreenPresenter.onChangeExpenditureRange(which);
                    }
                }).create().show();
    }
//
//    @OnClick(R.id.fab)
//    void addExpenditure() {
//        fab.toggle();
//    }

    @Nullable
    @Override
    protected BasePresenter<?> getBasePresenter() {
        return expenditureScreenPresenter;
    }

    @Override
    public void refreshDisplay(@NonNull List<Expenditure> expenditures,
                               String totalExpenditure, String totalBudget, String rangeName) {
        GenericUtils.ensureNotNull(expenditures);
        delegate.refreshDataSet(expenditures, adapter);
        this.totalBudget.setText(getString(R.string.total_budget, totalBudget));
        this.totalExpenditure.setText(getString(R.string.total_expenditire, totalExpenditure));
        showByFlag(expenditures.isEmpty(), emptyView);
        showByFlag(!expenditures.isEmpty(), expenseList);
        rangeText.setText(rangeName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return expenditureScreenPresenter.onMenuItemClicked(item.getItemId())
                || super.onOptionsItemSelected(item);
    }

    @Override
    public Activity getCurrentActivity() {
        return getActivity();
    }

    public void showFab() {
        fab.animate().scaleX(1.0f).scaleY(1.0f).start();
    }

    public void hideFab() {
        fab.collapse();
        fab.animate().scaleX(0).scaleY(0).setInterpolator(new AccelerateInterpolator()).start();
    }


    private static class CustomScrollListener extends RecyclerView.OnScrollListener {

        private final View totalView;
        private final View fab;
        private final ExpenseFragment fragment;

        public CustomScrollListener(ExpenseFragment fragment) {
            this.totalView = fragment.totalView;
            this.fab = fragment.fab;
            this.fragment = fragment;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            String scrollState;
            switch (newState) {
                case SCROLL_STATE_DRAGGING:
                    fragment.hideFab();
                    scrollState = "dragging";
                    break;
                case SCROLL_STATE_IDLE:
                    scrollState = "idle";
                    fragment.showFab();
                    break;
                case SCROLL_STATE_SETTLING:
                    scrollState = "settling";
                    break;
                default:
                    scrollState = "unknown";
                    break;
            }
            PLog.d(TAG, "scroll state: %s", scrollState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            PLog.d(TAG, "dx: %d, dy: %d", dx, dy);
            if (Math.abs(dy) >= totalView.getHeight()) {
                if (dy < 0) { //scrolling up
                    //animate totalView into view
                    totalView.setVisibility(View.VISIBLE);
                    totalView.setTranslationY(0);
                } else { //scrolling down
                    //animate totalView out of view;
                    totalView.animate().translationY(-totalView.getHeight())
                            .setInterpolator(new DecelerateInterpolator())
                            .setDuration(200)
                            .setListener(new AnimatorListenerImpl(totalView, true)).start();
                }
            }
        }
    }

    private static class AnimatorListenerImpl implements Animator.AnimatorListener {

        private final View target;
        private final boolean hideOnComplete;

        public AnimatorListenerImpl(View target, boolean hideOnComplete) {
            this.target = target;
            this.hideOnComplete = hideOnComplete;
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            target.setVisibility(hideOnComplete ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
