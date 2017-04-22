package com.zealous.expense;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import org.w3c.dom.Text;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/22/17.
 */
public class AttachmentHolder extends BaseAdapter.Holder {

    @Bind(R.id.preview)
    ImageView preview;
    @Bind(R.id.title)
    TextView title;

    public AttachmentHolder(View itemView) {
        super(itemView);
    }
}
