package com.zealous.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.zealous.utils.PLog;

import java.util.List;

import butterknife.ButterKnife;

/**
 * the base class of all Adapters in this app.
 * to use it create an instance of {@link Delegate} and let your view holders subclass
 * <p/>
 * <p/>
 * avoid calling {@link #notifyDataSetChanged()} directly on this adapter as it can put it
 * into an undefined state. use {@link #notifyDataChanged} rather
 * {@link Holder}
 * author Null-Pointer on 1/23/2016.
 */
public abstract class BaseAdapter<H extends BaseAdapter.Holder, T> extends RecyclerView.Adapter<H> {

    private static final String TAG = BaseAdapter.class.getSimpleName();
    protected final Delegate<H, T> delegate;
    protected final LayoutInflater inflater;
    private List<T> items;

    public BaseAdapter(Delegate<H, T> delegate) {
        this.items = delegate.dataSet("");
        this.delegate = delegate;
        inflater = LayoutInflater.from(delegate.context());
    }

    /**
     * a hook for subclasses to notify us that they are now
     * in sync so we should refresh the  data set.
     * <p/>
     * subclasses should always call this method some  time  after they return false in
     * {@link #outOfSync()}.
     */
    protected final void onInSync() {
        notifyDataChanged("");
    }

    /**
     * returns the current item T in the data set
     *
     * @param position the  position
     * @return The current item in the data set
     * @throws ArrayIndexOutOfBoundsException if the position is out of bounds
     */
    public T getItem(int position) {
        return items.get(position);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onBindViewHolder(final H holder, int position) {
        doBindHolder(holder, position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                delegate.onItemClick(BaseAdapter.this, view, position, getItemId(position));
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                return delegate.onItemLongClick(BaseAdapter.this, view, position, getItemId(position));
            }
        });
    }

    /**
     * notify the adapter that it's dataset has changed
     * please use this  instead of {@link #notifyDataSetChanged()} for correct behaviour
     */
    public void notifyDataChanged(String constraint) {
        //the best place to do this is in notifyDataSetChanged() but its final so we cannot override it
        if (outOfSync()) {
            PLog.v(TAG, "out of sync");
            return;
        }
        items = delegate.dataSet(constraint);
        notifyDataSetChanged();
    }

    /**
     * returns true if the adapter is out sync. this is used to determine whether
     * {@link super#notifyDataSetChanged()} should be called
     *
     * @return true if the adapter is out sync, false otherwise
     */
    protected boolean outOfSync() {
        return false;
    }

    /**
     * a hook for subclasses to bind their view holder to the adapter
     *
     * @param holder   the holder returned in {@link #onBindViewHolder(Holder, int)}
     * @param position the current position in the adapter
     */
    protected abstract void doBindHolder(H holder, int position);

    /**
     * an interface that this adapter uses to delegate some duties.
     *
     * @param <T> the model class used in this adapter
     */
    public interface Delegate<H extends Holder, T> {

        Context context();

        /**
         * @param adapter  the adapter to which this view is bound
         * @param view     the current view that has been clicked
         * @param position the current position in the data set
         * @param id       the id of the current item
         */
        void onItemClick(BaseAdapter<H, T> adapter, View view, int position, long id);

        /**
         * @param adapter  the adapter to which this view is bound
         * @param view     the current view that has been clicked
         * @param position the current position in the data set
         * @param id       the id of the current item
         */
        boolean onItemLongClick(BaseAdapter<H, T> adapter, View view, int position, long id);

        /**
         * @return the dataset used to back this adapter, may not be null
         */
        @NonNull
        List<T> dataSet(String constrain);


    }

    /**
     * a {@link RecyclerView.ViewHolder} implementation that uses
     * butterKnife to bind views to it.. just subclass it and use all the nice butterKnife
     * annotations. Because they will work :P
     */
    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public Context getContext() {
            return itemView.getContext();
        }
    }

}
