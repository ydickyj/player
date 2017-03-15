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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.listener.DialogListener;
import com.sample.andremion.musicplayer.view.BottomDialog;
import com.sample.andremion.musicplayer.view.LyricView;
import com.sample.andremion.musicplayer.view.MusicCoverView;
import com.sample.andremion.musicplayer.view.NumberProgressBar;
import com.sample.andremion.musicplayer.view.ProgressView;
import com.sample.andremion.musicplayer.view.TransitionAdapter;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;


@EActivity(R.layout.content_detail)
public class DetailActivity extends PlayerActivity {
    String TAG = DetailActivity.class.getName();
    @ViewById(R.id.music_cover)
    MusicCoverView mCoverView;
    @ViewById
    TextView displayName;
    @ViewById
    TextView displayAuthor;
    @ViewById
    LyricView lyricView;
    @ViewById(R.id.progress)
    ProgressView mProgressView;
    @ViewById
    ImageView repeat;
    @ViewById
    ImageView shuffle;
    //指定操作的文件名称
    SharedPreferences share;
    BottomDialog newBtn;
    private int repSumClick = 0;
    private int ranSumClick = 0;
    private int delfault[] = {180, 150, 150, 150, 180};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        share = getSharedPreferences("userData", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCoverView.setCallbacks(new MusicCoverView.Callbacks() {
            @Override
            public void onMorphEnd(MusicCoverView coverView) {
                // Nothing to do
                Log.e(TAG, "onMorphEnd");
            }

            @Override
            public void onRotateEnd(MusicCoverView coverView) {
                Log.e(TAG, "onRotateEnd");
                supportFinishAfterTransition();
            }
        });
        getWindow().getSharedElementEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                play();
                mCoverView.start();
                lyricView.setVisibility(View.VISIBLE);
                Log.e(TAG, "" + getPosition());
            }
        });
        newBtn = BottomDialog.create(getSupportFragmentManager()).setLayoutRes(R.layout.mixer_dialog);
        newBtn.setKeyListener(new DialogListener() {
            @Override
            public void onKey(KeyEvent event, int keyCode) {
                Log.e(TAG, "" + event.getAction() + "" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    for (int i = 0; i < 5; i++) {
                        saveBandPositionData(i);
                    }
                }
            }
        });
        newBtn.setViewListener(new BottomDialog.ViewListener() {
            @Override
            public void bindView(View v) {
                initView(v);
            }
        });
    }

    @Override
    public void onBackPressed() {
        onFabClick(null);
    }

    public void onFabClick(View view) {
        lyricView.setVisibility(View.GONE);
        pause();
        mCoverView.stop();
    }

    @Click(R.id.repeat)
    void repeat() {
        repSumClick++;
        Log.e(TAG, "repSumClick" + repSumClick);
        if (repSumClick > 1) {
            repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_white_24dp));
            setRepeatPlayMode(false);
            setRandomPlayMode(false);
            repSumClick = 0;

        } else {
            repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_white_pressed24dp));
            shuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_white_24dp));
            setRepeatPlayMode(true);
            setRandomPlayMode(false);
            ranSumClick = 0;
        }
        Log.e(TAG, "repSumClick" + repSumClick);
    }

    @Click(R.id.shuffle)
    void shuffle() {
        ranSumClick++;
        Log.e(TAG, "ranSumClick" + ranSumClick);
        if (ranSumClick > 1) {
            shuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_white_24dp));
            setRandomPlayMode(false);
            setRepeatPlayMode(false);
            ranSumClick = 0;
        } else {
            shuffle.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_white_pressed24dp));
            repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_white_24dp));
            setRandomPlayMode(true);
            setRepeatPlayMode(false);
            repSumClick = 0;
        }
        Log.e(TAG, "ranSumClick" + ranSumClick);
    }

    @Click(R.id.previous)
    void previousSong() {
        preMusic();
    }

    @Click(R.id.rewind)
    void rewindSong() {
        rewindMusic();
    }

    @Click(R.id.forward)
    void forward() {
        forwardMusic();
    }

    @Click(R.id.next)
    void nextMusic() {
        next();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                newBtn.show();
                break;
            case KeyEvent.KEYCODE_BACK:
                newBtn.onDestroyView();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void saveBandPositionData(int index) {
        SharedPreferences.Editor edit = share.edit(); //编辑文件
        edit.putInt("band" + index, getBandLevel((short) index));         //根据键值对添加数据
        edit.apply();  //保存数据信息
    }

    private void initView(View v) {

        int position = share.getInt("spinner", 0);
        for (int i = 0; i < 5; i++) {
            int bandPosition = share.getInt("band" + i, -1);
            if (bandPosition != -1) {
                setBandLevel((short) i, bandPosition);
            } else {
                setBandLevel((short) i, getBandLevel((short) i));
                SharedPreferences.Editor edit = share.edit(); //编辑文件
                edit.putInt("band" + i, getBandLevel((short) i));         //根据键值对添加数据
                edit.apply();  //保存数据信息
            }
        }
        Spinner mSpinner = (Spinner) v.findViewById(R.id.mixer_sp);

        Switch mSwitch = (Switch) v.findViewById(R.id.switch_eq);
        Button mResetBtn = (Button) v.findViewById(R.id.reset_btn);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEqualizerEnabled(isChecked);
            }
        });
        mSwitch.setChecked(getEqualizerEnabled());
        mSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getReverbVals()));
