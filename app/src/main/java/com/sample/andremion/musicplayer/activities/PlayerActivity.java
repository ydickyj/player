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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.model.MediaEntity;
import com.sample.andremion.musicplayer.music.PlayerService;
import com.sample.andremion.musicplayer.musicUtils.utils;
import com.sample.andremion.musicplayer.view.ProgressView;

import java.util.List;

public abstract class PlayerActivity extends AppCompatActivity {

    private PlayerService mService;
    public boolean mBound = false;
    private TextView mTimeView;
    private TextView mDurationView;
    private TextView mName;
    private TextView mAuthor;
    private ProgressView mProgressView;
    private final Handler mUpdateProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    final int position = mService.getPosition();
                    final int mDuration = mService.getDuration();
                    Log.e("position:",""+position+"12321   "+mDuration);
                    final String name = mService.getDisplayName();
                    final String author = mService.getDisplayAuthor();
                    onUpdateProgress(position, mDuration,name,author);
                    sendEmptyMessageDelayed(0, DateUtils.SECOND_IN_MILLIS);
                    break;
            }

        }
    };
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to PlayerService, cast the IBinder and get PlayerService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            Log.e("12312",""+mService);
            mBound = true;
            onBind();
        }

        @Override
        public void onServiceDisconnected(ComponentName classname) {
            mBound = false;
            onUnbind();
        }
    };

    private void onUpdateProgress(int position, int duration, String name, String author) {
        if (mTimeView != null) {
            mTimeView.setText(utils.IntToStrTime(position));
        }
        if (mDurationView != null) {
            Log.e("12312",""+utils.IntToStrTime((duration/ 1000))+ "   "+duration);
            mDurationView.setText(utils.IntToStrTime(duration));
        }
        if (mProgressView != null) {
            mProgressView.setMax(duration);
            mProgressView.setProgress(position);
        }
        if (mAuthor!=null){
            mAuthor.setText(author);
        }
        if (mName != null){
            mName.setText(name);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind to PlayerService
        Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mTimeView = (TextView) findViewById(R.id.time);
        mDurationView = (TextView) findViewById(R.id.duration);
        mProgressView = (ProgressView) findViewById(R.id.progress);
        mName = (TextView)findViewById(R.id.display_name);
        mAuthor=(TextView)findViewById(R.id.display_author);
    }

    @Override
    protected void onDestroy() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    private void onBind() {
        mUpdateProgressHandler.sendEmptyMessage(0);

    }

    private void onUnbind() {
        mUpdateProgressHandler.removeMessages(0);
    }

    public void play() {
        mService.play();
    }

    public void pause() {
        mService.pause();
    }

    public void update(List<MediaEntity> mListMedia,int dex){
            Log.e("123","123");
            mService.update(mListMedia,dex);
           mUpdateProgressHandler.sendEmptyMessage(0);
            Log.e("123","1231");
    }


}
