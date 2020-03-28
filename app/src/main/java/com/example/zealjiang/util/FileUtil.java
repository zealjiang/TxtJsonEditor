package com.example.zealjiang.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

/**
 * Created by zealjiang on 2018/1/7.
 */

public class FileUtil {


    public static String getRealFilePath(final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();

            //替换file://
            data = uri.toString().replace("file://", "");
        }
        return data;
    }

    public static String getFileName(String path){

        int start=path.lastIndexOf("/");
        int end=path.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            return path.substring(start+1,end);
        }else{
            return null;
        }

    }

    public static String readFile(File file) {

        String out_str = "";
        StringBuilder sb = new StringBuilder();

        try {
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int n;
            try {
                while ((n = in.read(buffer)) != -1) {
                    out_str = new String(buffer, 0, n);
                    sb.append(out_str);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String s_temp = sb.toString();
        // 将文件内容的第一个?号去掉
        // s_temp = sb.toString().substring(1);

        System.out.println("s_temp :" + s_temp);
        return s_temp;
    }


    public static String convertCodeAndGetText(String str_filepath) {// 转码

        File file = new File(str_filepath);
        BufferedReader reader;
        String text = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();
            if (first3bytes[0] == (byte)0xEF  && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {// utf-8

                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));

            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {

                reader = new BufferedReader(
                        new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16le"));
            } else {

                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }
            String str = reader.readLine();

            while (str != null) {
                text = text + str + System.getProperty("line.separator");
                str = reader.readLine();

            }
            reader.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * 获取URL的编码
     *
     * @param url
     * @return
     */
    public static String getUrlEncode(URL url) {
        /*
         * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
         * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，如ParsingDetector、
         * JChardetFacade、ASCIIDetector、UnicodeDetector。
         * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
         * 字符集编码。使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
         * cpDetector是基于统计学原理的，不保证完全正确。
         */
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        detector.add(new ParsingDetector(false));
        detector.add(new ByteOrderMarkDetector());
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         *
         * 用到antlr.jar、chardet.jar
         */
        detector.add(JChardetFacade.getInstance());
        // ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());


        java.nio.charset.Charset charset = null;
        try {
            charset = detector.detectCodepage(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (charset != null) {
            return charset.name();
        }
        return null;
    }

    public static boolean isPic(String path){
        if(TextUtils.isEmpty(path)){
            return false;
        }
        if(path.endsWith("jpeg")||path.endsWith("jpg")||path.endsWith("png")||path.endsWith("bmp")){
            if(new File(path).exists() && new File(path).isFile()){
                return true;
            }
        }
        return false;
    }

    public static boolean isText(String path){
        if(TextUtils.isEmpty(path)){
            return false;
        }

        if(path.endsWith("txt")||path.endsWith("xml")||path.endsWith("json")||path.endsWith("html")
                ||path.endsWith("htm")||path.endsWith("hts")||path.endsWith("log")
                ||!path.contains(".")){
            if(new File(path).exists() && new File(path).isFile()){
                return true;
            }
        }
        return false;
    }

    public static void writeSDFile(File file, String content) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            byte[] bytes = content.getBytes();
            fos.write(bytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