//        mSpinner.setAdapter(new SimpleAdapter(this,listems,R.layout.mixer_sp_item,new String[]{"name"},new int[]{R.id.tv_spinner}));
        final NumberProgressBar bassProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_bar_bass);
        int eqMax = getEqualizerMax();
        int bandLevel = getBandLevel((short) 0);
        bassProgress.setMax(eqMax);
        bassProgress.setProgress(bandLevel);
        Button bassAddBtn = (Button) v.findViewById(R.id.add_button_bass);
        Button bassReduceBtn = (Button) v.findViewById(R.id.reduce_button_bass);


        final NumberProgressBar mediumProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_bar_medium_bass);
        Button mediumAddBtn = (Button) v.findViewById(R.id.add_button_medium_bass);
        Button mediumReduceBtn = (Button) v.findViewById(R.id.reduce_button_medium_bass);
        mediumProgress.setMax(getEqualizerMax());
        mediumProgress.setProgress(getBandLevel((short) 1));

        final NumberProgressBar mediantProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_mediant);
        Button mediantAddBtn = (Button) v.findViewById(R.id.add_button_mediant);
        Button mediantReduceBtn = (Button) v.findViewById(R.id.reduce_button_mediant);
        mediantProgress.setMax(getEqualizerMax());
        mediantProgress.setProgress(getBandLevel((short) 2));

        final NumberProgressBar msProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_bar_mezzo_soprano);
        Button msAddBtn = (Button) v.findViewById(R.id.add_button_mezzo_soprano);
        Button msReduceBtn = (Button) v.findViewById(R.id.reduce_button_mezzo_soprano);
        msProgress.setMax(getEqualizerMax());
        msProgress.setProgress(getBandLevel((short) 3));

        final NumberProgressBar altProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_alt);
        Button altAddBtn = (Button) v.findViewById(R.id.add_button_alt);
        Button altReduceBtn = (Button) v.findViewById(R.id.reduce_alt);
        altProgress.setMax(getEqualizerMax());
        altProgress.setProgress(getBandLevel((short) 4));

        mSpinner.setSelection(position);
        setPresetReverbPreset(getReverbNames().get(position));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setPresetReverbPreset(getReverbNames().get(position));
                SharedPreferences.Editor edit = share.edit(); //编辑文件
                edit.putInt("spinner", position);         //根据键值对添加数据
                edit.apply();  //保存数据信息
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 5; i++) {
                    setBandLevel((short) i, delfault[i]);
                }
                bassProgress.setProgress(getBandLevel((short) 0));
                mediumProgress.setProgress(getBandLevel((short) 1));
                mediantProgress.setProgress(getBandLevel((short) 2));
                msProgress.setProgress(getBandLevel((short) 3));
                altProgress.setProgress(getBandLevel((short) 4));
            }
        });
        bassAddBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int newPosition = bassProgress.getProgress();
                if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = bassProgress.getProgress() + 1;
                    bassProgress.setProgress(newPosition);
                    Log.e(TAG, event.getAction() + "  " + keyCode + "  " + newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 0, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });
        final boolean[] isOneClicked = {true};
        bassReduceBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e(TAG, event.getAction() + "  " + keyCode + " " + isOneClicked[0]);
                int newPosition = bassProgress.getProgress();
                if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_UP) {
                    if (isOneClicked[0]) {
                        newPosition = bassProgress.getProgress() + 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = bassProgress.getProgress() + 3;
                    }
                    bassProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_UP) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 0, newPosition);//手指抬起时停止发送

                } else if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_DOWN) {
                    if (isOneClicked[0]) {
                        newPosition = bassProgress.getProgress() - 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = bassProgress.getProgress() - 3;
                    }
                    bassProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_DOWN) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 0, newPosition);    //手指抬起时停止发送
                    saveBandPositionData((short) 0);
                }
                return false;
            }
        });

