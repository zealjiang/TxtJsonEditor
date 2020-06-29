package com.example.zealjiang.bean;

import java.util.List;

public class UcMusicIdBean {


    /**
     * duration : 182000
     * filesize : 3673546
     * musicId : 28251526
     * filemd5 : b95ac09f3d497cb3ba0a673d3eb82e91
     * version : 2
     * parts : ["0,3072000"]
     * bitrate : 160000
     * md5 : deeb45ef930cd229f0ffb225e6d3ed13
     */

    private int duration;
    private int filesize;
    private int musicId;
    private String filemd5;
    private int version;
    private int bitrate;
    private String md5;
    private List<String> parts;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getFilemd5() {
        return filemd5;
    }

    public void setFilemd5(String filemd5) {
        this.filemd5 = filemd5;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public List<String> getParts() {
        return parts;
    }

    public void setParts(List<String> parts) {
        this.parts = parts;
    }
}
