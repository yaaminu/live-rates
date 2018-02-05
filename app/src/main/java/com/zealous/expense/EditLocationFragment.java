package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.zealous.R;
import com.zealous.utils.GenericUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/15/17.
 */
public class EditLocationFragment extends BottomSheetDialogFragment {

    public static final String LOCATION = "location";
    @BindView(R.id.edit_location)
    EditText etLocation;
    @Nullable
    private String location;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.edit_location_fragment, container, false);
        ButterKnife.bind(this, view);
        setCancelable(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            location = arguments.getString(LOCATION);
            if (!GenericUtils.isEmpty(location)) {
                etLocation.setText(location);
                etLocation.setSelection(location.length());
            }
        }

    }

    @OnClick(R.id.done)
    void done() {
        final String tmp = etLocation.getText().toString().trim();
        if (!GenericUtils.isEmpty(tmp)) {
            location = tmp;
            dismiss();
        } else {
            etLocation.setError(getString(R.string.enter_location));
        }
    }


    @Nullable
    public String getLocation() {
        return location;
    }
}
