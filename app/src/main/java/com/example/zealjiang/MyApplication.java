package com.example.zealjiang;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by zealjiang on 2018/2/4.
 */

public class MyApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);

        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
