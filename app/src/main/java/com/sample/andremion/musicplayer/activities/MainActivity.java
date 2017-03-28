

package com.sample.andremion.musicplayer.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.broadcastReceiver.MyFirstReceiver;
import com.sample.andremion.musicplayer.listener.MyItemClickListener;
import com.sample.andremion.musicplayer.listener.MyItemOnFocusChangeListener;
import com.sample.andremion.musicplayer.model.MediaEntity;
import com.sample.andremion.musicplayer.musicUtils.Utils;
import com.sample.andremion.musicplayer.view.RecyclerViewAdapter;
import com.sample.andremion.musicplayer.view.sweetAlertDialog.SweetAlertDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@EActivity(R.layout.content_list)
public class MainActivity extends PlayerActivity implements MyItemClickListener {
    private final static int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE = 0x123;
    public RecyclerViewAdapter mAdapter;
    public boolean onCreate = false;
    String TAG = "MainActivity";
    @ViewById(R.id.main_music_cover)
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
    @ViewById
    ImageView btnRefresh;
    @ViewById
    TextView tvCounter;
    private int intentIndex = -1;
    private boolean notNeedWaitServer = false;
    private boolean isScanOver = false;
    private SweetAlertDialog pDialog;
    private ArrayList<String> mListScreen = new ArrayList<>();
    private List<MediaEntity> mListMedia = new ArrayList<>();
    private MyFirstReceiver mReceiver = new MyFirstReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "scanFlag":
                    mListScreen = intent.getStringArrayListExtra("musicList");
                    scanMusicFile();
                    break;
                case Intent.ACTION_VIEW:
                    intent.getDataString();
                    Log.e("ACTION_VIEW", intent.getDataString());
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isScanOver = false;
        notNeedWaitServer = false;
        onResumeUpdate();
    }

    @AfterViews
    void afterView() {
//        ((MusicCoverView) mCoverView).setScaleType(ImageView.ScaleType.CENTER_CROP);
        IntentFilter filter = new IntentFilter();
        filter.addAction("scanFlag");
        registerReceiver(mReceiver, filter);
        mFabView.setFocusable(true);
        btnRefresh.setFocusable(true);
        btnRefresh.setFocusableInTouchMode(true);
        assert recyclerView != null;
        // improve performance if you know that changes in content do not change the size of the RecyclerView
        //如果确定每个item的内容不会改变RecyclerView的大小，设置这个选项可以提高性能
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerViewAdapter(mListMedia);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnFocusChangeListener(new MyItemOnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.e("1231", v.toString() + "121ss:" + v.getVerticalScrollbarPosition());
                }
            }
        });
        recyclerView.setAdapter(mAdapter);

        onCreate = true;
        checkPermission();
    }

    //    判断权限
    void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {//判断当前系统的版本
            int checkWriteStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//获取系统是否被授予该种权限
            int checkReadStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);//获取系统是否被授予该种权限
            int checkRecordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);//获取系统是否被授予RECORD_AUDIO权限
            int checkMountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
            //判断是否需要 向用户解释，为什么要申请该权限
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
            Log.e(TAG, "" + checkMountPermission);
            if (checkWriteStoragePermission != PackageManager.PERMISSION_GRANTED || checkReadStoragePermission != PackageManager.PERMISSION_GRANTED || checkRecordAudioPermission != PackageManager.PERMISSION_GRANTED) {//如果没有被授予
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE);
            } else {
                btnRefresh();//定义好的获取权限后的处理的事件
            }
        } else {
            btnRefresh();
        }
    }

    @Background
    void scanMusicFile() {
        if (!pDialog.isShowing()) {
            return;
        }
        Log.e(TAG, "扫描中");
        mListMedia.clear();
        mListMedia.addAll(Utils.getAllMediaList(getApplicationContext(), null));
        if (getIntent().getDataString() != null) {
            Intent intent = getIntent();
            intent.getDataString();
            try {
                String path = URLDecoder.decode(intent.getDataString(), "utf-8");//关键啊 ！
                Log.e("ACTION_VIEW", path);
                path = path.substring(7, path.length());
                final File mFile = new File(path);
                for (int i = 0; i < mListMedia.size(); i++) {
                    if (!pDialog.isShowing()) {
                        return;
                    }
                    if (Objects.equals(mFile.getName(), mListMedia.get(i).getDisplay_name()) && Objects.equals(mFile.getAbsolutePath(), mListMedia.get(i).getPath())) {
                        intentIndex = i;
                        break;
                    }
                }
                if (intentIndex == -1) {
                    MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            if (!pDialog.isShowing()) {
                                return;
                            }
                            mListMedia.clear();
                            mListMedia.addAll(Utils.getAllMediaList(getApplicationContext(), null));
                            for (int i = 0; i < mListMedia.size(); i++) {
                                if (!pDialog.isShowing()) {
                                    return;
                                }
                                if (Objects.equals(mFile.getName(), mListMedia.get(i).getDisplay_name()) && Objects.equals(mFile.getAbsolutePath(), mListMedia.get(i).getPath())) {
                                    intentIndex = i;
                                    break;
                                }
                            }
                        }
                    });
                    Log.e("your 被我解决了吧", "" + intentIndex);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (!onCreate) {
            ArrayList<MediaEntity> temporaryList = new ArrayList<>();
            for (int i = 0; i < mListMedia.size(); i++) {
                if (!pDialog.isShowing()) {
                    return;
                }
                for (int a = 0; a < mListScreen.size(); a++) {
                    if (!pDialog.isShowing()) {
                        return;
                    }
                    if (Objects.equals(mListMedia.get(i).getPath(), mListScreen.get(a))) {
                        temporaryList.add(mListMedia.get(i));
                        break;
                    }
                }
                Log.e("路径", mListMedia.get(i).getPath());
            }
            mListMedia.clear();
            mListMedia.addAll(temporaryList);
        } else {
            onCreate = false;
        }
        Log.e(TAG, "扫描结束");
        update();
    }

    @UiThread
    void update() {
        if (tvCounter != null) {
            tvCounter.setText("" + mListMedia.size() + " songs");
        }
        mAdapter.notifyDataSetChanged();
        if (mBound) {
            update(mListMedia, 0);
        } else {
            bindService();
        }
        if (pDialog.isShowing()) {
            pDialog.dismissWithAnimation();
        }
        if (intentIndex != -1 && !isScanOver) {
            update(mListMedia, intentIndex);
            intentIndex = -1;
            isScanOver = false;
            fab(null);
        }
    }

    @Click
    public void fab(View view) {
        //noinspection unchecked
//        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
////                new Pair<>(mCoverView, ViewCompat.getTransitionName(mCoverView)),
//                new Pair<>(mTitleView, ViewCompat.getTransitionName(mTitleView)),
//                new Pair<>(mTimeView, ViewCompat.getTransitionName(mTimeView)),
//                new Pair<>(mDurationView, ViewCompat.getTransitionName(mDurationView)),
////                new Pair<>(mFabView, ViewCompat.getTransitionName(mFabView)),
//                new Pair<>(mProgressView, ViewCompat.getTransitionName(mProgressView)));
//        ActivityCompat.startActivity(this, new Intent(this, DetailActivity_.class), options.toBundle());
        if (mListMedia.size() != 0) {
            notNeedWaitServer = true;
            startActivity(new Intent(this, DetailActivity_.class));
        } else {
            Toast.makeText(this, "media list is null", Toast.LENGTH_SHORT).show();
        }

    }

    @Click(R.id.btn_refresh)
    void btnRefresh() {
        Log.e(TAG, "点击");
        isScanOver = true;
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Loading");
        pDialog.setCancelable(true);
        pDialog.show();
        if (onCreate) {
            scanMusicFile();
        } else {
            scanSdCard();
        }

    }


    @Background
    public void scanSdCard() {
        if (!pDialog.isShowing()) {
            return;
        }
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String storagePath = "/storage";
        final ArrayList<String> strListMusic = Utils.folderScan(filePath);
        final ArrayList<String> strStorageListMusic = Utils.folderScan(storagePath);
        strListMusic.addAll(strStorageListMusic);
        Log.e("音乐列表长度", "" + strListMusic.size());
        if (strListMusic.size() != 0) {
            final String[] mStr = new String[strListMusic.size()];
            for (int i = 0; i < strListMusic.size(); i++) {
                if (!pDialog.isShowing()) {
                    return;
                }
                mStr[i] = strListMusic.get(i);
            }
            final int[] completedSum = {0};
            MediaScannerConnection.scanFile(this, mStr, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    if (!pDialog.isShowing()) {
                        return;
                    }
                    completedSum[0]++;
                    if (completedSum[0] == mStr.length) {
                        Log.e(TAG, "scanFlag: " + completedSum[0]);
                        sendBroadcast(new Intent("scanFlag").putStringArrayListExtra("musicList", strListMusic));
                    }
                    Log.e(TAG, path);

                }
            });

        } else {
            mListMedia.clear();
            update();
        }
    }

    @Background
    void bindService() {
        while (!mBound) {
            if (notNeedWaitServer) {
                return;
            }
            Log.e("wait", "等待服务初始化完毕");
        }
        update(mListMedia, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "keyCode: " + keyCode + " KeyEvent: " + event.getAction());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("You want to exit the player?")
                    .setCancelText("No,cancel!")
                    .setConfirmText("Yes,i do!")
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            unregisterReceiver(mReceiver);
                            finish();
                        }
                    })
                    .show();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            btnRefresh.requestFocus();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onItemClick(View view, int postion) {
        if (mBound) {
            if (!Objects.equals(mListMedia.get(postion).getPath(), getCurrentPath())) {
                update(mListMedia, postion);
            }
        } else {
            bindService();
        }
        startActivity(new Intent(this, DetailActivity_.class));
//        KShareViewActivityManager.getInstance(MainActivity.this).startActivity(this, DetailActivity_.class,R.layout.content_list,R.layout.content_detail, mProgressView);
    }
}
