package com.example.zealjiang.inf;

import com.example.zealjiang.bean.Mp3ImjadInfoBean;

/**
 * mp3音乐信息
 */
public interface Mp3ImjadInfoInf {
    void onSuccess(Mp3ImjadInfoBean bean);
    void onFailure(int code, String errMsg);
}
