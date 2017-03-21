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
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.listener.ProgressListener;
import com.sample.andremion.musicplayer.listener.VisualizerListener;
import com.sample.andremion.musicplayer.model.MediaEntity;
import com.sample.andremion.musicplayer.music.PlayerService;
import com.sample.andremion.musicplayer.musicUtils.Utils;
import com.sample.andremion.musicplayer.view.LyricView;
import com.sample.andremion.musicplayer.view.MyCycleView;
import com.sample.andremion.musicplayer.view.ProgressView;

import java.io.File;
import java.util.List;
import java.util.Objects;

public abstract class PlayerActivity extends AppCompatActivity {

    public boolean mBound = false;
    private PlayerService mService;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to PlayerService, cast the IBinder and get PlayerService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
//            Log.e("12312",""+mService);
            mBound = true;
            onBind();
        }

        @Override
        public void onServiceDisconnected(ComponentName classname) {
            mBound = false;
            onUnbind();
        }
    };
    private TextView mTimeView;
    private TextView mDurationView;
    private TextView mName;
    private TextView mAuthor;
    private ProgressView mProgressView;
    private LyricView mLyricView;
    private MyCycleView mMusicCoverView;
    private String mOldName;
    private String mOldAlbums;
    private boolean isRepeat;
    private boolean isRandom;
    private int msgWhat = 0;
    private final Handler mUpdateProgressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    final int position = mService.getPosition();
                    final int mDuration = mService.getDuration();
//                    Log.e("position:", "" + position + " Duration   " + mDuration);
                    final String name = mService.getDisplayName();
                    final String author = mService.getDisplayAuthor();
                    onUpdateProgress(position, mDuration, name, author, mService.getMusicAlbums());
                    mUpdateProgressHandler.sendEmptyMessageDelayed(msgWhat, DateUtils.SECOND_IN_MILLIS);
                    break;
                case 1:
                    final int endPosition = mService.getPosition();
                    final int endDuration = mService.getDuration();
                    final String endName = mService.getDisplayName();
                    final String endAuthor = mService.getDisplayAuthor();
                    Log.e("position:", "end");
