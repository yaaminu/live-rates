package com.zealous.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by yaaminu on 4/8/17.
 */

public abstract class BasePresenter<T extends Screen> {

    public abstract void onCreate(@Nullable Bundle savedState, @NonNull T screen);

    public void saveState(@NonNull Bundle bundle) {

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
