package com.example.zealjiang.util;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import com.example.zealjiang.bean.MediaInfoBean;

import java.io.File;

public class MediaUtil {

    public static MediaInfoBean getMedieInfo(String filePath){
        if(TextUtils.isEmpty(filePath)){
            ToastUtil.sysToast("文件不存在");
            return null;
        }

        if(!FileUtil.existFile(filePath)){
            ToastUtil.sysToast("文件不存在");
            return null;
        }

        MediaInfoBean bean = null;
        try {
            MediaMetadataRetriever mmr=new MediaMetadataRetriever();//实例化MediaMetadataRetriever对象mmr
            File file=new File(filePath);//实例化File对象file，指定文件路径为/storage/sdcard/Music/music1.mp3
            //FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
            //mmr.setDataSource(inputStream.getFD());
            mmr.setDataSource(file.getAbsolutePath());//设置mmr对象的数据源为上面file对象的绝对路径
            String ablumString=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);//获得音乐专辑的标题
            String artistString=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);//获取音乐的艺术家信息
            String titleString=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);//获取音乐标题信息
            String mimetypeString=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);//获取音乐mime类型
            String durationString=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);//获取音乐持续时间
            String bitrateString=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);//获取音乐比特率，位率
            String dateString=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);//获取音乐的日期


            /* 设置文本的内容 */
            bean = new MediaInfoBean();
            bean.album = ablumString;
            bean.artist = artistString;
            bean.title = titleString;
            bean.mimetype = mimetypeString;
            bean.duration = durationString;
            bean.bitrate = bitrateString;
            bean.date = dateString;
        }catch (Exception e){
            e.printStackTrace();
        }



        return bean;
    }
}
