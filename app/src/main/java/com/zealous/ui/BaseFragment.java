package com.zealous.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by yaaminu on 12/20/16.
 */
public abstract class BaseFragment extends Fragment {

    private boolean viewIsDestroyed;

    @Nullable
    Unbinder unbinder;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layout = getLayout();
        View view = null;
        if (layout != 0) {
            view = inflater.inflate(layout, container, false);
            unbinder = ButterKnife.bind(this, view);
        }
        viewIsDestroyed = false;
        return view;
    }


    @Override
    public void onStop() {
        BasePresenter<?> presenter = getBasePresenter();
        if (presenter != null) {
            presenter.onStop();
        }
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        BasePresenter<?> presenter = getBasePresenter();
        if (presenter != null) {
            presenter.onStart();
        }
    }

    @Override
    public void onDestroy() {
        BasePresenter<?> presenter = getBasePresenter();
        if (presenter != null) {
            presenter.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        viewIsDestroyed = true;
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    protected boolean isViewDestroyed() {
        return viewIsDestroyed;
    }

    @LayoutRes
    protected abstract int getLayout();

    @Nullable
    protected BasePresenter<?> getBasePresenter() {
        return null;
    }
}
