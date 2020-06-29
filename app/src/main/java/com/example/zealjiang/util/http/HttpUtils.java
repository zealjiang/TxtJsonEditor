package com.example.zealjiang.util.http;


import android.text.TextUtils;

import com.example.zealjiang.util.MD5Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;

public class HttpUtils {

    private static volatile OkHttpClient mClient = null;
    private static volatile OkHttpClient mClientTimeOut = null;
    public interface ProgressListener {
        void onProgress(long bytesRead, long contentLength, boolean done);

        void onFailure(String msg);
    }

    public static void setExecutor(OkHttpClient mOkHttpClient){
        try {
            if (mOkHttpClient == null){
                return;
            }
            Executor executor = new ThreadPoolExecutor(
                    0,
                    2147483647,
                    60L,
                    TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp ConnectionPool", true)){
                @Override
                public void execute(Runnable command) {
                    try {
                        super.execute(command);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            ConnectionPool mConnectionPool =  mOkHttpClient.connectionPool();
            Class clazz_pool = mConnectionPool.getClass();//1-
            Field field = clazz_pool.getDeclaredField("executor");
            field.setAccessible(true);
            field.set(mConnectionPool, executor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OkHttpClient getInstance() {
        if (mClient == null) {
            synchronized (HttpUtils.class) {
                if (mClient == null) {
//                    mCookieJar = new CookieJar() {
//                        @Override
//                        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
//                        }
//
//                        @Override
//                        public List<Cookie> loadForRequest(HttpUrl httpUrl) {
//                            return null;
//                        }
//                    };
                    mClient = new OkHttpClient.Builder()
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
//                            .cookieJar(mCookieJar)
                            .build();
                    setExecutor(mClientTimeOut);
                }
            }
        }
        return mClient;
    }

    public static OkHttpClient getInstanceTimeOut() {
        if (mClientTimeOut == null) {
            synchronized (HttpUtils.class) {
                if (mClientTimeOut == null) {
                    mClientTimeOut = new OkHttpClient.Builder()
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            .connectTimeout(20 , TimeUnit.SECONDS)
                            .readTimeout(30 , TimeUnit.SECONDS)
                            .build();
                    //-----
                    setExecutor(mClientTimeOut);
                }
            }
        }

        return mClientTimeOut;
    }

    private static volatile OkHttpClient mClientUpdate = null;
    public static OkHttpClient getInstanceUpdate() {
        if (mClientUpdate == null) {
            synchronized (HttpUtils.class) {
                if (mClientUpdate == null) {
                    mClientUpdate = new OkHttpClient.Builder()
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            .connectTimeout(20 , TimeUnit.SECONDS)
                            .readTimeout(30 , TimeUnit.SECONDS)
                            .build();
                    //-----
                    setExecutor(mClientTimeOut);
                }
            }
        }
        return mClientUpdate;
    }

    public static Call get(String url, DefaultRequestCallback callback) {
        Request request = new Request.Builder()
                .addHeader("User-agent","")//BaseHttp.getUserAgent())
                .url(url)
                .build();
        Call call = getInstance().newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static void post(String url,
                            RequestBody requestBodyPost,
                            DefaultRequestCallback callback) {

        if (TextUtils.isEmpty(url)) return;
        Request requestPost = new Request.Builder()
                .addHeader("User-agent","")
                .url(url)
                .post(requestBodyPost)
                .build();
        getInstance().newCall(requestPost).enqueue(callback);
    }

/*    public static void getWithQTCookie(String url, DefaultRequestCallback callback) {
        StringBuilder cookieHeader = new StringBuilder();
        try {
            String q1 = URLEncoder.encode(LoginManager.getInstance(MediaApplication.getContext()).getQ(), "UTF-8");
            String t1 = URLEncoder.encode(LoginManager.getInstance(MediaApplication.getContext()).getT(), "UTF-8");
            cookieHeader.append("Q").append('=').append(q1).append("; ").append("T").append('=').append(t1);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .addHeader("User-agent", BaseHttp.getUserAgent())
                .addHeader("Cookie", cookieHeader.toString())
                .url(url)
                .build();
        getInstance().newCall(request).enqueue(callback);
    }*/

/*    public static void getWithCookie(String url, String cookie, DefaultRequestCallback callback) {
        Request request = new Request.Builder()
                .addHeader("User-agent",BaseHttp.getUserAgent())
                .addHeader("Cookie",cookie)
                .url(url)
                .build();
        getInstance().newCall(request).enqueue(callback);
    }*/

    /** 参数排序加密处理 */
    public static String paramsProcess(String params,String key){
        //排序
        String sortParams = HttpUtils.sortParams(params);
        //加key
        sortParams = sortParams
                + "&key="+key;
        //生成校验码sign
        String sign = HttpUtils.md5(sortParams);
        //加sign
        String finalParam = params
                + "&sign=" + sign;
        return finalParam;//为什么不用排序和加key后的？
    }

    /** 对参数进行排序 */
    public static String sortParams(String params){
        //&type=3&ts=1527736517&ver=3.0.0.1013&mid=54488d2651b8d89e74ee4d22050062c1&imei=37c178e8c18bbfd5bc2dbb8dc5c61dee&device=1&model=vivo X9s&ch=300001

        if(TextUtils.isEmpty(params) || !params.contains("&")){
            return "";
        }
        //params = params.toLowerCase();
        String[] arrays = params.split("&");
        HashMap map = new HashMap();
        for (int i = 0; i < arrays.length; i++) {
            if(TextUtils.isEmpty(arrays[i]) || !arrays[i].contains("=")){
                continue;
            }
            String[] kv = arrays[i].split("=");
            if(kv.length == 1){
                map.put(kv[0],"");
            }else{
                map.put(kv[0],arrays[i].substring((kv[0]+"=").length()));
            }
        }

        //第一步： 将HashMap转成List
        List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        //第二步：Collections.sort排序
        Collections.sort(infoIds, new Comparator<Map.Entry<String,  String>>() {
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                // 指定排序器按照升序排列
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        //升序
        //Collections.sort(map, Collator.getInstance(java.util.Locale.CHINA));//注意：是根据的汉字的拼音的字母排序的，而不是根据汉字一般的排序方法

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < infoIds.size(); i++) {
            Map.Entry<String, String> entry = infoIds.get(i);
            if(i>0) {
                sb.append("&");
            }
            sb.append(entry.getKey().toLowerCase());
            sb.append("=");
            sb.append(entry.getValue());
        }
        String sortParams = sb.toString();
        return sortParams;
    }

    public static String md5(String s) {
        return MD5Util.md5LowerCase(s);
    }
}
