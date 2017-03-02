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

package com.sample.andremion.musicplayer.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sample.andremion.musicplayer.model.MediaEntity;

import java.util.ArrayList;
import java.util.List;

public class PlayerService extends Service {

    private static final String TAG = PlayerService.class.getSimpleName();
    List<MediaEntity> mListMedia = new ArrayList<>();
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private int musicIndex = 0;
    public static MediaPlayer mp = new MediaPlayer();

    public PlayerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mp != null) {
            mp.release();
            Log.e("1231","12321");
        }
        return super.onUnbind(intent);
    }


    public int getPosition() {
        if (mp != null && mListMedia.size() != 0) {
            try {
                return mp.getCurrentPosition();
            } catch (Exception e) {
                Log.e("Exception", "" + e);
            }

        }
        return 0;
    }

    public int getDuration() {
        if (mListMedia.size() != 0) {
            return mListMedia.get(musicIndex).getDuration();
        } else {
            return 0;
        }
    }

    public String getDisplayName() {
        if (mListMedia.size() != 0) {
            return mListMedia.get(musicIndex).getDisplay_name();
        }
        return "空";
    }

    public String getDisplayAuthor() {
        if (mListMedia.size() != 0) {
            return mListMedia.get(musicIndex).getArtist();
        }
        return "空";
    }

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public PlayerService getService() {
            // Return this instance of PlayerService so clients can call public methods
            return PlayerService.this;
        }
    }

    public void play() {
        try{
            mp.start();
        }catch (Exception e){
            Log.e("niu",""+e);
        }
    }

    public void pause() {
        if (mp == null){
            Log.e(TAG,"播放器未初始化");
        }else {
            try{
                mp.pause();
            }catch (Exception e){
                Log.e(TAG,""+e);
            }
        }
    }

    public void stop() {
        if (mp != null) {
            mp.stop();
            try {
                mp.prepare();
                mp.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void nextMusic() {
        if (mp != null && musicIndex < mListMedia.size()) {
            mp.stop();
            try {
                mp.reset();
                mp.setDataSource(mListMedia.get(musicIndex + 1).getPath());
                musicIndex++;
                mp.prepare();
                mp.seekTo(0);
                mp.start();
            } catch (Exception e) {
                Log.d("hint", "can't jump next music");
                e.printStackTrace();
            }
        }
    }

    public void preMusic() {
        if (mp != null && musicIndex > 0) {
            mp.stop();
            try {
                mp.reset();
                mp.setDataSource(mListMedia.get(musicIndex - 1).getPath());
                musicIndex--;
                mp.prepare();
                mp.seekTo(0);
                mp.start();
            } catch (Exception e) {
                Log.d("hint", "can't jump pre music");
                e.printStackTrace();
            }
        }
    }

    public void update(List<MediaEntity> listMedia, int mMusicIndex) {
        mListMedia = new ArrayList<>();
        mListMedia.addAll(listMedia);
        musicIndex = mMusicIndex;
//        Log.e("hint", "" + mListMedia.get(musicIndex).getPath());
        try {
            mp.reset();
            mp.setDataSource(mListMedia.get(musicIndex).getPath());
            mp.prepare();
        } catch (Exception e) {
            Log.e("hint", "can't get to the song");
            e.printStackTrace();
        }
    }
}
