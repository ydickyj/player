package com.sample.andremion.musicplayer.model;

import android.net.Uri;

import java.io.Serializable;

public class MediaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public int id; //id标识  
    public String title; // 显示名称  
    public String display_name; // 文件名称  
    public String path; // 音乐文件的路径  
    public int duration; // 媒体播放总时间
    public String durationStr; // 媒体播放总时间
    public String albums; // 专辑  
    public String artist; // 艺术家   
    public String singer; //歌手   
    public long size;
    public Uri uri = Uri.parse("-1");

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDurationStr() {
        return durationStr;
    }

    public void setDurationStr(String durationStr) {
        this.durationStr = durationStr;
    }

    public String getAlbums() {
        return albums;
    }

    public void setAlbums(String albums) {
        this.albums = albums;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}