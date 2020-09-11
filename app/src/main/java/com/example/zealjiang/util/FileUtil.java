package com.example.zealjiang.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.example.zealjiang.MyApplication;
import com.example.zealjiang.util.log.XLog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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


    public static File getCacheDir(String dirName) {
        File result;
        if (existsSdcard()) {
            File cacheDir = MyApplication.getContext().getExternalFilesDir("res");
            if (cacheDir == null) {
                result = new File(Environment.getExternalStorageDirectory(),
                        "Android/data/" + MyApplication.getContext().getPackageName() + "/res/" + dirName);
            } else {
                result = new File(cacheDir, dirName);
            }

            if (!result.exists() && !result.mkdirs()) {
                result = new File(MyApplication.getContext().getCacheDir(), dirName);
            }
        } else {
            result = new File(MyApplication.getContext().getCacheDir(), dirName);
        }
        if (result.exists() || result.mkdirs()) {
            return result;
        } else {
            return null;
        }
    }

    public static File getFileDir(String dirName) {
        File result;
        String parentDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            File file = MyApplication.getContext().getExternalFilesDir(null);
            if(file != null){
                parentDir = file.getPath();
            }else{
                parentDir = new File(Environment.getExternalStorageDirectory(),
                        "Android/data/" + MyApplication.getContext().getPackageName() + "/files/").getPath();
            }
        } else {
            parentDir = MyApplication.getContext().getFilesDir().getPath();
        }
        result = new File(parentDir, dirName);
        if (result.exists() || result.mkdirs()) {
            return result;
        } else {
            return null;
        }
    }

    public static File getKeyfilesDir(String dirName) {
        File result;
        if (existsSdcard()) {
            File cacheDir = MyApplication.getContext().getExternalFilesDir("keyfiles");
            if (cacheDir == null) {
                result = new File(Environment.getExternalStorageDirectory(),
                        "Android/data/" + MyApplication.getContext().getPackageName() + "/keyfiles/" + dirName);
            } else {
                result = new File(cacheDir, dirName);
            }

            if (!result.exists() && !result.mkdirs()) {
                result = new File(MyApplication.getContext().getFilesDir(), "/keyfiles/" + dirName);
            }
        } else {
            result = new File(MyApplication.getContext().getFilesDir(), "/keyfiles/" + dirName);
        }
        if (result.exists() || result.mkdirs()) {
            return result;
        } else {
            return null;
        }
    }

    public static boolean saveFile(String response,String dirPath,String dirName,String fileName){
        //保存数据到本地
        if(TextUtils.isEmpty(response) || TextUtils.isEmpty(fileName))return false;
        if (TextUtils.isEmpty(dirPath)) {
            if(TextUtils.isEmpty(dirName))return false;
            dirPath = MaterialManager.getDir(dirName);
        }
        if (TextUtils.isEmpty(dirPath)) {
            ToastUtil.showToastCenter("保存失败，文件路径不存在");
            return false;
        }else{
            if(!TextUtils.isEmpty(response)){
                File file = new File(dirPath, fileName);


                //创建文件夹
                FileUtil.createDir(dirPath);
                //缓存结果到本地文件
                FileUtil.writeSDFile(file, response);
            }
        }
        return false;
    }

    public static String readFile(String dirPath,String fileName){
        if (TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(fileName))return "";
        //从缓存文件中读取数据
        File file = new File(dirPath,fileName);
        if(file.exists()) {
            String response = FileUtil.readSDFile(file);
            return response;
        }
        return "";
    }


    public static boolean existFile(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return false;
        }
        File file = new File(filename);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static boolean existFolder(String fileFolder) {
        if (TextUtils.isEmpty(fileFolder)) {
            return false;
        }
        File file = new File(fileFolder);
        if (file.isDirectory()) {
            return true;
        }
        return false;
    }

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

    /**
     * 检查磁盘空间是否大于10mb
     *
     * @return true 大于
     */
    public static boolean isDiskAvailable() {
        long size = getDiskAvailableSize();
        return size > 10 * 1024 * 1024; // > 10bm
    }

    /**
     * 获取磁盘可用空间
     *
     * @return byte 单位 kb
     */
    public static long getDiskAvailableSize() {
        if (!existsSdcard()) return 0;
        File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        StatFs stat = new StatFs(path.getAbsolutePath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
        // (availableBlocks * blockSize)/1024 KIB 单位
        // (availableBlocks * blockSize)/1024 /1024 MIB单位
    }

    public static Boolean existsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static long getFileOrDirSize(File file) {
        if (!file.exists()) return 0;
        if (!file.isDirectory()) return file.length();

        long length = 0;
        File[] list = file.listFiles();
        if (list != null) { // 文件夹被删除时, 子文件正在被写入, 文件属性异常返回null.
            for (File item : list) {
                length += getFileOrDirSize(item);
            }
        }

        return length;
    }

    public static String getFileNameWithoutExtension(File file) {
        if (file == null) return "";

        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            return fileName.substring(0, pos);
        }

        return "";
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null) return "";
        if ( TextUtils.isEmpty(fileName) ) return "";

        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            return fileName.substring(0, pos);
        }

        return "";
    }

    /**
     *
     * @param url  "http://se.360.cn/hv/ios/opening/videozip/kuaishipin_opening.zip"
     * @return kuaishipin_opening.zip
     */
    public static String getFileNameByUrl(String url) {
        if (TextUtils.isEmpty(url)) return "";
        int pos = url.lastIndexOf("/");
        if (pos > 0 && url.length()>pos+1) {
            return url.substring(pos+1);
        }else{
            return url;
        }
    }

    /**
     * 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 获取扩展名
     * @return
     */
    public static String getFileNameExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int pos = filename.lastIndexOf('.');
            if (pos > 0) {
                return filename.substring(pos);
            }
        }
        return "";
    }

    /**
     * 获取文件的扩展名
     * @param file
     * @return
     */
    public static String getFileExtension(File file) {
        if (file == null) return "";

        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            return fileName.substring(pos);
        }

        return "";
    }

    /**
     * 获取文件的扩展名
     * @param filePath
     * @return
     */
    public static String getFileExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";

        int pos = filePath.lastIndexOf(".");
        if (pos > 0) {
            return filePath.substring(pos);
        }

        return "";
    }

    public static boolean isZip(String zipFilePath){
        if(TextUtils.isEmpty(zipFilePath))return false;
        String ex = getFileNameExtension(zipFilePath);
        if(TextUtils.isEmpty(ex))return false;
        ex = ex.substring(1);//去掉.
        if("rar".equals(ex) || "zip".equals(ex)){
            return true;
        }
        return false;
    }

    /**
     * 删除文件，可以是单个文件或文件夹
     *
     * @param fileName 待删除的文件名
     * @return 文件删除成功返回true, 否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {

                return deleteFile(fileName);
            } else {
                return deleteDirectory(fileName);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 被删除文件的文件名
     * @return 单个文件删除成功返回true, 否则返回false
     */
    private static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param dir 被删除目录的文件路径
     * @return 目录删除成功返回true, 否则返回false
     */
    private static boolean deleteDirectory(String dir) {
        //如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
            //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }

        if (!flag) {
            return false;
        }

        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static ArrayList<String> getAllFileNamesList(String fileDir) {
        ArrayList<String> list = new ArrayList<>();
        File file = new File(fileDir);
        if (!file.isDirectory()) {
            return null;
        }
        String[] names = file.list();
        if (names != null) {
            list.addAll(Arrays.asList(names));
            return list;
        }
        return null;

    }

    /**
     * 判断指定的dir目录是否为空，为空或不存在返回true，不为空返回false
     * @param dir
     * @return
     */
    public static boolean isFileDirEmpty(String dir){
        if(TextUtils.isEmpty(dir)){
            return true;
        }
        File file = new File(dir);
        if(!file.exists()){
            return true;
        }
        if (!file.isDirectory()) {
            return true;
        }
        if(file.listFiles().length == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 返回指定目录下所有文件名称列表，不包含子目录下的文件
     * @param fileDir
     * @return
     */
    public static ArrayList<String> getAllFilePathList(String fileDir) {
        ArrayList<String> list = new ArrayList<>();
        File file = new File(fileDir);
        if (!file.isDirectory()) {
            return null;
        }
        File[] files = file.listFiles();
        if (files == null || files.length==0) {
            return null;
        }
        //排序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (File f: fileList) {
            if(f.isFile()) {
                list.add(f.getAbsolutePath());
            }
        }
        return list;
    }


    public static void copy(String from, String to) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) return;

        FileChannel input;
        FileChannel output;

        try {
            input = new FileInputStream(new File(from)).getChannel();
            output = new FileOutputStream(new File(to)).getChannel();
            output.transferFrom(input, 0, input.size());

            input.close();
            output.close();
        } catch (Exception e) {
        }
    }


    public static boolean isIllegal(File file) {
        return file == null || !file.exists();
    }

    public static String readSDFile(File file) {
        String res = "";
        FileInputStream fis;
        try {
            if(file != null){
                fis = new FileInputStream(file);
                int length = fis.available();
                byte[] buffer = new byte[length];
                fis.read(buffer);
                res = new String(buffer, "UTF-8");
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public static boolean isWebp(File file) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[16];
            fis.read(buffer);
            fis.close();

            if (buffer[8] == 0x57 && buffer[9] == 0x45 && buffer[10] == 0x42 && buffer[11] == 0x50) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * 创建 文件夹
     * @param dirPath 文件夹路径
     * @return 创建成功返回true,失败返回false
     */
    public static boolean createDir(String dirPath) {

        File dir = new File(dirPath);
        //文件夹是否已经存在
        if (dir.exists()) {
            return true;
        }
        //创建文件夹
        if (dir.mkdirs()) {
            return true;
        }

        return false;
    }

    public static void sort(List<File> list) {
        if (list == null || list.size() == 0) return;
        try {
            Collections.sort(list, new FileComparator());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FileComparator implements Comparator<File> {
        public int compare(File file1, File file2) {
            if (file1.lastModified() > file2.lastModified()) {
                return -1;
            } else if (file1.lastModified() < file2.lastModified()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * 判断是图片文件还是视频文件
     * @param path
     * @return 图片返回"pic",视频返回"video"
     */
    public static String isPicOrVideo(String path){
        String suffix = null;
        if(TextUtils.isEmpty(path)){
            return "";
        }
        if (!path.contains(".")) {
            return "";
        }
        int pos = path.lastIndexOf(".");
        if (pos > 0 && path.length()>pos+1) {
            suffix = path.substring(pos+1);
        }
        try {
            suffix = suffix.toUpperCase();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
        //判断是否是图片
        //JPEG、TIFF、RAW、BMP、GIF、PNG
        String[] picSuffixArray = {"JPEG","JPG","TIFF","RAW","BMP","GIF","PNG"};
        List<String> picList = Arrays.asList(picSuffixArray);
        if(picList.contains(suffix)){
            return "pic";
        }
        //判断是否是视频
        //wmv、asf、asx、rm、 rmvb、mp4、3gp、mov、m4v、avi、dat、mkv、flv、vob
        String[] videoSuffixArray = {"WMV","ASF","ASX","RM","RMVB","MP4","3GP","MOV","M4V","AVI","DAT","MKV","FLV","VOB","WEBM"};
        List<String> videoList = Arrays.asList(videoSuffixArray);
        if(videoList.contains(suffix)){
            return "video";
        }
        return "other";
    }

    public static FileTypeEnum getFileType(String path){
        String type = isPicOrVideo(path);
        if("pic".equals(type)){
            return FileTypeEnum.PIC;
        }else if("video".equals(type)){
            return FileTypeEnum.VIDEO;
        }else if("other".equals(type)){
            return FileTypeEnum.OTHER;
        }else{
            return FileTypeEnum.OTHER;
        }
    }

    public static String getUniqueFilePath(String path, String shortName) {
        final File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // normalized, exist '/' in front of shortname

        File nor = new File(path, shortName);
        String s = nor.getName();

        int num = 0;
        final String ext = _getFileExtension(s);
        final String name = _getFileTitle(s);
        File file = new File(path, s);
        while (file.exists()) {
            num++;
            file = new File(path, name + "-" + num + ext);
        }

        String result = file.getAbsolutePath();
        return result;
    }

    private static String _getFileExtension(final String path) {
        if (path != null && path.lastIndexOf('.') != -1) {
            return path.substring(path.lastIndexOf('.'));
        }
        return null;
    }

    private static String _getFileTitle(String shortName) {
        return shortName.substring(0, shortName.lastIndexOf('.'));
    }

    public enum FileTypeEnum {
        /**
         * 压缩文件
         */
        ZIP(0,"压缩文件"),
        /**
         * 图片
         */
        PIC(1,"PIC"),
        /**
         * 视频
         */
        VIDEO(2,"VIDEO"),
        /**
         * 未知
         */
        OTHER(99,"UN_KNOWN");

        public int id;
        public String name;

        // 定义一个带参数的构造器，枚举类的构造器只能使用 private 修饰
        FileTypeEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static FileMsg getFileMsg(String mUri) {
        long duration = 0;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();

        FileMsg fileMsg = new FileMsg();
        try {
            if (mUri != null) {
                mmr.setDataSource(mUri);
            }

            duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            int width = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            if (width == 0 || height == 0) {
                duration = 0;
            }
            XLog.error("LightVideoHelper","width=" + width
                    + " height=" + height
                    + " duration=" + duration
            );

            fileMsg.duration = duration;
            fileMsg.width = width;
            fileMsg.height = height;
        } catch (Exception ex) {
            XLog.error("LightVideoHelper","Exception:" + ex);
        } finally {
            mmr.release();
        }


        return fileMsg;
    }

    public static class FileMsg{
        public long duration;
        public int width;
        public int height;

    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.MediaColumns.DATA};//{"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int column_index = cursor.getColumnIndexOrThrow(projection[0]);//"_data");
                        if(column_index > -1) {
                            return cursor.getString(column_index);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }else{
            return uri.toString();
        }
        return null;
    }

    /**
     * 将uri转换成真实路径
     *
     * @param selectedVideoUri
     * @param contentResolver
     * @return
     */
    public static String getFilePathFromContentUri(Uri selectedVideoUri,
                                                   ContentResolver contentResolver) {
        String filePath = "";
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn,
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getColumnIndex(filePathColumn[0]);
                if(id > -1)
                    filePath = cursor.getString(id);
            }
            cursor.close();
        }

        return filePath;
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


    public static void appendContentToFile(ArrayList<String> needAddFilePathList, String outFilePath){
        if(needAddFilePathList == null || needAddFilePathList.size() == 0)return;
        if(TextUtils.isEmpty(outFilePath))return;

        RandomAccessFile raf = null;
        try {

            raf = new RandomAccessFile(outFilePath,"rw");

            for (int i = 0; i < needAddFilePathList.size(); i++) {
                String filePath = needAddFilePathList.get(i);
                XLog.debug("mtest"," write to master filePath: "+filePath);
                if(TextUtils.isEmpty(filePath))continue;
                File needAddFile = new File(filePath);
                if(needAddFile == null || !needAddFile.exists() || !needAddFile.isFile())return;


                long length =  raf.length();
                raf.seek(length);

                FileInputStream bis = new FileInputStream(needAddFile);
                // 指定文件位置读取的文件流
                InputStream sbs = new BufferedInputStream(bis);

                byte buffer[] = new byte[4 * 1024];
                int len = 0;
                while ((len = sbs.read(buffer)) != -1)//
                {
                    raf.write(buffer, 0, len);
                }
            }


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    /**
     * 排序
     */
    public static List<String> sortBean(List<String> listBean){

        if(listBean.size() == 0) {
            return listBean;
        }
        //新的排序没有排序文件
        Collections.sort(listBean, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {

                try{
                    int separator1 = o1.lastIndexOf(File.separator);
                    int separator2 = o2.lastIndexOf(File.separator);
                    String o1Name = o1.substring(separator1+1);
                    String o2Name = o2.substring(separator2+1);

                    int value = Integer.valueOf(o1Name) - Integer.valueOf(o2Name);//o1Name.compareTo(o2Name);
                    if(value > 0){
                        value = 1;
                    }else if(value < 0){
                        value = -1;
                    }else if(value == 0){
                        value = 0;
                    }


                    return value;

                }catch (Exception e){
                    e.printStackTrace();
                    return o1.compareTo(o2);
                }

            }
        });
        return listBean;
    }


    /**
     * 文件分割方法
     *
     * @param srcFilePath 源文件Path
     * @param dstFilePath 分割文件的目标目录
     * @param count       分割个数
     */
    public static ArrayList<String> splitFile(String srcFilePath, String dstFilePath, int count) {
        ArrayList<String> tempFileList = new ArrayList<>();
        RandomAccessFile raf = null;
        try {
            //获取目标文件 预分配文件所占的空间 在磁盘中创建一个指定大小的文件   r 是只读
            raf = new RandomAccessFile(new File(srcFilePath), "r");
            long length = raf.length();//文件的总长度
            long maxSize = length / count;//文件切片后的长度
            long offSet = 0L;//初始化偏移量
            for (int i = 0; i < count - 1; i++) { //最后一片单独处理
                long begin = offSet;
                long end = (i + 1) * maxSize;
//                offSet = writeFile(file, begin, end, i);

                offSet = getWrite(srcFilePath, dstFilePath, i, begin, end,tempFileList);
            }
            if (length - offSet > 0) {
                getWrite(srcFilePath, dstFilePath, count - 1, offSet, length,tempFileList);
            }

            for (String path : tempFileList) {
                Log.d("mtest", "path :" + path);
            }
        } catch (FileNotFoundException e) {
            Log.e("TAG", "没有找到文件 srcFilePath:" + srcFilePath);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("TAG", "IOException");
            e.printStackTrace();
        } finally {
            try {
                if (raf != null)
                    raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tempFileList;
    }




    /**
     * 指定文件每一份的边界，写入不同文件中
     *
     * @param srcFilePath 源文件
     * @param dstFilePath 目标目录
     * @param index       源文件的顺序标识
     * @param begin       开始指针的位置
     * @param end         结束指针的位置
     * @return long
     */
    public static long getWrite(String srcFilePath, String dstFilePath, int index, long begin,
                                long end,ArrayList<String> tempFileList) {
        File srcFile = new File(srcFilePath);
        long endPointer = 0L;
        try {
            //申明文件切割后的文件磁盘
            RandomAccessFile in = new RandomAccessFile(new File(srcFilePath), "r");
            //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
            String path = dstFilePath + srcFile.getName()
                    .split("\\.")[0]
                    + "_" + index + ".tmp";

            //创建文件夹
            FileUtil.createDir(dstFilePath);
            RandomAccessFile out = new RandomAccessFile(new File(path), "rw");

            //申明具体每一文件的字节数组
            byte[] b = new byte[1024];
            int n = 0;
            //从指定位置读取文件字节流
            in.seek(begin);
            //判断文件流读取的边界
            while (in.getFilePointer() <= end && (n = in.read(b)) != -1) {
                //从指定每一份文件的范围，写入不同的文件
                out.write(b, 0, n);
            }
            //定义当前读取文件的指针
            endPointer = in.getFilePointer();
            //关闭输入流
            in.close();
            //关闭输出流
            out.close();

            if(tempFileList != null){
                tempFileList.add(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "getWrite Exception");
        }
        return endPointer;
    }


    /**
     * @param srcFile 分割文件目录
     * @param dstFile 目标合并文件绝对路径
     */
    public static void merge(String srcFile, String dstFile) {
        File file = new File(srcFile);
        if (file != null && file.exists() && file.listFiles().length > 0) {
            merge(dstFile, srcFile, file.listFiles().length);
        }
    }

    /**
     * 文件合并
     *
     * @param dstFile   指定合并文件
     * @param tempFile  分割前的目录
     * @param tempCount 文件个数
     */
    private static void merge(String dstFile, String tempFile, int tempCount) {
        RandomAccessFile raf = null;
        try {
            //申明随机读取文件RandomAccessFile
            raf = new RandomAccessFile(new File(dstFile), "rw");
            //开始合并文件，对应切片的二进制文件
            File splitFileDir = new File(tempFile);
            File[] files = splitFileDir.listFiles();
            for (int i = 0; i < tempCount; i++) {
                //读取切片文件

                RandomAccessFile reader = new RandomAccessFile(files[i], "r");
                byte[] b = new byte[1024];
                int n = 0;
                //先读后写
                while ((n = reader.read(b)) != -1) {//读
                    raf.write(b, 0, n);//写
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "merge Exception" + e.getMessage());
        } finally {
            try {
                if (raf != null)
                    raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getSplitCount(String path,long splitFileSize,int maxSplitCount){
        //计算拆分文件的个数 读取文件的大小
        if(TextUtils.isEmpty(path)){
            XLog.error("mtest","path 为空");
            return 0;
        }
        long fileSize = FileUtil.getFileOrDirSize(new File(path));
        if(fileSize <= 0){
            XLog.error("mtest","读取文件大小失败");
            return 0;
        }

        int count = (int)(fileSize%splitFileSize == 0 ? fileSize/splitFileSize : fileSize/splitFileSize +1);
        if(count > maxSplitCount){
            count = maxSplitCount;
        }

        return count;
    }
}
