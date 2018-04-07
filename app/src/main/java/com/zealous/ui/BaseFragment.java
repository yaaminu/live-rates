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
 * A base fragment that takes care of injecting views using
 * butter knife.
 * <p>
 * <p>
 * <p>
 * All subclasses can attach an instance of {@link BasePresenter} in line
 * with the MVP architecture guideline. It takes care of notifying all presenters
 * attached to it about it's lifecycle so subclasses need not worry
 * <p>
 * <p>
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

    /**
     * Checks whether view is destroyed or not. This method will return true between the
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} and {@link #onDestroyView()}. In
     * all other cases, it returns false
     *
     * @return true if view is destroyed  false otherwise
     */
    protected boolean isViewDestroyed() {
        return viewIsDestroyed;
    }

    /**
     * @return the layout for this fragment.
     */
    @LayoutRes
    protected abstract int getLayout();

    /**
     * @return the presenter attached to this fragment if any
     */
    @Nullable
    protected BasePresenter<?> getBasePresenter() {
        return null;
    }
}
