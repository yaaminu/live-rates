package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.zealous.R;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/15/17.
 */
public class EditDateFragment extends BottomSheetDialogFragment {

    public static final String DATE = "date";
    private static final String TAG = "EditDateFragment";

    @BindView(R.id.date_picker)
    DatePicker datePicker;

    @Nullable
    private Date time;

    @Nullable
    public Date getTime() {
        return time;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.edit_date_fragment, container, false);
        ButterKnife.bind(this, view);
        setCancelable(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        time = null;
        if (bundle != null) {
            long time = bundle.getLong(DATE);
            GenericUtils.ensureConditionTrue(time > 0, "invalid time");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(time));
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }
    }

    @OnClick(R.id.done)
    void onDone() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        calendar.set(Calendar.YEAR, datePicker.getYear());
        calendar.set(Calendar.MONTH, datePicker.getMonth());
        time = calendar.getTime();
        PLog.d(TAG, "date is %s", getTime());
        getDialog().dismiss();
    }

}
