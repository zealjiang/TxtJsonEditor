package com.example.zealjiang.helper;

import android.app.Activity;
import android.os.Environment;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.example.zealjiang.MyApplication;
import com.example.zealjiang.bean.MediaInfoBean;
import com.example.zealjiang.bean.Mp3ImjadInfoBean;
import com.example.zealjiang.bean.UcMusicIdBean;
import com.example.zealjiang.inf.Mp3ImjadInfoInf;
import com.example.zealjiang.util.FileUtil;
import com.example.zealjiang.util.MaterialManager;
import com.example.zealjiang.util.MediaUtil;
import com.example.zealjiang.util.NetworkUtils;
import com.example.zealjiang.util.ToastUtil;
import com.example.zealjiang.util.http.BaseHttp;
import com.example.zealjiang.util.http.DefaultRequestCallback;
import com.example.zealjiang.util.log.XLog;
import com.example.zealjiang.util.thread.LibTaskController;
import com.example.zealjiang.view.PageStateLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class UcToMp3Helper {

    private Fragment fragement;
    private Activity activity;
    private Mp3ImjadInfoInf mp3ImjadInfoInf;

    private final String rootPath = Environment.getExternalStorageDirectory()
            +File.separator+"netease"
            +File.separator+"cloudmusic"
            +File.separator+"Cache";
    private final String musicPath = rootPath
            + File.separator+"Music1";
    private final String lyricPath = rootPath
            + File.separator+"Lyric";
    private final String lyric = "Lyric";
    private final String dir = MaterialManager.UC_MUSIC_DIR;


    public UcToMp3Helper(Fragment fragement){
        this.fragement = fragement;
    }
    public UcToMp3Helper(Activity activity){
        this.activity = activity;
    }

    private void findIdxFile(String mp3FilePath){
        if(TextUtils.isEmpty(mp3FilePath)){
            ToastUtil.sysToast("要解码的文件不存在");
            return;
        }
        String noLastEx = FileUtil.getFileNameNoEx(mp3FilePath);
        if(TextUtils.isEmpty(noLastEx)){
            ToastUtil.sysToast("获取缓存名字失败");
            return;
        }
        String idxFilePath = noLastEx+".idx!";

        String prefixId = "";
        if(FileUtil.existFile(idxFilePath)){
            //对应的idx文件不存在
            String response = FileUtil.readSDFile(new File(idxFilePath));
            if (!TextUtils.isEmpty(response)) {
                UcMusicIdBean ucMusicIdBean;
                //解析
                try {
                    ucMusicIdBean = JSON.parseObject(response, UcMusicIdBean.class);
                    if(ucMusicIdBean != null){
                        prefixId = ucMusicIdBean.getMusicId()+"";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(TextUtils.isEmpty(prefixId)){
            String name = FileUtil.getFileNameByUrl(idxFilePath);
            if(TextUtils.isEmpty(name)){
                ToastUtil.sysToast("获取缓存文件名称失败");
                return;
            }
            int index = name.indexOf("-");
            if(index == -1){
                ToastUtil.sysToast("获取缓存文件前缀id失败");
                return;
            }
            prefixId = name.substring(0,index);
        }

        if(TextUtils.isEmpty(prefixId)){
            return;
        }

        //通过歌词文件来获取歌曲信息
        File fileMp3 = new File(mp3FilePath);
        File fileLyric = new File(fileMp3.getParentFile(),lyric);
        if(!fileLyric.exists()){
            ToastUtil.sysToast("获取歌词文件夹失败");
            return;
        }
        //查找这个id歌词文件是否存在
        File fileLyricId = new File(fileLyric,prefixId);
        if(!fileLyricId.exists()){
            ToastUtil.sysToast("获取歌词文件夹失败");
            return;
        }
    }

    private String getPrefix(String ucMusicPath){
        if(TextUtils.isEmpty(ucMusicPath)){
            ToastUtil.sysToast("要解码的文件不存在");
            return "";
        }
        int index0 = ucMusicPath.lastIndexOf("/");
        if(index0 == -1 || ucMusicPath.length() <= index0 +1){
            ToastUtil.sysToast("获取缓存文件前缀id失败");
            return "";
        }
        String lastStr = ucMusicPath.substring(index0+1);
        if(TextUtils.isEmpty(lastStr)){
            ToastUtil.sysToast("获取缓存文件前缀id失败");
            return "";
        }

        int index = lastStr.indexOf("-");
        if(index == -1){
            ToastUtil.sysToast("获取缓存文件前缀id失败");
            return "";
        }
        String prefixId = lastStr.substring(0,index);
        return prefixId;
    }

    public void readFile(final PageStateLayout pageStateLayout,String ucMusicPath){
        if(TextUtils.isEmpty(ucMusicPath))return;
        if(!FileUtil.existFile(ucMusicPath)){
            ToastUtil.sysToast("要解码的文件不存在");
            return;
        }

        if (pageStateLayout != null) {
            pageStateLayout.showLoading();
        }

        LibTaskController.run(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                FileInputStream fis = null;
                String fileOutPath = "";
                try {
                    XLog.debug("mtest","开始解码");
                    fileOutPath = dir+"temp.mp3";//"/sdcard/back_in_time.mp3";
                    File fout = new File(fileOutPath);
                    fout.createNewFile();
                    fos = new FileOutputStream(fout);


                    String filePath = ucMusicPath;//"/sdcard/a.uc.mp3";
                    File f = new File(filePath);
                    fis = new FileInputStream(f);
                    byte[] buffer = new byte[1024 * 10];
                    int n;
                    while ((n = fis.read(buffer)) != -1) {
                        for (int i = 0; i < n; i++) {
                            fos.write(buffer[i]^0xa3);
                        }
                    }
                    XLog.debug("mtest","解码完成");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(fos != null){
                            fos.close();
                        }
                        if(fos != null){
                            fos.close();
                        }
                        if(fos != null){
                            fos.close();
                        }
                        if(fis != null){
                            fis.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(TextUtils.isEmpty(fileOutPath)){
                    XLog.debug("mtest","重命名失败,输出文件名称为空");

                    showContent(pageStateLayout);
                    return;
                }
                //根据音乐文件信息重命名文件
                MediaInfoBean bean = MediaUtil.getMedieInfo(fileOutPath);
                if(TextUtils.isEmpty(bean.title)){
                    XLog.debug("mtest","重命名失败,获取解码后音乐文件的名称为空");

                    //通过网络获取歌曲名称
                    String mp3Id = getPrefix(ucMusicPath);
                    if(TextUtils.isEmpty(mp3Id)){
                        XLog.debug("mtest","mp3Id is null");
                        showContent(pageStateLayout);
                        return;
                    }

                    final String fileOrignalPath = fileOutPath;
                    obtainMp3Info(null, mp3Id, new Mp3ImjadInfoInf() {
                        @Override
                        public void onSuccess(Mp3ImjadInfoBean bean) {
                            if(bean == null || bean.getSongs() == null || bean.getSongs().size() == 0){
                                showContent(pageStateLayout);
                                return;
                            }
                            String title = bean.getSongs().get(0).getName();
                            if(TextUtils.isEmpty(title)){
                                XLog.debug("mtest","网络获取歌曲名失败");
                                showContent(pageStateLayout);
                                return;
                            }

                            String newTitle = dir+title+".mp3";
                            (new File(fileOrignalPath)).renameTo(new File(newTitle));
                            showContent(pageStateLayout);
                            XLog.debug("mtest","重命名成功");
                        }

                        @Override
                        public void onFailure(int code, String errMsg) {
                            XLog.debug("mtest","网络获取歌曲名失败"+errMsg);
                            showContent(pageStateLayout);
                        }
                    });
                    return;
                }

            }
        });
    }

    private void showContent(final PageStateLayout pageStateLayout){
        if(activity != null && !activity.isDestroyed()){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pageStateLayout != null) {
                        pageStateLayout.showContent();
                    }
                }
            });
        }
    }

    public void setMp3ImjadInfoInf(Mp3ImjadInfoInf mp3ImjadInfoInf){
        this.mp3ImjadInfoInf = mp3ImjadInfoInf;
    }

    public void obtainMp3Info(final PageStateLayout pageStateLayout, final String id,
                              final Mp3ImjadInfoInf mp3ImjadInfoInf) {

        if (pageStateLayout != null) {
            pageStateLayout.showLoading();
        }
        //判断有无网络
        boolean isConnected = NetworkUtils.isNetworkConnected(MyApplication.getContext());
        if (!isConnected) {
            if (pageStateLayout != null) {
                pageStateLayout.showContent();
            }
            obtainFail(-1, "network not connected");
            return;
        }

        BaseHttp.getMusicInfo(id,new DefaultRequestCallback() {

            @Override
            public void onResponse(String response) {
/*                if (fragement == null || fragement.getActivity() == null) {
                    XLog.error("mtest", " onResponse getActivity() is null");
                    return;
                }

                fragement.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pageStateLayout != null) {
                            pageStateLayout.showContent();
                        }
                    }
                });*/

                //XLog.info(TAG, "onResponse " + response);
                if (TextUtils.isEmpty(response)) {
                    XLog.info("mtest", "返回数据为空");
                    obtainFail(401,"返回数据为空");//返回数据为空
                    return;
                }

                Mp3ImjadInfoBean mp3ImjadInfoBean;
                //解析
                try {
                    mp3ImjadInfoBean = JSON.parseObject(response, Mp3ImjadInfoBean.class);
                } catch (Exception e) {
                    e.printStackTrace();

                    fragement.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToastCenter("return data error");//数据返回异常，解析失败
                        }
                    });
                    return;
                }

                //通知界面
                if(mp3ImjadInfoBean!=null){
                    if(mp3ImjadInfoInf != null){
                        mp3ImjadInfoInf.onSuccess(mp3ImjadInfoBean);
                    }
                }else {
                    //obtainFail(401,"json parse error");
                    if(mp3ImjadInfoInf != null){
                        mp3ImjadInfoInf.onFailure(401,"json parse error");
                    }
                }

            }

            @Override
            public void onFailure(final int code, final String errMsg) {
                if (fragement.getActivity() == null) {
                    XLog.error("mtest", " getDacoration onFailure getActivity() is null");
                    return;
                }

                //通知界面
                fragement.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pageStateLayout != null) {
                            pageStateLayout.showContent();
                        }
                    }
                });

                //obtainFail(code, errMsg);
                if(mp3ImjadInfoInf != null){
                    mp3ImjadInfoInf.onFailure(code,errMsg);
                }
            }
        });
    }

    /**
     * 服务器获取失败
     * @param code
     * @param errMsg
     */
    private void obtainFail(int code,String errMsg){
        returnErrorDataToView(code,errMsg);
    }

    private void returnErrorDataToView(final int code,final String errMsg){
        if(fragement.getActivity() == null){
            return;
        }
        fragement.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mp3ImjadInfoInf!=null){
                    mp3ImjadInfoInf.onFailure(code,errMsg);
                }
            }
        });
    }

    private void returnDataToView(final Mp3ImjadInfoBean mp3ImjadInfoBean){
        if(fragement.getActivity() == null){
            return;
        }
        fragement.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mp3ImjadInfoInf!=null){
                    mp3ImjadInfoInf.onSuccess(mp3ImjadInfoBean);
                }
            }
        });
    }

    private void bb(){

    }

    private void aa(String ucMusicPath){
        FileOutputStream fos = null;
        FileInputStream fis = null;
        String fileOutPath = "";
        try {
            XLog.debug("mtest","开始解码");
            fileOutPath = dir+"temp.mp3";//"/sdcard/back_in_time.mp3";
            File fout = new File(fileOutPath);
            fout.createNewFile();
            fos = new FileOutputStream(fout);


            String filePath = ucMusicPath;//"/sdcard/a.uc.mp3";
            File f = new File(filePath);
            fis = new FileInputStream(f);
            byte[] buffer = new byte[1024 * 10];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                for (int i = 0; i < n; i++) {
                    fos.write(buffer[i]^0xa3);
                }
            }
            XLog.debug("mtest","解码完成");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null){
                    fos.close();
                }
                if(fos != null){
                    fos.close();
                }
                if(fos != null){
                    fos.close();
                }
                if(fis != null){
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
