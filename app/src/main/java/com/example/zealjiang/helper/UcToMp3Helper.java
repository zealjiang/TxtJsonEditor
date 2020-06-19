package com.example.zealjiang.helper;

import android.text.TextUtils;
import android.widget.Toast;

import com.example.zealjiang.MyApplication;
import com.example.zealjiang.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UcToMp3Helper {

    public void readFile(String ucMusicPath){
        if(TextUtils.isEmpty(ucMusicPath))return;
        if(!FileUtil.existFile(ucMusicPath)){
            Toast.makeText(MyApplication.getContext(),"要解码的文件不存在",Toast.LENGTH_SHORT).show();
            return;
        }


        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {

            String fileOutPath = "/sdcard/back_in_time.mp3";
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
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
