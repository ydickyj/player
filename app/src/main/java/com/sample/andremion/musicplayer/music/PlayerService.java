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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerService extends Service {

    private static final String TAG = PlayerService.class.getSimpleName();
    List<MediaEntity> mListMedia = new ArrayList<>();
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private int musicIndex = 0;
    private int lyricIndex = 0;
    public static MediaPlayer mp = new MediaPlayer();
    private boolean mMediaPlayerIsReady = false;

    public PlayerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mMediaPlayerIsReady) {
            mMediaPlayerIsReady = false;
            mp.release();
            Log.e("1231", "12321");
        }
        return super.onUnbind(intent);
    }


    public int getPosition() {
        if (mMediaPlayerIsReady && mListMedia.size() != 0) {
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

    public String getPath() {
        if (mListMedia.size() != 0) {
            return mListMedia.get(musicIndex).getPath();
        } else {
            return null;
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
        if (mMediaPlayerIsReady) {
            try {
                mp.start();
            } catch (Exception e) {
                Log.e("niu", "" + e);
            }
        } else {
            Log.e(TAG, "播放器未初始化");
        }
    }

    public void pause() {
        if (!mMediaPlayerIsReady) {
            Log.e(TAG, "播放器未初始化");
        } else {
            try {
                mp.pause();
            } catch (Exception e) {
                Log.e(TAG, "" + e);
            }
        }
    }

    public void stop() {
        if (mMediaPlayerIsReady) {
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
        if (mMediaPlayerIsReady && musicIndex < mListMedia.size()) {
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

    public void reMusic() {
        if (mMediaPlayerIsReady && musicIndex < mListMedia.size()) {
            mp.stop();
            try {
                mp.reset();
                mp.setDataSource(mListMedia.get(musicIndex).getPath());
                mp.prepare();
                mp.seekTo(0);
                mp.start();
            } catch (Exception e) {
                Log.d("hint", "can't jump repeat music");
                e.printStackTrace();
            }
        }
    }


    public void randomMusic() {
        int max = mListMedia.size() - 1;
        int min = 0;
        Random random = new Random();
        musicIndex = random.nextInt(max) % (max - min + 1) + min;
        if (mMediaPlayerIsReady && musicIndex < mListMedia.size()) {
            mp.stop();
            try {
                mp.reset();
                mp.setDataSource(mListMedia.get(musicIndex).getPath());
                mp.prepare();
                mp.seekTo(0);
                mp.start();
            } catch (Exception e) {
                Log.d("hint", "can't jump random music");
                e.printStackTrace();
            }
        }
    }

    public void preMusic() {
        if (mMediaPlayerIsReady && musicIndex > 0) {
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

    public void forwardMusic() {
        int pos = mp.getCurrentPosition();
        pos += 1500; // milliseconds
        if (pos >= mp.getDuration()) {
            mp.seekTo(mp.getDuration());
        } else {
            mp.seekTo(pos);
        }
    }

    public void rewindMusic() {
        int pos = mp.getCurrentPosition();
        pos -= 1500; // milliseconds
        if (pos <= 0) {
            mp.seekTo(0);
        } else {
            mp.seekTo(pos);
        }
    }

    public void update(List<MediaEntity> listMedia, int mMusicIndex) {
        lyricIndex = 0;
        mListMedia = new ArrayList<>();
        mListMedia.addAll(listMedia);
        musicIndex = mMusicIndex;
        if (mListMedia.size() == 0) {
            mMediaPlayerIsReady = false;
        } else {
            try {
                mp = new MediaPlayer();
                mp.setDataSource(mListMedia.get(musicIndex).getPath());
                mp.prepare();
            } catch (IOException e) {
                Log.e("hint", "can't get to the song IOException" + e);
                e.printStackTrace();
            }
            mMediaPlayerIsReady = true;
        }
    }

    public void upLyricIndex(int index) {
        lyricIndex = index;
    }

    public String getMusicAlbums() {
        if (musicIndex < mListMedia.size() && mListMedia.size() != 0) {
            return mListMedia.get(musicIndex).albums;
        }else {
            return  null;
        }


    }
}
