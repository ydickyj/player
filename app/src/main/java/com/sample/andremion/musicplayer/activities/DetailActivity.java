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

import android.graphics.Color;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.view.LyricView;
import com.sample.andremion.musicplayer.view.MusicCoverView;
import com.sample.andremion.musicplayer.view.ProgressView;
import com.sample.andremion.musicplayer.view.TransitionAdapter;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;


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


}
