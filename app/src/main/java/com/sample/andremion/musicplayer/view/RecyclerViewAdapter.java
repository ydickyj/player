/*
 * Copyright (c) 2016. André Mion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.andremion.musicplayer.view;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.model.MediaEntity;
import com.sample.andremion.musicplayer.music.MusicContent.MusicItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

//    private final List<MusicItem> mValues;

    private final List<MediaEntity> mValues;

    private List<Integer> listId = new ArrayList<>();

    public RecyclerViewAdapter(List<MediaEntity> items) {
        mValues = items;
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_list_item, parent, false);
        view.setFocusable(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int max=5;
        int min=0;
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;
        holder.mItem = mValues.get(position);
        Log.e("tag",holder.mItem.getAlbums()+"");
        Drawable img = Drawable.createFromPath(holder.mItem.getAlbums());
        if (holder.mItem.getAlbums()==null){
            holder.mCoverView.setImageResource(listId.get(1));
        }else {
            holder.mCoverView.setImageDrawable(img);
        }
        holder.mTitleView.setText(holder.mItem.getDisplay_name());
        holder.mArtistView.setText(holder.mItem.getArtist());
        holder.mDurationView.setText(holder.mItem.getDurationStr());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nothing to do
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mCoverView;
        public final TextView mTitleView;
        public final TextView mArtistView;
        public final TextView mDurationView;
        public MediaEntity mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCoverView = (ImageView) view.findViewById(R.id.cover);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mArtistView = (TextView) view.findViewById(R.id.artist);
            mDurationView = (TextView) view.findViewById(R.id.duration);
        }
    }


}
