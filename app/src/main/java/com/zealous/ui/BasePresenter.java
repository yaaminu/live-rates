package com.zealous.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zealous.utils.GenericUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Created by yaaminu on 4/8/17.
 */

public abstract class BasePresenter<T extends Screen> {

    public static final String PREF_NAME = BasePresenter.class.getName() + ".presenter.saved.state";

    public abstract void onCreate(@Nullable Bundle savedState, @NonNull T screen);

    @NonNull
    protected final Map<String, ?> getSavedState(Context context) {
        Map<String, ?> all = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getAll();
        if (all == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(all);
    }

    protected final void saveState(@NonNull Context context, @NonNull Map<String, ?> bundle) {
        GenericUtils.ensureNotNull(context, bundle);
        SharedPreferences.Editor editor = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, ((Integer) value));
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, ((Boolean) value));
            } else if (value instanceof String) {
                editor.putString(key, ((String) value));
            } else {
                throw new AssertionError("unknown type");
            }
        }
        editor.apply();
    }

    public abstract void onDestroy();

    public void onStart() {
    }

    public void onStop() {
    }

    public boolean onMenuItemClicked(int itemId) {
        return false;
    }

}
