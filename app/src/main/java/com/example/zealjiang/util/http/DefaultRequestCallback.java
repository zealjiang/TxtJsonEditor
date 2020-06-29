package com.example.zealjiang.util.http;

import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;


public abstract class DefaultRequestCallback implements okhttp3.Callback {


    private android.os.Handler mUIHandler = new android.os.Handler(Looper.getMainLooper());

    @Override
    public void onFailure(Call call, IOException e) {
        //网络异常
        serverReturn = false;
        onFailure(-10, e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (abandon) {
            return;
        }
        String responseMsg;
        try {
            responseMsg = response.body().string();
            onResponse(responseMsg);
/*            responseMsg = response.body().string();
            DefaultResponseBean bean = JSON.parseObject(responseMsg, DefaultResponseBean.class);
            if (bean.code == 0 || bean.errno == 0) {
                onResponse(bean.data);
            } else {
                onFailure(bean.code, TextUtils.isEmpty(bean.msg) ? bean.errmsg : bean.msg);
                onFailure(bean.code,bean.errno,TextUtils.isEmpty(bean.msg) ? bean.errmsg : bean.msg);
            }*/
        } catch (Exception e) {
            //---onFailure(-10, e.getMessage());
            serverReturn = false;
            onFailure(-10, "return data analysis error");
        }

        identity = "";
    }

    protected boolean serverReturn = true;
    protected boolean abandon = false;

    public boolean isAbandon() {
        return abandon;
    }

    abstract public void onResponse(String response);

    abstract public void onFailure(int code, String errMsg);

    public void onFailure(int code,int errno, String errMsg){};

    private Call mCall;

    public void setCall(Call mCall_) {
        mCall = mCall_;
    }

    public void abandon() {
        try {
            abandon = true;
            if (mCall != null) {
                mCall.cancel();
                mCall = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 用来标示某一请求的结果是不是这个请求的(用在：如果从结果数据中无法判断，又需要区分的情况)*/
    private String identity = "";
    public DefaultRequestCallback setIdentity(String identity){
        this.identity = identity;
        return this;
    }
    public String getIdentity(){
        return identity;
    }
}
