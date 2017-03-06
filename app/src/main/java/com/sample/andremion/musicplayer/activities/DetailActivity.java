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
import android.os.Environment;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.listener.ProgressListener;
import com.sample.andremion.musicplayer.view.LyricView;
import com.sample.andremion.musicplayer.view.MusicCoverView;
import com.sample.andremion.musicplayer.view.ProgressView;
import com.sample.andremion.musicplayer.view.TransitionAdapter;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;



@EActivity(R.layout.content_detail)
public class DetailActivity extends PlayerActivity  {
    String TAG = DetailActivity.class.getName();
    @ViewById(R.id.cover)
    MusicCoverView mCoverView;
    @ViewById
    TextView displayName;
    @ViewById
    TextView displayAuthor;
    @ViewById
    LyricView lyricView;
    @ViewById
    ProgressView mProgressView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {


        mProgressView.setProgressListener(new ProgressListener() {
            @Override
            public void onProgressListener(boolean isFinish) {
                forwardMusic();
            }
        });
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
                Log.e(TAG,""+getPosition());
//                lyricView.updateIndex(getPosition());

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
    void repeat(){

    }
    @Click(R.id.shuffle)
    void shuffle(){

    }
    @Click(R.id.previous)
    void previousSong(){
        preMusic();
    }
    @Click(R.id.rewind)
    void rewindSong(){
        rewindMusic();
    }
    @Click(R.id.forward)
    void forward(){
        forwardMusic();
    }
    @Click(R.id.next)
    void nextMusic(){
        next();
    }


}
