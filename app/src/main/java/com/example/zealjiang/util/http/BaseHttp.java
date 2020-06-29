package com.example.zealjiang.util.http;

import android.content.Context;
import android.text.TextUtils;

import com.example.zealjiang.MyApplication;

public class BaseHttp {

    private static String mAgent = "";

/*    public static String getUserAgent(){
        if(TextUtils.isEmpty(mAgent)){
            String ver = getVersion(MyApplication.getContext());
            if(!TextUtils.isEmpty(ver) && ver.length() > 5){
                String sub = ver.substring(0,ver.length() - 5);
                mAgent = "QuickEditor/" + sub;
            }
        }
        return mAgent;
    }

    public static String getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }*/

    /**
     * 获取uc音乐信息
     *
     * @param callback
     */
    public static void getMusicInfo(String id,DefaultRequestCallback callback) {

        String params = "type=detail"
                + "&id="+id;

        String url = BaseHttpConfig.URL_UC_MP3_ID
                + params;
        HttpUtils.get(url, callback);
    }

    /** 公共参数 */
/*    public static String getCommonParameters(){
        String params = "&ver=" + getVersion(MediaApplication.getContext())
                + "&aver=" +android.os.Build.VERSION.SDK_INT //2020,03,23增加 4.2.3开始
                + "&mid=" + HttpUtils.getMid()
                + "&imei=" + HttpUtils.md5(DeviceUtil.getImei(MediaApplication.getContext()))
                + "&model=" + SystemUtil.getSystemModel()
                + "&ch=" + DeviceUtil.getChannel(MediaApplication.getContext(), true)
                + "&a_ver_int=" + android.os.Build.VERSION.SDK_INT
                + "&ts=" + String.valueOf(System.currentTimeMillis() / 1000);
        if (MediaApplication.isOverSea()) {
            String localeLanguage = LanguageUtil.getJustLanguage();
            params += "&device=" + get_device()
                    + "&lang=" + localeLanguage
                    + "&region=" + "s";
        } else {
            params +=  "&device=" + get_device();
            params += "&qmid="+ IQHStatAgent_inland.getQMid(MediaApplication.getContext());
        }
        return params;
    }*/
}
