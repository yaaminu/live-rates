package com.zealous.expense;

import android.view.ViewGroup;
import android.widget.TextView;

import com.zealous.adapter.BaseAdapter;

import java.io.File;

/**
 * Created by yaaminu on 4/22/17.
 */

public class AttachmentAdapter extends BaseAdapter<AttachmentHolder, Attachment> {
    public AttachmentAdapter(Delegate<AttachmentHolder, Attachment> delegate) {
        super(delegate);
    }

    @Override
    protected void doBindHolder(AttachmentHolder holder, int position) {
        ((TextView) holder.itemView).setText(getItem(position).getTitle());
    }

    @Override
    public AttachmentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AttachmentHolder(inflater.inflate(android.R.layout.simple_list_item_1, parent, false));
    }
}
