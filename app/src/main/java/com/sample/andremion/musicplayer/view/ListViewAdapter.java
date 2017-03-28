package com.sample.andremion.musicplayer.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.listener.MyItemClickListener;
import com.sample.andremion.musicplayer.listener.MyItemLongClickListener;
import com.sample.andremion.musicplayer.listener.MyItemOnFocusChangeListener;
import com.sample.andremion.musicplayer.model.MediaEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/28.
 */

public class ListViewAdapter extends BaseAdapter {
    private List<MediaEntity> mValues;

    private List<Integer> listId = new ArrayList<>();

    private MyItemClickListener mItemClickListener;
    private MyItemLongClickListener mItemLongClickListener;
    private MyItemOnFocusChangeListener itemOnFocusChangeListener;
    private LayoutInflater inflater;

    public ListViewAdapter(List<MediaEntity> items, Context mContext) {
        mValues = items;
        this.inflater = LayoutInflater.from(mContext);
        init();
    }

    private void init() {
        listId.add(R.drawable.album_cover_death_cab);
        listId.add(R.drawable.album_cover_the_1975);
        listId.add(R.drawable.album_cover_pinback);
        listId.add(R.drawable.album_cover_soad);
        listId.add(R.drawable.album_cover_two_door);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return mValues.get(position).id;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //加载布局为一个视图

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_list_item, parent, false);
        view.setFocusable(true);
        MyViewHolder mm = new MyViewHolder(view, mItemClickListener, mItemLongClickListener, itemOnFocusChangeListener, position);
        MediaEntity mediaEntity = null;
        if (mValues.size() != 0) {
            mediaEntity = mValues.get(position);
            Log.e("RecyclerViewAdapter", "专辑封面:" + mediaEntity.getAlbums());
            Drawable img = Drawable.createFromPath(mediaEntity.getAlbums());
            if (img == null) {
                mm.mCoverView.setImageResource(listId.get(1));
            } else {
                mm.mCoverView.setImageDrawable(img);
            }
            mm.mTitleView.setText(mediaEntity.getDisplay_name());
            mm.mArtistView.setText(mediaEntity.getArtist());
            mm.mDurationView.setText(mediaEntity.getDurationStr());
        }
        return view;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(MyItemLongClickListener listener) {
        this.mItemLongClickListener = listener;
    }

    public void setOnFocusChangeListener(MyItemOnFocusChangeListener listener) {
        this.itemOnFocusChangeListener = listener;
    }

    public class MyViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnFocusChangeListener {

        public View mView;
        public ImageView mCoverView;
        public TextView mTitleView;
        public TextView mArtistView;
        public TextView mDurationView;
        public int mPosition;
        public MediaEntity mItem;
        private MyItemClickListener mListener;
        private MyItemLongClickListener mLongClickListener;
        private MyItemOnFocusChangeListener itemOnFocusChangeListener;

        public MyViewHolder(View view, MyItemClickListener listener, MyItemLongClickListener longClickListener, MyItemOnFocusChangeListener onFocusChangeListener, int position) {
            mView = view;
            mPosition = position;
            mCoverView = (ImageView) view.findViewById(R.id.cover);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mArtistView = (TextView) view.findViewById(R.id.artist);
            mDurationView = (TextView) view.findViewById(R.id.duration);
            this.mListener = listener;
            this.mLongClickListener = longClickListener;
            this.itemOnFocusChangeListener = onFocusChangeListener;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            view.setOnFocusChangeListener(this);
        }


        /**
         * 点击监听
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, mPosition);
            }
        }

        /**
         * 长按监听
         */
        @Override
        public boolean onLongClick(View arg0) {
            if (mLongClickListener != null) {
                mLongClickListener.onItemLongClick(arg0, mPosition);
            }
            return true;
        }

        /**
         * 焦点监听
         */
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (itemOnFocusChangeListener != null) {
                itemOnFocusChangeListener.onFocusChange(v, hasFocus);
            }
        }

    }
}
