package com.zealous.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.ui.HomeItem;

import butterknife.Bind;

/**
 * @author by yaaminu on 12/20/16.
 */

public class HomeRecyclerViewAdapter extends BaseAdapter<HomeRecyclerViewAdapter.VHolder, HomeItem> {

    public HomeRecyclerViewAdapter(Delegate delegate) {
        super(delegate);
    }

    @Override
    protected void doBindHolder(VHolder holder, int position) {
        HomeItem item = getItem(position);
        holder.itemIcon.setImageResource(item.icon);
        holder.itemTitle.setText(item.title);
        holder.itemTitle.setTextColor(ContextCompat.getColor(holder.getContext(), item.titleColor));
    }

    @Override
    public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VHolder(inflater.inflate(R.layout.home_screen_item, parent, false));
    }

    public static class VHolder extends BaseAdapter.Holder {
        @Bind(R.id.item_icon)
        ImageView itemIcon;
        @Bind(R.id.item_title)
        TextView itemTitle;

        public VHolder(View view) {
            super(view);
        }

        Context getContext() {
            return itemView.getContext();
        }
    }


    public interface Delegate extends BaseAdapter.Delegate<VHolder, HomeItem> {

    }
}