//                    Log.e("position:", "" + endPosition + " Duration   " + endDuration);
                    onUpdateProgress(endPosition, endDuration, endName, endAuthor, mService.getMusicAlbums());
                    break;
            }
            return false;
        }
    });

    private void onUpdateProgress(int position, int duration, String name, String author, String albums) {
        if (mTimeView != null) {
            mTimeView.setText(Utils.IntToStrTime(position));
        }
        if (mDurationView != null) {
//            Log.e("12312",""+Utils.IntToStrTime((duration/ 1000))+ "   "+duration);
            mDurationView.setText(Utils.IntToStrTime(duration));
        }
        if (mProgressView != null) {
//            Log.e("123", "mProgressView");
            mProgressView.setMax(duration);
            mProgressView.setProgress(position);
        }
        if (mLyricView != null) {
            File lrcFile;
            if (!(getCurrentName().length() < 4)) {

                //                Log.e("getCurrentName", "" + (getCurrentName().length()));

                isFolderExists(Environment.getExternalStorageDirectory().toString() + "/Music/Lrc/");

                lrcFile = new File(Environment.getExternalStorageDirectory().toString() + "/Music/Lrc/" + name.substring(0, (name.length() - 4)) + ".lrc");
                if (lrcFile.exists()) {
                    if (!Objects.equals(mOldName, lrcFile.getName())) {
                        Log.e("TAG", "setLyricFile");
                        mLyricView.setLyricFile(lrcFile);
                        mOldName = lrcFile.getName();
                    }
                } else {
                    mOldName = null;
                    mLyricView.setLyricFile(null);
                }
                mLyricView.setCurrentTimeMillis(position);
            }
//            Log.e("TAG", lrcFile.exists() + "");
        }
        if (mAuthor != null) {
            mAuthor.setText(author);
        }
        if (mName != null) {
            mName.setText(name);
        }
        if (mMusicCoverView != null) {
            if (!Objects.equals(mOldAlbums, albums)) {
                mOldAlbums = albums;
                Matrix matrix = new Matrix();
                matrix.postScale(6, 6);
                Drawable img = Drawable.createFromPath(albums);
                if (img == null) {
                    mMusicCoverView.setImageDrawable(getResources().getDrawable(R.drawable.album_cover_the_1975));
                } else {
                    mMusicCoverView.setImageDrawable(img);
                }
//                mMusicCoverView.setImageMatrix(matrix);
            } else if (albums == null) {
                mMusicCoverView.setImageDrawable(getResources().getDrawable(R.drawable.album_cover_the_1975));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mProgressView != null) {
            mProgressView.setProgressListener(new ProgressListener() {
                @Override
                public void onProgressListener(boolean isFinish) {
                    if (isFinish) {
//                        Log.e("playMode","isRepeat:"+isRepeat+" isRandom：" +isRandom);
                        if (isRepeat) {
                            mService.reMusic();
                        } else if (isRandom) {
                            mService.randomMusic();
                        } else {
                            mService.nextMusic();
                        }
//                        Log.e("123", "nextMusic");
                    }
                }
            });
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
        mName = (TextView) findViewById(R.id.display_name);
        mAuthor = (TextView) findViewById(R.id.display_author);
        mLyricView = (LyricView) findViewById(R.id.lyricView);
        mMusicCoverView = (MyCycleView) findViewById(R.id.my_music_cover);

//        mMusicCoverView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    @Override
    protected void onDestroy() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        mUpdateProgressHandler.removeMessages(0);
        Log.e("onDestroy", "yes");
        super.onDestroy();
    }

    private void onBind() {
        mUpdateProgressHandler.sendEmptyMessage(1);
    }

    private void onUnbind() {
        mUpdateProgressHandler.removeMessages(0);
    }

    public void play() {
        mService.play();
        msgWhat = 0;
        mUpdateProgressHandler.sendEmptyMessage(msgWhat);
    }

    public void pause() {
        mUpdateProgressHandler.removeMessages(0);
        msgWhat = 1;
        mUpdateProgressHandler.sendEmptyMessage(msgWhat);
        mService.pause();
    }

    public void next() {
        mService.nextMusic();
    }

    public void preMusic() {
        mService.preMusic();
    }

    public void forwardMusic() {
        mService.forwardMusic();
    }

    public void rewindMusic() {
        mService.rewindMusic();
    }

    public void setRepeatPlayMode(boolean isOrNo) {
        isRepeat = isOrNo;
    }

    public void setRandomPlayMode(boolean isOrNo) {
        isRandom = isOrNo;
    }

    public String getCurrentName() {
        return mService.getDisplayName();
    }

    public String getCurrentPath() {
        return mService.getPath();
    }

    public int getBandLevel(short index) {
        return (mService.getBandLeve(index) / 10) - (-150);
    }

    public int getEqualizerMax() {
        return (mService.getEqualizerMax() / 10) * 2;
    }

    public void setBandLevel(short brand, int progress) {
        mService.setBandLevel(brand, progress);
    }

    public List<Short> getReverberationNames() {
        return mService.getReverbNames();
    }

    public List<String> getReverberationVals() {
        return mService.getReverbVals();
    }

    public void setPresetReverberationPreset(int index) {
        mService.setPresetReverbPreset(index);
    }

    public boolean getEqualizerEnabled() {
        return mService.getEqualizerEnabled();
    }

    public void setEqualizerEnabled(Boolean isOpened) {
        mService.setEqualizerEnabled(isOpened);
    }

    public void setVisualizerViewListener(VisualizerListener mListener) {
        mService.setVisualizerViewListener(mListener);
    }

    public void update(List<MediaEntity> mListMedia, int dex) {
        mService.update(mListMedia, dex);
        mUpdateProgressHandler.sendEmptyMessage(1);
        Log.e("123", "1231");
    }

    public void onResumeUpdate() {
        if (mBound) {
            mUpdateProgressHandler.sendEmptyMessage(1);
        }

    }


}
