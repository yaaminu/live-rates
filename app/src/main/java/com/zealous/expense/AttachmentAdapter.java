package com.zealous.expense;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.ViewGroup;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

/**
 * Created by yaaminu on 4/22/17.
 */

public class AttachmentAdapter extends BaseAdapter<AttachmentHolder, Attachment> {
    private final LruCache<String, Bitmap> imageCache;

    public AttachmentAdapter(Delegate<AttachmentHolder, Attachment> delegate) {
        super(delegate);
        imageCache = new LruCache<>((int) (Runtime.getRuntime().freeMemory() / 8));
    }

    @Override
    protected void doBindHolder(AttachmentHolder holder, int position) {
        Attachment item = getItem(position);
        holder.title.setText(GenericUtils.getString(R.string.attachment_title, position + 1));
        if (itsImage(item)) {
            Bitmap bm = imageCache.get(item.getSha1Sum());
            if (bm == null) {
                holder.preview.setImageResource(item.getPlaceHolderIcon());
                loadImage(item, holder);
            } else {
                holder.preview.setImageBitmap(bm);
            }
        } else {
            holder.preview.setImageResource(item.getPlaceHolderIcon());
        }
    }

    private boolean itsImage(Attachment item) {
        return item.getMimeType().startsWith("image/");
    }

    private void loadImage(Attachment item, final AttachmentHolder holder) {
        final byte[] data = item.getBlob();
        final String sha1Sum = item.getSha1Sum();
        TaskManager.executeNow(new Runnable() {
            @Override
            public void run() {
                if (ThreadUtils.isMainThread()) {
                    notifyItemChanged(holder.getAdapterPosition());
                } else {
                    imageCache.put(sha1Sum, BitmapFactory.decodeByteArray(data, 0, data.length));
                    TaskManager.executeOnMainThread(this);
                }
            }
        }, false);
    }

    @Override
    public AttachmentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AttachmentHolder(inflater.inflate(R.layout.attachment_list_item, parent, false));
    }
}
