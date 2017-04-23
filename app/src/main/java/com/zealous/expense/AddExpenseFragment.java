package com.zealous.expense;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ViewUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.zealous.BuildConfig;
import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.errors.ZealousException;
import com.zealous.ui.BaseFragment;
import com.zealous.ui.BasePresenter;
import com.zealous.utils.Config;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.TaskManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.zealous.utils.FileUtils.resolveContentUriToFilePath;

/**
 * Created by yaaminu on 4/14/17.
 */

public class AddExpenseFragment extends BaseFragment implements AddExpenseScreen {
    private static final String TAG = "AddExpenseFragment";
    public static final int INVALID_POSITION = -1;
    public static final String EXPENDITURE_ID = "expenditureID";
    public static final int REQUEST_CODE_TAKE_PIC = 1001;
    private static final int REQUEST_CODE_PICK_DOCUMENT = 1002;
    private static final int REQUEST_CODE_PICK_PICTURE = 1003;

    private static final SimpleDateFormat secondsPrecissionFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    @Inject
    AddExpenditurePresenter addExpenditurePresenter;
    @Inject
    ExpenditureCategoryAdapter adapter;
    @Inject
    GridLayoutManager layoutManager;
    @Inject
    AttachmentAdapter attachmentAdapter;
    @Inject
    ProgressDialog progressDialog;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.attachments)
    RecyclerView attachmentRv;
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
    private List<Attachment> attachments = Collections.emptyList();

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
            final ExpenditureCategory item = adapter.getItem(position);
            if (item == ExpenditureCategory.DUMMY_EXPENDITURE_CATEGORY) {
                return false;
            }
            showUpdateDeleteDialog(item);
            return true;
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


    private void showUpdateDeleteDialog(final ExpenditureCategory category) {
        new AlertDialog.Builder(getContext())
                .setItems(R.array.category_long_click_context_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addExpenditurePresenter.onCategoryContextMenuAction(getFragmentManager(), category, which);
                    }
                }).create().show();
    }

    private final AttachmentAdapterDelegate attachmentAdapterDelegate = new AttachmentAdapterDelegate() {
        @Override
        public Context context() {
            return getContext();
        }

        @Override
        public void onItemClick(BaseAdapter<AttachmentHolder, Attachment> adapter, View view, int position, long id) {
            addExpenditurePresenter.viewAttachment(adapter.getItem(position));
        }

        @Override
        public boolean onItemLongClick(BaseAdapter<AttachmentHolder, Attachment> adapter, View view, int position, long id) {
            showOptions(adapter.getItem(position));
            return false;
        }

        @NonNull
        @Override
        public List<Attachment> dataSet(String constrain) {
            return attachments;
        }
    };

    private void showOptions(final Attachment item) {
        new AlertDialog.Builder(getContext())
                .setItems(R.array.attach_click_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                addExpenditurePresenter.viewAttachment(item);
                                break;
                            case 1:
                                addExpenditurePresenter.removeAttachment(item);
                                break;
                            default:
                                throw new RuntimeException();
                        }
                    }
                }).create().show();
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_add_expense;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DaggerAddExpenditureComponent.builder()
                .addExpenseFragmentProvider(new AddExpenseFragmentProvider(this, delegate, attachmentAdapterDelegate))
                .build().inject(this);
        addExpenditurePresenter.onCreate(savedInstanceState, this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String id = bundle.getString(EXPENDITURE_ID);
            if (id != null) {
                addExpenditurePresenter.startWith(id);
            }
        }
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
                attachFileToExpenditure();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private void attachFileToExpenditure() {
        new AlertDialog.Builder(getContext())
                .setItems(R.array.attach_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleAttachItem(which);
                    }
                }).create().show();
    }


    private void handleAttachItem(int which) {
        Intent intent;
        switch (which) {
            case 0:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_PICTURE);
                break;
            case 1:
                addExpenditurePresenter.setCameraOutputUri(Uri.fromFile(new File(Config.getTempDir(),
                        secondsPrecissionFormatter.format(new Date()) + ".jpg")));
                takePhoto(getActivity(),
                        addExpenditurePresenter.getCameraOutputUri(),
                        REQUEST_CODE_TAKE_PIC);
                break;
            case 2:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, REQUEST_CODE_PICK_DOCUMENT);
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (addExpenditurePresenter.getCameraOutputUri() != null) {
            handleResults(REQUEST_CODE_TAKE_PIC, addExpenditurePresenter.getCameraOutputUri());
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        if (requestCode == REQUEST_CODE_PICK_DOCUMENT || requestCode == REQUEST_CODE_PICK_PICTURE || requestCode == REQUEST_CODE_TAKE_PIC) {
            if (resultCode == Activity.RESULT_OK) {
                TaskManager.executeNow(new Runnable() {
                    @Override
                    public void run() {
                        handleResults(requestCode, data.getData());
                    }
                }, false);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleResults(int requestCode, Uri data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_DOCUMENT:
                addExpenditurePresenter.addAttachment(resolveContentUriToFilePath(data, true));
                break;
            case REQUEST_CODE_PICK_PICTURE:
                addExpenditurePresenter.addAttachment(resolveContentUriToFilePath(data, true));
                break;
            case REQUEST_CODE_TAKE_PIC:
                if (data != null) {
                    addExpenditurePresenter.addAttachment(resolveContentUriToFilePath(data));
                    addExpenditurePresenter.setCameraOutputUri(null);
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    public static void takePhoto(Activity context, Uri outPutUri, int requestCode) {
        try {
            Intent attachIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            attachIntent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
            context.startActivityForResult(attachIntent, requestCode);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                PLog.e(TAG, e.getMessage(), e.getCause());
                throw new RuntimeException(e.getCause());
            }
            PLog.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        attachmentRv.setLayoutManager(new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false));
        attachmentRv.setAdapter(attachmentAdapter);
    }

    @OnClick({R.id.edit_date, R.id.edit_location})
    void onClick(View view) {
        final int position = delegate.getSelectedItemPosition();
        addExpenditurePresenter.updateData(amount.getText().toString().trim(),
                position == AdapterView.INVALID_POSITION ? ""
                        : categories.get(position).getName(), note.getText().toString().trim());
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
    public void refreshDisplay(List<ExpenditureCategory> categories,
                               long time, String location,
                               String currency, String amount, String categoryName,
                               String description, List<Attachment> attachments) {
        this.categories = categories;
        this.attachments = attachments;
        date.setText(DateUtils.formatDateTime(getContext(), time,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR));
        this.location.setText(location);
        if (!GenericUtils.isEmpty(categoryName)) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getName().equals(categoryName)) {
                    selectedItem = i;
                    break;
                }
            }
        }
        adapter.notifyDataChanged("");
        this.note.setText(description);
        this.amount.setText(amount);
        this.amount.setSelection(amount.length());
        this.currency.setText(currency);
        if (attachments.isEmpty()) {
            View view = getView();
            assert view != null;
            com.zealous.utils.ViewUtils.hideViews(ButterKnife.findById(view, R.id.attachments_pane));
        } else {
            View view = getView();
            assert view != null;
            com.zealous.utils.ViewUtils.showViews(ButterKnife.findById(view, R.id.attachments_pane));
            attachmentAdapter.notifyDataChanged("");
        }
    }

    @Override
    public void showValidationError(final String errorMessage) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getContext())
                        .setMessage(errorMessage)
                        .setPositiveButton(android.R.string.ok, null)
                        .setTitle(R.string.error)
                        .create().show();
            }
        });
    }

    @Override
    public void showValidationError(@StringRes int errorMessage) {
        showValidationError(getString(errorMessage));
    }

    void addNewExpenditure() {
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

    @Override
    public Activity getCurrentActivity() {
        return getActivity();
    }

    @Override
    public void showProgressDialog(boolean cancellable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog.setCancelable(false);
                progressDialog.setMessage(GenericUtils.getString(R.string.loading));
                progressDialog.show();
            }
        });
    }

    @Override
    public void dismissProgressDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