//        medium音段按钮
//        mediumAddBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = mediumProgress.getProgress() + 1;
//                mediumProgress.setProgress(newPosition);
//                setBandLevel((short) 1, newPosition);
//            }
//        });
//        mediumReduceBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = mediumProgress.getProgress() - 1;
//                mediumProgress.setProgress(newPosition);
//                setBandLevel((short) 1, newPosition);
//            }
//        });
        mediumAddBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int newPosition = mediumProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = mediumProgress.getProgress() + 1;
                    mediumProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 1, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });
        mediumReduceBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e(TAG, event.getAction() + "  " + keyCode);
                int newPosition = mediumProgress.getProgress();
                if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_UP) {
                    if (isOneClicked[0]) {
                        newPosition = mediumProgress.getProgress() + 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = mediumProgress.getProgress() + 3;
                    }
                    mediumProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_UP) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 1, newPosition);    //手指抬起时停止发送
                    saveBandPositionData((short) 1);
                } else if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_DOWN) {
                    if (isOneClicked[0]) {
                        newPosition = mediumProgress.getProgress() - 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = mediumProgress.getProgress() - 3;
                    }
                    mediumProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_DOWN) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 1, newPosition);    //手指抬起时停止发送
                    saveBandPositionData((short) 1);
                }
                return false;
            }
        });

//        mediant音段按钮
//        mediantAddBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = mediantProgress.getProgress() + 1;
//                mediantProgress.setProgress(newPosition);
//                setBandLevel((short) 2, newPosition);
//            }
//        });
//        mediantReduceBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = mediantProgress.getProgress() - 1;
//                mediantProgress.setProgress(newPosition);
//                setBandLevel((short) 2, newPosition);
//            }
//        });
        mediantAddBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int newPosition = mediantProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = mediantProgress.getProgress() + 1;
                    mediantProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 2, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });
        mediantReduceBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e(TAG, event.getAction() + "  " + keyCode);
                int newPosition = mediantProgress.getProgress();
                if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_UP) {
                    if (isOneClicked[0]) {
                        newPosition = mediantProgress.getProgress() + 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = mediantProgress.getProgress() + 3;
                    }
                    mediantProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_UP) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 2, newPosition);    //手指抬起时停止发送
                } else if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_DOWN) {
                    if (isOneClicked[0]) {
                        newPosition = mediantProgress.getProgress() - 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = mediantProgress.getProgress() - 3;
                    }
                    mediantProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_DOWN) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 2, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });

//        ms音段按钮
//        msAddBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = msProgress.getProgress() + 1;
//                msProgress.setProgress(newPosition);
//                setBandLevel((short) 3, newPosition);
//            }
//        });
//        msReduceBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = msProgress.getProgress() - 1;
//                msProgress.setProgress(newPosition);
//                setBandLevel((short) 3, newPosition);
//            }
//        });
        msAddBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int newPosition = msProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = msProgress.getProgress() + 1;
                    msProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 3, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });
        msReduceBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e(TAG, event.getAction() + "  " + keyCode);
                int newPosition = msProgress.getProgress();
                if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_UP) {
                    if (isOneClicked[0]) {
                        newPosition = msProgress.getProgress() + 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = msProgress.getProgress() + 3;
                    }
                    msProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_UP) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 3, newPosition);    //手指抬起时停止发送
                } else if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_DOWN) {
                    if (isOneClicked[0]) {
                        newPosition = msProgress.getProgress() - 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = msProgress.getProgress() - 3;
                    }
                    msProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_DOWN) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 3, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });

//        alt音段按钮
//        altAddBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = altProgress.getProgress() + 1;
//                altProgress.setProgress(newPosition);
//                setBandLevel((short) 4, newPosition);
//            }
//        });
//        altReduceBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int newPosition = altProgress.getProgress() - 1;
//                altProgress.setProgress(newPosition);
//                setBandLevel((short) 4, newPosition);
//            }
//        });
        altAddBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int newPosition = altProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = altProgress.getProgress() + 1;
                    altProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 4, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });

        altReduceBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e(TAG, event.getAction() + "  " + keyCode);
                int newPosition = altProgress.getProgress();
                if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_UP) {
                    if (isOneClicked[0]) {
                        newPosition = altProgress.getProgress() + 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = altProgress.getProgress() + 3;
                    }
                    altProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_UP) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 4, newPosition);    //手指抬起时停止发送
                } else if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_DOWN) {
                    if (isOneClicked[0]) {
                        newPosition = altProgress.getProgress() - 1;
                        isOneClicked[0] = false;
                    } else {
                        newPosition = altProgress.getProgress() - 3;
                    }
                    altProgress.setProgress(newPosition);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_DOWN) {
                    isOneClicked[0] = true;
                    setBandLevel((short) 4, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });
    }

}
