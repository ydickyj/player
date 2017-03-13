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

import android.os.Bundle;

import android.transition.Transition;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.view.LyricView;
import com.sample.andremion.musicplayer.view.MusicCoverView;
import com.sample.andremion.musicplayer.view.NumberProgressBar;
import com.sample.andremion.musicplayer.view.ProgressView;
import com.sample.andremion.musicplayer.view.TransitionAdapter;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import me.shaohui.bottomdialog.BottomDialog;

import static android.view.KeyEvent.*;


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

    private int repSumClick = 0;
    private int ranSumClick = 0;
    BottomDialog newBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                BottomDialog m = BottomDialog.create(getSupportFragmentManager()).setLayoutRes(R.layout.mixer_dialog);
                m.setViewListener(new BottomDialog.ViewListener() {
                    @Override
                    public void bindView(View v) {
                        initView(v);
                    }
                });
                m.show();
                break;
        }

        return super.onKeyDown(keyCode, event);

    }

    private void initView(View v) {
        TextView mE = (TextView) v.findViewById(R.id.eq);
        Spinner mSpinner = (Spinner) v.findViewById(R.id.mixer_sp);
        List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();
        HashMap map = new HashMap();
        map.put("name","556665566123");
        listems.add(map);
        HashMap mapOne = new HashMap();
        mapOne.put("name","1231");
        listems.add(mapOne);
        for (int i = 0 ;i<10;i++){
            mapOne = new HashMap();
            mapOne.put("name","你好"+i);
            listems.add(mapOne);
        }
        mSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getReverbVals()));

//        mSpinner.setAdapter(new SimpleAdapter(this,listems,R.layout.mixer_sp_item,new String[]{"name"},new int[]{R.id.tv_spinner}));
        final NumberProgressBar bassProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_bar_bass);
        int eqMax = getEqualizerMax();
        int bandLeve = getBandLeve((short) 0);
        bassProgress.setMax(eqMax * 2);
        bassProgress.setProgress(bandLeve);
        Button bassAddBtn = (Button) v.findViewById(R.id.add_button_bass);
        Button bassReduceBtn = (Button) v.findViewById(R.id.reduce_button_bass);


        final NumberProgressBar mediumProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_bar_medium_bass);
        Button mediumAddBtn = (Button) v.findViewById(R.id.add_button_medium_bass);
        Button mediumReduceBtn = (Button) v.findViewById(R.id.reduce_button_medium_bass);
        mediumProgress.setMax(getEqualizerMax());
        mediumProgress.setProgress(getBandLeve((short) 1));

        final NumberProgressBar mediantProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_mediant);
        Button mediantAddBtn = (Button) v.findViewById(R.id.add_button_mediant);
        Button mediantReduceBtn = (Button) v.findViewById(R.id.reduce_button_mediant);
        mediantProgress.setMax(getEqualizerMax());
        mediantProgress.setProgress(getBandLeve((short) 2));

        final NumberProgressBar msProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_bar_mezzo_soprano);
        Button msAddBtn = (Button) v.findViewById(R.id.add_button_mezzo_soprano);
        Button msReduceBtn = (Button) v.findViewById(R.id.reduce_button_mezzo_soprano);
        msProgress.setMax(getEqualizerMax());
        msProgress.setProgress(getBandLeve((short) 3));

        final NumberProgressBar altProgress = (NumberProgressBar) v.findViewById(R.id.number_progress_alt);
        Button altAddBtn = (Button) v.findViewById(R.id.add_button_alt);
        Button altReduceBtn = (Button) v.findViewById(R.id.reduce_alt);
        altProgress.setMax(getEqualizerMax());
        altProgress.setProgress(getBandLeve((short) 4));

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setPresetReverbPreset(getReverbNames().get(position));
                bassProgress.setProgress(getBandLeve((short) 0));
                mediumProgress.setProgress(getBandLeve((short) 1));
                mediantProgress.setProgress(getBandLeve((short) 2));
                msProgress.setProgress(getBandLeve((short) 3));
                altProgress.setProgress(getBandLeve((short) 4));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        bassReduceBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e(TAG, event.getAction() + "  " + keyCode);
                int newPosition = bassProgress.getProgress();
                if (event.getAction() == ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = bassProgress.getProgress() - 1;
                    bassProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 0, newPosition);    //手指抬起时停止发送
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
                int newPosition = mediumProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = mediumProgress.getProgress() - 1;
                    mediumProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 1, newPosition);    //手指抬起时停止发送
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
                int newPosition = mediantProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = mediantProgress.getProgress() - 1;
                    mediantProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
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
                int newPosition = msProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = msProgress.getProgress() - 1;
                    msProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
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
                int newPosition = altProgress.getProgress();
                if (event.getAction() == MotionEvent.ACTION_DOWN && keyCode == KEYCODE_DPAD_CENTER) {
                    newPosition = altProgress.getProgress() - 1;
                    altProgress.setProgress(newPosition);
                    //手指按下时触发不停的发送消息
                } else if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KEYCODE_DPAD_CENTER) {
                    setBandLevel((short) 4, newPosition);    //手指抬起时停止发送
                }
                return false;
            }
        });
    }


}
