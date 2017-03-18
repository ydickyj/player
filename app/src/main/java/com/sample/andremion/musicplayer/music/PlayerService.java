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
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sample.andremion.musicplayer.listener.VisualizerListener;
import com.sample.andremion.musicplayer.model.MediaEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class PlayerService extends Service {

    private static final String TAG = PlayerService.class.getSimpleName();
    public static MediaPlayer mp = new MediaPlayer();
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    public VisualizerListener vListener;
    List<MediaEntity> mListMedia = new ArrayList<>();
    private int musicIndex = 0;
    private boolean mMediaPlayerIsReady = false;
    // 定义系统的频谱
    private Visualizer mVisualizer;
    // 定义系统的均衡器
    private Equalizer mEqualizer;
    // 定义系统的重低音控制器
    private BassBoost mBass;
    // 定义系统的预设音场控制器
    private PresetReverb mPresetReverb;
    private List<Short> reverbNames = new ArrayList<>();
    private List<String> reverbVals = new ArrayList<>();

    public PlayerService() {
//        // 初始化示波器
//        setupVisualizer();
        // 初始化均衡控制器
        setupEqualizer();
//        // 初始化重低音控制器
//        setupBassBoost();
//        // 初始化预设音场控制器
//        setupPresetReverb();
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
            // 释放所有对象
//            mVisualizer.release();
            mEqualizer.release();
            mPresetReverb.release();
//            mBass.release();
            Log.e(TAG, "释放所有对象");
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
                if (mListMedia.size() == 1 || (musicIndex + 1) == mListMedia.size()) {
                    mp.setDataSource(mListMedia.get(0).getPath());
                } else {
                    musicIndex++;
                    mp.setDataSource(mListMedia.get(musicIndex).getPath());
                }
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
                musicIndex--;
                if (musicIndex < 0) {
                    musicIndex = 0;
                }
                mp.setDataSource(mListMedia.get(musicIndex).getPath());

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
            setupEqualizer();
            setupVisualizer();
            setupPresetReverb();
            // 初始化示波器

        }
    }

    public String getMusicAlbums() {
        if (musicIndex < mListMedia.size() && mListMedia.size() != 0) {
            return mListMedia.get(musicIndex).albums;
        } else {
            return null;
        }
    }

    /**
     * 初始化频谱
     */
    private void setupVisualizer() {
        if (mMediaPlayerIsReady) {
            mVisualizer = new Visualizer(mp.getAudioSessionId());

            //设置需要转换的音乐内容长度，专业的说这就是采样，该采样值一般为2的指数倍，如64,128,256,512,1024。
            mVisualizer.setCaptureSize(256);
            // 为mVisualizer设置监听器
        /*
         * Visualizer.setDataCaptureListener(OnDataCaptureListener listener, int rate, boolean waveform, boolean fft
         *
         *      listener，表监听函数，匿名内部类实现该接口，该接口需要实现两个函数
                rate， 表示采样的周期，即隔多久采样一次，联系前文就是隔多久采样128个数据
                iswave，是波形信号
                isfft，是FFT信号，表示是获取波形信号还是频域信号
         */
            mVisualizer.setDataCaptureListener(
                    new Visualizer.OnDataCaptureListener() {
                        //这个回调应该采集的是快速傅里叶变换有关的数据
                        @Override
                        public void onFftDataCapture(Visualizer visualizer,
                                                     byte[] fft, int samplingRate) {
                            if (vListener != null) {
                                vListener.updateView(fft);
                            }
                        }

                        //这个回调应该采集的是波形数据
                        @Override
                        public void onWaveFormDataCapture(Visualizer visualizer,
                                                          byte[] waveform, int samplingRate) {
                            // 用waveform波形数据更新mVisualizerView组件
//                        mVisualizerView.updateVisualizer(waveform);
                            if (vListener != null) {
                                vListener.updateView(waveform);
                            }
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, true, true);
        }
        mVisualizer.setEnabled(true);
    }

    /**
     * 初始化均衡控制器
     */
    private void setupEqualizer() {
        if (mMediaPlayerIsReady) {
            // 以MediaPlayer的AudioSessionId创建Equalizer
            // 相当于设置Equalizer负责控制该MediaPlayer
            mEqualizer = new Equalizer(0, mp.getAudioSessionId());
            // 启用均衡控制效果
            mEqualizer.setEnabled(true);
            // 获取均衡控制器支持最小值和最大值
            final short minEQLevel = mEqualizer.getBandLevelRange()[0];//第一个下标为最低的限度范围
            short maxEQLevel = mEqualizer.getBandLevelRange()[1];  // 第二个下标为最高的限度范围
            // 获取均衡控制器支持的所有频率
            short brands = mEqualizer.getNumberOfBands();
            for (short i = 0; i < brands; i++) {
                // 显示均衡控制器的最小值
                Log.e("maxEQLevel", (minEQLevel / 100) + " dB" + " brands.size:" + brands);
                // 显示均衡控制器的最大值
                Log.e("maxEQLevel", (maxEQLevel / 100) + " dB");
            }
        } else {
            Log.e(TAG, "播放器未准备完成");
        }
    }

    public short getBandLeve(short index) {
        if (mEqualizer != null) {
            return mEqualizer.getBandLevel(index);
        } else {
            return 0;
        }
    }

    public short getEqualizerMax() {
        if (mEqualizer != null) {
            return mEqualizer.getBandLevelRange()[1];
        } else {
            return 0;
        }
    }

    public short getEqualizerMin() {
        if (mEqualizer != null) {
            return mEqualizer.getBandLevelRange()[0];
        } else {
            return 0;
        }
    }

    public void setBandLevel(short brand, int progress) {
        if (mEqualizer != null) {
            // 设置该频率的均衡值
            Log.e(TAG, (progress * 10) + "  " + mEqualizer.getBandLevelRange()[0] + "");
            mEqualizer.setBandLevel(brand,
                    (short) ((progress * 10) + mEqualizer.getBandLevelRange()[0]));
        } else {
            Log.e(TAG, "均衡器未初始化");
        }
    }

    /**
     * 初始化预设音场控制器
     */
    private void setupPresetReverb() {
        // 以MediaPlayer的AudioSessionId创建PresetReverb
        // 相当于设置PresetReverb负责控制该MediaPlayer
        mPresetReverb = new PresetReverb(0,
                mp.getAudioSessionId());
        // 设置启用预设音场控制
        mPresetReverb.setEnabled(true);

//        mPresetReverb.setPreset(reverbNames.get(arg2));
    }

    /**
     * 设置频谱动画监听器
     */
    public void setVisualizerViewListener(VisualizerListener listener) {
        this.vListener = listener;
    }

    /**
     * 初始化重低音控制器
     */
//    private void setupBassBoost() {
//        // 以MediaPlayer的AudioSessionId创建BassBoost
//        // 相当于设置BassBoost负责控制该MediaPlayer
//        mBass = new BassBoost(0, mPlayer.getAudioSessionId());
//        // 设置启用重低音效果
//        mBass.setEnabled(true);
//        bar.setMax(1000);
//        bar.setProgress(0);
//        // 为SeekBar的拖动事件设置事件监听器
//        // 设置重低音的强度
//        mBass.setStrength((short) progress);
//    }
//
//    /**
//     * 初始化预设音场控制器
//     */
//    private void setupPresetReverb() {
//        // 以MediaPlayer的AudioSessionId创建PresetReverb
//        // 相当于设置PresetReverb负责控制该MediaPlayer
//        mPresetReverb = new PresetReverb(0,
//                mPlayer.getAudioSessionId());
//        // 设置启用预设音场控制
//        mPresetReverb.setEnabled(true);
//
//        // 获取系统支持的所有预设音场
//        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
//            reverbNames.add(i);
//            reverbVals.add(mEqualizer.getPresetName(i));
//        }
//        // 使用Spinner做为音场选择工具
//        Spinner sp = new Spinner(this);
//        sp.setAdapter(new ArrayAdapter<String>(MediaPlayerTest.this,
//                android.R.layout.simple_spinner_item, reverbVals));
//        // 为Spinner的列表项选中事件设置监听器
//        sp.setOnItemSelectedListener(new Spinner
//                .OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> arg0
//                    , View arg1, int arg2, long arg3) {
//                // 设定音场
//                mPresetReverb.setPreset(reverbNames.get(arg2));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> arg0) {
//            }
//        });
//        layout.addView(sp);
//    }
    public List<Short> getReverbNames() {
        reverbNames.clear();
        if (mEqualizer != null) {
            // 获取系统支持的所有预设音场
            for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
                if (Objects.equals(mEqualizer.getPresetName(i), "Jazz") || Objects.equals(mEqualizer.getPresetName(i), "Pop") || Objects.equals(mEqualizer.getPresetName(i), "Rock")) {
                    Log.e(TAG, "bad parameter value");
                } else {
                    reverbNames.add(i);
                }
            }
            return reverbNames;
        } else {
            return null;
        }
    }

    public List<String> getReverbVals() {
        reverbVals.clear();

        if (mEqualizer != null) {
            // 获取系统支持的所有预设音场
            for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
                if (Objects.equals(mEqualizer.getPresetName(i), "Jazz") || Objects.equals(mEqualizer.getPresetName(i), "Pop") || Objects.equals(mEqualizer.getPresetName(i), "Rock")) {
                    Log.e(TAG, "bad parameter value");
                } else {
                    reverbVals.add(mEqualizer.getPresetName(i));
                }
            }
            return reverbVals;
        } else {
            return null;
        }
    }

    public void setPresetReverbPreset(int index) {
        if (mPresetReverb != null) {
            mPresetReverb.setPreset((short) index);
        }
    }

    public boolean getEqualizerEnabled() {
        return mEqualizer != null && mEqualizer.getEnabled();
    }

    public void setEqualizerEnabled(Boolean isOpened) {
        if (mEqualizer != null) {
            mEqualizer.setEnabled(isOpened);
        }
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

}
