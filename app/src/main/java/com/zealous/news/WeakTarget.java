package com.zealous.news;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.zealous.utils.PLog;

import java.lang.ref.WeakReference;

/**
 * Created by yaaminu on 4/28/17.
 */
public class WeakTarget implements Target {
    private static final String TAG = "WeakTarget";
    private final WeakReference<ImageView> imageViewWeakReference;

    public WeakTarget(ImageView thumbnail) {
        imageViewWeakReference = new WeakReference<>(thumbnail);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        PLog.d(TAG, "bitmap loaded from %s", from);
        ImageView imageView = imageViewWeakReference.get();
        if (imageView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!imageView.isAttachedToWindow()) {
                    return;
                }
            }
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        PLog.d(TAG, "failed to load thumbnail");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        PLog.d(TAG, "preparing to load thumbnail");
    }
}
