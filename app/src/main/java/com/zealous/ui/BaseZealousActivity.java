package com.zealous.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.zealous.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

/**
 * @author by yaaminu on 12/20/16.
 */
public abstract class BaseZealousActivity extends AppCompatActivity {

    private static final String TAG = "BaseZealousActivity";
    @Nullable
    @Bind(R.id.toolbar)
    protected Toolbar toolbar;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().clearFlags(FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(Integer.MIN_VALUE);
        }
        setContentView(getLayout());
        ButterKnife.bind(this);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (hasParent()) {
                //noinspection ConstantConditions
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        doCreate(savedInstanceState);
    }

    protected final void setUpStatusBarColor(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, colorRes));
        }
    }

    protected void doCreate(@Nullable Bundle savedInstanceState) {
    }

    @LayoutRes
    protected abstract int getLayout();

    protected abstract boolean hasParent();
}
