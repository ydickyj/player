

package com.sample.andremion.musicplayer.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.andremion.musicplayer.R;
import com.sample.andremion.musicplayer.broadcastReceiver.MyFirstReceiver;
import com.sample.andremion.musicplayer.listener.MyItemClickListener;
import com.sample.andremion.musicplayer.model.MediaEntity;
import com.sample.andremion.musicplayer.musicUtils.Utils;
import com.sample.andremion.musicplayer.view.ListViewAdapter;
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
    public ListViewAdapter mAdapter;
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
    ListView recyclerView;
    @ViewById
    TextView displayName;
    @ViewById
    TextView displayAuthor;
    @ViewById
    ImageView btnRefresh;
    @ViewById
    TextView tvCounter;
    private int intentIndex = -1;
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
                case "onBindFinish":
                    if (intentIndex != -1 && !isScanOver) {
                        update(mListMedia, intentIndex);
                        intentIndex = -1;
                        isScanOver = false;
                        fab(null);
                    } else {
                        if (mListMedia.size() != 0 && onCreate) {
                            update(mListMedia, 0);
                        }
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            btnRefresh();
        }
    };


    @AfterViews
    void afterView() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("scanFlag");
        filter.addAction("onBindFinish");
        registerReceiver(mReceiver, filter);
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
        mFabView.setFocusable(true);
        btnRefresh.setFocusable(true);
        btnRefresh.setFocusableInTouchMode(true);
        assert recyclerView != null;
        mAdapter = new ListViewAdapter(mListMedia, this);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemsCanFocus(true);
        onCreate = true;
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanOver = false;
        onResumeUpdate();
    }

    /**
     * 判断权限,小于api 20的android版本不需要检测版本
     */
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
            //如果没有被授予
            if (checkWriteStoragePermission != PackageManager.PERMISSION_GRANTED || checkReadStoragePermission != PackageManager.PERMISSION_GRANTED || checkRecordAudioPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE);
            } else {
                btnRefresh();//定义好的获取权限后的处理的事件
            }
        } else {
            btnRefresh();
        }
    }

    /**
     * 扫描媒体文件，从媒体库中获取文件，在后台运行
     */
    @Background
    void scanMusicFile() {
        if (!pDialog.isShowing()) {
            return;
        }
        Log.e(TAG, "扫描中");
        mListMedia.clear();
        mListMedia.addAll(Utils.getAllMediaList(getApplicationContext(), null));

        Log.e("tag", "" + getIntent().getData() + "  " + getIntent().toString());
        boolean is;
        is = getIntent().getData() != null;
        if (is) {
            if (Build.VERSION.SDK_INT >= 23) {
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
                    if (intentIndex == -1) {
                        MediaEntity intentData = new MediaEntity();
                        Uri mUri = intent.getData();
                        intentData.setUri(mUri);
                        intentData.setDisplay_name((mUri).getLastPathSegment());
                        intentData.setPath(mUri.getPath());
                        Log.e("your 被我解决了吧", "" + mUri.getPath());
                        mListMedia.add(intentData);
                        if (mListMedia.size() >= 1) {
                            intentIndex = (mListMedia.size() - 1);
                        } else {
                            intentIndex = 0;
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                String prefix = getIntent().getData().toString().substring(getIntent().getData().toString().lastIndexOf("/"), getIntent().getData().toString().length());
                prefix = prefix.substring(1, prefix.length());
                Intent intent = getIntent();
                intent.getDataString();
                for (int i = 0; i < mListMedia.size(); i++) {
                    if (!pDialog.isShowing()) {
                        return;
                    }
                    if (Objects.equals(Integer.parseInt(prefix), mListMedia.get(i).id)) {
                        intentIndex = i;
                        break;
                    }
                }
                if (intentIndex == -1) {
                    MediaEntity intentData = new MediaEntity();
                    intentData.setUri(intent.getData());
                    intentData.setDisplay_name((intent.getData()).getLastPathSegment());
                    mListMedia.add(intentData);
                    if (mListMedia.size() >= 1) {
                        intentIndex = (mListMedia.size() - 1);
                    } else {
                        intentIndex = 0;
                    }

                }
            }
        }
        Log.e(TAG, "扫描结束");
        update();
    }

    /**
     * 更新界面
     */
    @UiThread
    void update() {
        if (tvCounter != null) {
            tvCounter.setText("" + mListMedia.size() + " songs");
        }
        mAdapter.notifyDataSetChanged();
        if (mBound) {
            update(mListMedia, 0);
        }
        if (pDialog.isShowing()) {
            pDialog.dismissWithAnimation();
        }
    }

    @Click
    public void fab(View view) {
        if (mListMedia.size() != 0) {
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
        scanMusicFile();
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
    public void onItemClick(View view, int position) {
        if (mBound) {
            if (!Objects.equals(mListMedia.get(position).getPath(), getCurrentPath())) {
                update(mListMedia, position);
            }
            onCreate = false;
            startActivity(new Intent(this, DetailActivity_.class));
        } else {
            Toast.makeText(this, "播放服务未绑定", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        unregisterReceiver(mScanListener);
        super.onDestroy();
    }
}
