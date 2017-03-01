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

package com.sample.andremion.musicplayer.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.model.MediaEntity;
import com.sample.andremion.musicplayer.music.MusicContent;
import com.sample.andremion.musicplayer.musicUtils.utils;
import com.sample.andremion.musicplayer.view.ProgressView;
import com.sample.andremion.musicplayer.view.RecyclerViewAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.content_list)
public class MainActivity extends PlayerActivity {
    String TAG = "WWWWWW";
    @ViewById(R.id.cover)
    View mCoverView;
    @ViewById(R.id.title)
    View mTitleView;
    @ViewById(R.id.time)
    View mTimeView;
    @ViewById(R.id.duration)
    View mDurationView;
    @ViewById(R.id.progress)
    View mProgressView;
    @ViewById(R.id.fab)
    View mFabView;
    @ViewById(R.id.tracks)
    RecyclerView recyclerView;
    @ViewById
    TextView displayName;
    @ViewById
    TextView displayAuthor;
    private final static int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE=0x123;
    private List<MediaEntity> mListMedia = new ArrayList<>();
    public RecyclerViewAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the recycler adapter

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @AfterViews
    void afterView(){
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerViewAdapter(mListMedia);
        recyclerView.setAdapter(mAdapter);
        checkPermission();
    }

    @Background
    void scanMusicFile(){
        Log.e(TAG,"扫描中");
        mListMedia.clear();
        mListMedia.addAll(utils.getAllMediaList(getApplicationContext(),null));
        Log.e(TAG,"扫描结束");
        update();
    }

    @UiThread
    void update(){
        mAdapter.notifyDataSetChanged();
        if (mBound){
            update(mListMedia,0);
        }else {
            bindService();
        }
//        displayName.setText(mListMedia.get(0).getDisplay_name());
//        displayAuthor.setText(mListMedia.get(0).getArtist());
//        ((ProgressView) mProgressView).setMax(mListMedia.get(0).getDuration()/1000);
//        ((TextView)mDurationView).setText(mListMedia.get(0).getDurationStr());
    }

    @Click
    public void fab(View view) {
        //noinspection unchecked
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                new Pair<>(mCoverView, ViewCompat.getTransitionName(mCoverView)),
                new Pair<>(mTitleView, ViewCompat.getTransitionName(mTitleView)),
                new Pair<>(mTimeView, ViewCompat.getTransitionName(mTimeView)),
                new Pair<>(mDurationView, ViewCompat.getTransitionName(mDurationView)),
                new Pair<>(mProgressView, ViewCompat.getTransitionName(mProgressView)),
                new Pair<>(mFabView, ViewCompat.getTransitionName(mFabView)));
        ActivityCompat.startActivity(this, new Intent(this, DetailActivity_.class), options.toBundle());
    }

//    判断权限
    void checkPermission(){
        if(Build.VERSION.SDK_INT >= 23){//判断当前系统的版本
            int checkWriteStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//获取系统是否被授予该种权限
            if(checkWriteStoragePermission != PackageManager.PERMISSION_GRANTED){//如果没有被授予
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE);
            }else{
                scanMusicFile();//定义好的获取权限后的处理的事件
            }
        }else {
            scanMusicFile();
        }
    }
    @Background
    void bindService(){
        while(!mBound){
        }
        update(mListMedia,0);
    }
}
