package com.sample.andremion.musicplayer.musicUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.sample.andremion.musicplayer.model.MediaEntity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Administrator on 2017/2/28.
 */

public class Utils {

    private static String TAG = "音乐工具类";

    public static List<MediaEntity> getAllMediaList(Context context, String selection) {
        Log.e(TAG, "扫描类");
        Cursor cursor = null;
        List<MediaEntity> mediaList = new ArrayList<MediaEntity>();

        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE,
                            MediaStore.Audio.Media.ALBUM_ID
                    },
                    selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
            if (cursor == null) {
                Log.e(TAG, "The getMediaList cursor is null.");
                return mediaList;
            }
            int count = cursor.getCount();
            Log.e(TAG, "The getMediaList cursor count is " + count);
            if (count <= 0) {
                Log.e(TAG, "The getMediaList cursor count is 0.");
                return mediaList;
            }
            mediaList = new ArrayList<MediaEntity>();
            MediaEntity mediaEntity = null;
//          String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {

                mediaEntity = new MediaEntity();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                mediaEntity.durationStr = IntToStrTime(mediaEntity.duration);
                if (!checkIsMusic(mediaEntity.duration, mediaEntity.size)) {
                    continue;
                }
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Log.e(TAG, "PATH:" + mediaEntity.path);
                mediaEntity.albums = getAlbumArt(context, cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                mediaList.add(mediaEntity);
            }
        } catch (Exception e) {
            Log.e(TAG, e + "");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mediaList;
    }


    /**
     * 根据时间和大小，来判断所筛选的media 是否为音乐文件，具体规则为筛选小于30秒和1m一下的
     */
    private static boolean checkIsMusic(int time, long size) {
        if (time <= 0 || size <= 0) {
            return false;
        }

        time /= 1000;
        int minute = time / 60;
//  int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        if (minute <= 0 && second <= 30) {
            return false;
        }
        if (size <= 1024 * 512) {
            return false;
        }
        return true;
    }

    public static String IntToStrTime(int time) {
//        int a=time;
//        long hour=a/3600;    //小时
//        long minute=a%3600/60;  //分钟
//        long second=a%60;        //秒
//        String  Time = hour+":"+minute+":"+second;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.CHINA);
        return format.format(date);
    }

    public static ArrayList<String> folderScan(String path) {
        ArrayList<String> mStrList;
        mStrList = new ArrayList<>();
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] array = new File[]{};
            try {
                array = file.listFiles();
            } catch (Exception e) {
                Log.e(TAG, "" + e);
            }
            if (array != null) {
                for (File f : array) {
                    if (f.isFile()) {//FILE TYPE
                        String name = f.getName();
//                        name.endsWith(".mp4") name.endsWith(".jpg")
                        if (name.endsWith(".mp3")) {
                            mStrList.add(f.getAbsolutePath());
                        }
                    } else {//FOLDER TYPE
                        mStrList.addAll(folderScan(f.getAbsolutePath()));
                    }
                }
            }
        }
        return mStrList;
    }

    /**
     * 功能 通过album_id查找 album_art 如果找不到返回null
     *
     * @param album_id
     * @return album_art
     */
    private static String getAlbumArt(Context mContext, int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = mContext.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

    public static String replaceSpace(StringBuffer str) {
        String str1 = str.toString();
        char[] charArray = str1.toCharArray();
        StringBuilder sBuilder = new StringBuilder();
        for (char c : charArray) {
            if (c == ' ') {
                sBuilder.append("%20");
            } else {
                sBuilder.append(c);
            }
        }
        return sBuilder.toString();
    }

    public int getRandom(int inMax, int inMin) {
        Random random = new Random();
        return random.nextInt(inMax) % (inMax - inMin + 1) + inMin;
    }
}
