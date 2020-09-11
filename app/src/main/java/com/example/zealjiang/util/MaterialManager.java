package com.example.zealjiang.util;


import java.io.File;


public class MaterialManager {

    /** 音乐 */
    public final static String UC_MUSIC_NAME="uc_music";
    public static String UC_MUSIC_DIR = getDir(UC_MUSIC_NAME);
    public final static String UC_TEMP_MUSIC_NAME="temp_music";
    public static String UC_TEMP_MUSIC_DIR = UC_MUSIC_DIR+UC_TEMP_MUSIC_NAME+ File.separator;


    /** 贴图文件夹 */
    public final static String STICKER_NAME="sticker";
    //public static String STICKER_DIR = getDir(STICKER_NAME);//FileUtil.getCacheDir(STICKER_NAME).getAbsolutePath()+ File.separator;
    public static final String STICKER_FILE = "file_sticker.txt";//贴图资源文件
    public final static String STICKER_ANIMATION_NAME= "animation";//动图
    //public static String STICKER_ANIMATION_DIR= STICKER_DIR+STICKER_ANIMATION_NAME+File.separator;//动图地址


    /** 动画 */
    public final static String VIDEO_ANIM_NAME="video_anim";
    //public static String VIDEO_ANIM_DIR = getDir(VIDEO_ANIM_NAME);//FileUtil.getCacheDir(PIP_NAME).getAbsolutePath()+ File.separator;
    public static final String VIDEO_ANIM_FILE = "file_video_anim.txt";//动画列表文件


    public static String getDir(String dirName){
        File file = FileUtil.getCacheDir(dirName);
        if(file == null){
            ToastUtil.showToastCenter("请申请文件读写权限");
            return "";
        }
        return file.getAbsolutePath()+ File.separator;
    }

    public static String getKeyFilesDir(String dirName){
        File file = FileUtil.getKeyfilesDir(dirName);
        if(file == null){
            ToastUtil.showToastCenter("请申请文件读写权限");
            return "";
        }
        return file.getAbsolutePath()+ File.separator;
    }

    public static String getFileDir(String dirName){
        File file = FileUtil.getFileDir(dirName);
        if(file == null){
            ToastUtil.showToastCenter("请申请文件读写权限");
            return "";
        }
        return file.getAbsolutePath()+ File.separator;
    }
}
