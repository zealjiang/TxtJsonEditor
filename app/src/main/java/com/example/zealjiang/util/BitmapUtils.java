package com.example.zealjiang.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zealjiang.MyApplication;
import com.example.zealjiang.bean.Point;
import com.example.zealjiang.util.log.XLog;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    public static final String IMAGE_FILE_URI_PREFIX = "file:///";
    public static final String IMAGE_CONTENT_URI_PREFIX = "content://media/external/images/media";
    public static final String EXTENSION_JPG = ".jpg";
    public static final String EXTENSION_JPEG = ".jpeg";
    public static final String EXTENSION_PNG = ".png";
    public static final int TRY_GET_BITMAP_FROM_VIEW_MAX_REPEAT_TIME = 2;
    public static final String SCALE_16_9 = "16:9";
    public static final String SCALE_1_1 = "1:1";
    public static final String SCALE_9_16 = "9:16";

    public static int computeSampleSize1(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int roundedSize = 1;
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            int sample1 = options.outWidth / reqWidth;
            int sample2 = options.outHeight / reqHeight;
            roundedSize = sample1 < sample2 ? sample1 : sample2;
        }
        if (roundedSize < 1) {
            roundedSize = 1;
        }
        return roundedSize;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;

            for (int halfWidth = width / 2; halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth; inSampleSize *= 2) {
                ;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeFile(Context context, String pathName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        if(width <= 0 || height <= 0)return null;
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;


        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        return bitmap;
    }

    /*
     * return bitmap maybe same as src
     */
    public static Bitmap createScaledBitmap(Bitmap src, float scale, boolean filter) {
        return createScaledBitmap(src, (int) (src.getWidth() * scale), (int) (src.getHeight() * scale), filter);
    }

    /*
     * return bitmap maybe same as src
     */
    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        try {
            return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, filter);
        } catch (OutOfMemoryError e) {
            handleOutOfMemory();
            try {
                return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, filter);
            } catch (Throwable t) {
                return null;
            }
        } catch (Throwable t) {
            return null;
        }
    }

    public static Bitmap createScaledBitmapByScreen(Bitmap src,boolean filter) {
        if(src == null && src.getWidth() <= 0 && src.getHeight() <= 0)return src;
        try {
            int screenWidth = ScreenUtils.getScreenWidth(MyApplication.getContext());
            int screenHeight = ScreenUtils.getScreenHeight(MyApplication.getContext());

            int width = src.getWidth();
            int height = src.getHeight();

            if(width >screenWidth || height > screenHeight){
                int scale = 1;
                if(width > screenWidth && height <= screenHeight){
                   scale = width/screenWidth;
                }else if(width <= screenWidth && height > screenHeight){
                    scale = height/screenHeight;
                }else if(width > screenWidth && height > screenHeight){
                   int wscale = width/screenWidth;
                   int hscale = height/screenHeight;

                   scale = wscale > hscale ? wscale : hscale;
                }

                if(scale == 1)return src;
                Bitmap b = Bitmap.createScaledBitmap(src, src.getWidth()/scale, src.getHeight()/scale, filter);
                if(b != null){
                    src.recycle();
                    src = null;
                }
                return b;
            }

            return src;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return src;
    }

    public static Bitmap decodeFile(String pathName, boolean lowQualityFlag) {
        if (pathName == null || !new File(pathName).exists()) {
            return null;
        }

        BitmapFactory.Options op = new BitmapFactory.Options();
        if (lowQualityFlag) {
            op = getLowQualityOptions(op);
        }
        op.inDensity = DisplayMetrics.DENSITY_HIGH;
        op.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

        try {
            return BitmapFactory.decodeFile(pathName, op);
        } catch (OutOfMemoryError e) {
            try {
                handleOutOfMemory();
                return BitmapFactory.decodeFile(pathName, op);
            } catch (OutOfMemoryError e2) {
                handleOutOfMemory();
                return null;
            }
        }
    }

    private static void handleOutOfMemory() {

    }

    public static Bitmap decodeFileToBitmap(File bitmapfile, int requiredwidth, int requiredheight, boolean lowQualityFlag) {
        if (!bitmapfile.exists()) {
            return null;
        }
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(bitmapfile.getAbsolutePath(), op);
            int width = op.outWidth;
            int height = op.outHeight;
            if (requiredwidth <= 0)
                requiredwidth = width;
            if (requiredheight <= 0)
                requiredheight = height;
            int scale = 1;
            while (true) {
                if (width / 2 < requiredwidth || height / 2 < requiredheight) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }
            op.inSampleSize = scale;
            op.inJustDecodeBounds = false;
            if (lowQualityFlag) {
                op = getLowQualityOptions(op);
            }

            Bitmap bitmap = BitmapFactory.decodeFile(bitmapfile.getAbsolutePath(), op);
            Bitmap scaledBitmap = createScaledBitmap(bitmap, requiredwidth, requiredheight, true);
            if (bitmap != scaledBitmap) {
                bitmap.recycle();
            }
            return scaledBitmap;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Bitmap decodeFileToScaleBitmap(String filePath, int requiredwidth, int requiredheight, boolean lowQualityFlag) {

        File bitmapfile = new File(filePath);
        if (bitmapfile == null || !bitmapfile.exists()) {
            return null;
        }
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(bitmapfile.getAbsolutePath(), op);
            int width = op.outWidth;
            int height = op.outHeight;
            if (requiredwidth <= 0)
                requiredwidth = width;
            if (requiredheight <= 0)
                requiredheight = height;
            int scale = 1;
            while (true) {
                if (width / 2 < requiredwidth || height / 2 < requiredheight) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }
            op.inSampleSize = scale;
            op.inJustDecodeBounds = false;
            if (lowQualityFlag) {
                op = getLowQualityOptions(op);
            }

            Bitmap bitmap = BitmapFactory.decodeFile(bitmapfile.getAbsolutePath(), op);
            Bitmap scaledBitmap = createBitmapByScale(bitmap, requiredwidth, requiredheight);
            if (bitmap != scaledBitmap) {
                bitmap.recycle();
            }
            return scaledBitmap;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    //不失真 按比例压缩图片
    private static Bitmap createBitmapByScale(Bitmap target, int newWidth, int newHeight) {
        int width = target.getWidth();
        int height = target.getHeight();

        float scaleWidth = 0;
        float scaleHeight = 0;
        if (newWidth < width || newHeight < height) {
            if (width >= height) {
                scaleWidth = ((float) newWidth) / width;
                scaleHeight = scaleWidth;
            } else {
                scaleHeight = ((float) newHeight) / height;
                scaleWidth = scaleHeight;
            }
        } else {
            return target;
        }

        // 计算缩放比例
//		float scaleWidth = ((float) newWidth) / width;
//		float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        if(width <= 0 || height <=0 )return target;
        Bitmap newbm = Bitmap.createBitmap(target, 0, 0, width, height, matrix,
                true);
        if (target != null && !target.equals(newbm) && !target.isRecycled()) {
            target.recycle();
        }
        return newbm;
    }

    private static byte[] inputStream2ByteArr(InputStream inputStream, long nFileLen) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[(int) nFileLen];
        int len = 0;
        while ((len = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, len);
        }
        inputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static Bitmap decodeBuffToBitmap(byte[] data, int requiredwidth, int requiredheight, boolean lowQualityFlag) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;

        try {

            // BitmapFactory.decodeStream(bitmapInputStream, null, op);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, op);
            if (op.outHeight == -1 || op.outWidth == -1)
                return null;

            int width = op.outWidth;
            int height = op.outHeight;
            if (requiredwidth <= 0)
                requiredwidth = width;
            if (requiredheight <= 0)
                requiredheight = height;
            int scale = 1;
            while (true) {
                if (width / 2 < requiredwidth || height / 2 < requiredheight) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }
            op.inSampleSize = scale;
            if (lowQualityFlag) {
                op = getLowQualityOptions(op);
            }
            // bitmapInputStream.reset();
            op.inJustDecodeBounds = false;
            // Bitmap result = BitmapFactory.decodeStream(bitmapInputStream,
            // null, op);
            Bitmap result = BitmapFactory.decodeByteArray(data, 0, data.length, op);
            data = null;
            Bitmap scaledBitmap = createScaledBitmap(result, requiredwidth, requiredheight, true);
            if (scaledBitmap != result) {
                recycleBitmap(result);
            }
            return scaledBitmap;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeInputStreamToBitmap(InputStream bitmapInputStream, long nFileLen, int requiredwidth, int requiredheight, boolean lowQualityFlag) {
        // if (!bitmapInputStream.markSupported()) {
        // bitmapInputStream = new BufferedInputStream(bitmapInputStream);
        // }
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;

        try {

            if (nFileLen == 0) {
                nFileLen = 1024 * 1024;
            }

            byte[] data = inputStream2ByteArr(bitmapInputStream, nFileLen);

            // BitmapFactory.decodeStream(bitmapInputStream, null, op);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, op);
            int width = op.outWidth;
            int height = op.outHeight;
            if (requiredwidth <= 0)
                requiredwidth = width;
            if (requiredheight <= 0)
                requiredheight = height;
            int scale = 1;
            while (true) {
                if (width / 2 < requiredwidth || height / 2 < requiredheight) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }
            op.inSampleSize = scale;
            if (lowQualityFlag) {
                op = getLowQualityOptions(op);
            }
            // bitmapInputStream.reset();
            op.inJustDecodeBounds = false;
            // Bitmap result = BitmapFactory.decodeStream(bitmapInputStream,
            // null, op);
            Bitmap result = BitmapFactory.decodeByteArray(data, 0, data.length, op);
            data = null;
            Bitmap scaledBitmap = createScaledBitmap(result, requiredwidth, requiredheight, true);
            if (scaledBitmap != result) {
                recycleBitmap(result);
            }
            return scaledBitmap;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeResource(Resources res, int id, int scaleSize, boolean lowQualityFlag) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inSampleSize = scaleSize;

        if (lowQualityFlag) {
            op = getLowQualityOptions(op);
        }
        return BitmapFactory.decodeResource(res, id, op);
    }

    public static Bitmap decodeStream(InputStream is, boolean lowQualityFlag) {
        BitmapFactory.Options op = null;
        if (lowQualityFlag) {
            op = getLowQualityOptions(null);
        }
        return op == null ? BitmapFactory.decodeStream(is) : BitmapFactory.decodeStream(is, null, op);
    }

    public static Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts = null;
            // if (width > 0 && height > 0)
            {
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                if (width == 0 || height == 0) {
                    width = opts.outWidth;
                    height = opts.outHeight;
                }
                // 璁＄畻鍥剧墖缂╂斁姣斾緥
                // final int minSideLength = Math.min(width, height);
                // opts.inSampleSize = computeSampleSize(opts, minSideLength,
                // width * height);
                opts.inSampleSize = computeSampleSize1(opts, width, height);
                if (opts.inSampleSize <= 1) {
                    opts.inSampleSize = 1;
                }
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;
            }

            try {
                Bitmap bmp = BitmapFactory.decodeFile(dst.getPath(), opts);
                return bmp;
            } catch (Throwable e) {
                e.printStackTrace();
                try {
                    for (int i = 0; i < 20; i++) {
                        System.gc();
                        System.runFinalization();
                    }
                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bmp = BitmapFactory.decodeFile(dst.getPath(), opts);
                    return bmp;
                } catch (Throwable e3) {
                    e3.printStackTrace();
                }
            }
        }
        return null;
    }

    public static BitmapFactory.Options getLowQualityOptions(BitmapFactory.Options op) {
        if (op == null) {
            op = new BitmapFactory.Options();
        }
        op.inPurgeable = true;
        op.inPreferredConfig = Bitmap.Config.RGB_565;
        op.inDither = false;
        op.inInputShareable = true;
        return op;
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 将文字 生成 文字图片 生成显示编码的Bitmap
     *
     * @param contents
     * @param context
     * @return
     */
    public static Bitmap creatCodeBitmap(String contents, int color, boolean hasShadow, Context context) {
        //float scale = context.getResources().getDisplayMetrics().scaledDensity;
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setTextSize(11);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(color);
        if (hasShadow) {
            tv.setShadowLayer(1, 3, 3, Color.GRAY);
        }
        tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.setBackgroundColor(Color.TRANSPARENT);

        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        return bitmapCode;
    }

    /**
     * 保存方法
     */
    public static void saveBitmap(Bitmap bm) {
        File f = new File("/sdcard/LiveEngine", System.currentTimeMillis() + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 保存方法
     */
    public static void saveBitmap(Bitmap bm, String desPath) {

        if (bm == null) {
            //XLog.debug(BitmapUtils.class.getName(), "saveBitmap bm is null");
            return;
        }
        File f = new File(desPath);
        if (f != null && f.exists()) {
            f.delete();
        }

        try {
            if(f != null){

                if(!f.getParentFile().exists()){
                    f.getParentFile().mkdirs();
                }
                if(!f.exists()){
                    f.createNewFile();
                }

            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            XLog.debug("Kevin", "save sticker bitmap success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveBitmap_jpg(Bitmap bm, String desPath) {

        if (bm == null) {
            //XLog.debug(BitmapUtils.class.getName(), "saveBitmap bm is null");
            return;
        }
        File f = new File(desPath);
        if (f != null && f.exists()) {
            f.delete();
        }

        try {
            if(f != null){

                if(!f.getParentFile().exists()){
                    f.getParentFile().mkdirs();
                }
                if(!f.exists()){
                    f.createNewFile();
                }

            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            XLog.debug("Kevin", "save sticker bitmap success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveBitmapByLossless(Bitmap bm, String desPath) {

        if (bm == null) {
            //XLog.debug(BitmapUtils.class.getName(), "saveBitmap bm is null");
            return;
        }
        File f = new File(desPath);
        if (f != null && f.exists()) {
            f.delete();
        }

        try {
            if(f != null){

                if(!f.getParentFile().exists()){
                    f.getParentFile().mkdirs();
                }
                if(!f.exists()){
                    f.createNewFile();
                }

            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            XLog.debug("Kevin", "save sticker bitmap success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Bitmap getBitmapFromView(View v) {
        if (v == null) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        Bitmap bitmap = v.getDrawingCache();
        return bitmap;
    }

    public static Bitmap convertViewToBitmap(View view) {
        if(view == null)return null;
        view.setDrawingCacheEnabled(true);
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache(false);
        if(bitmap == null){
            return null;
        }

        //XLog.debug(SubtitleTextViewHelper.class.getName(),"  bitmap size kb "+bitmap.getByteCount()/1024);
        //XLog.debug(SubtitleTextViewHelper.class.getName(),"  width: "+bitmap.getWidth()+"   height: "+bitmap.getHeight());
        return bitmap;
    }

    public static Bitmap convertViewToBitmap(View view,int width,int height) {
        if(view == null)return null;
        Bitmap bitmap;
        // 创建对应大小的bitmap
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.scale(5,5);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap convertViewToBitmap(View view,float scale) {
        if(view == null || scale < 0)return null;
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        float newWidth = view.getMeasuredWidth() * scale;
        float newHeight = view.getMeasuredHeight() * scale;
        if(newWidth == 0 || newHeight == 0)return null;
        Bitmap bitmap = null;
        // 创建对应大小的bitmap
        //此处如果尺寸过大可能导致内存溢出 outOfMemory
        try{
            //构造一个空的bitmap用来存放字幕
            bitmap = Bitmap.createBitmap((int)newWidth, (int)newHeight, Bitmap.Config.ARGB_8888);
            Log.d("convertViewToBitmap"," newWidth: "+newWidth+"  newHeight: "+newHeight);
        }catch (Throwable e){
            XLog.error("convertViewToBitmap"," newWidth: "+newWidth+"  newHeight: "+newHeight);
            e.printStackTrace();
            e.getMessage();
        }
        if(bitmap == null)return bitmap;

        final Canvas canvas = new Canvas(bitmap);
        canvas.scale(scale,scale);
        view.draw(canvas);
        return bitmap;
    }

    public static void saveFile(Bitmap bm, String dirName, String fileName) {
        File dirFile = new File(dirName);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        try {
            File myCaptureFile = new File(dirName + "/" + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.PNG, 80, bos);
            bos.flush();
            bos.close();
            bm.recycle();
            Log.i("zxt", "贴纸开始保存成功 ");

        } catch (Exception e) {

        }

    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        if(width <=0 || height <= 0 || ratio <= 0)return origin;
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
        if (origin.equals(newBM)) {
            return newBM;
        }
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    public static Bitmap scaleBitmap(Bitmap origin, float ratio, boolean recycle) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        if(width <=0 || height <= 0 || ratio <= 0)return origin;
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
        if (origin.equals(newBM)) {
            return newBM;
        }
        if ( recycle ) {
            origin.recycle();
        }
        return newBM;
    }


    /**
     * 选择变换
     *
     * @param origin 原图
     * @param rotate  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float rotate) {
        if (origin == null) {
            return null;
        }

        if (rotate != 0) {
            if(origin.getWidth() <= 0 || origin.getHeight() <= 0){
                return origin;
            }
            Bitmap newBitmap = Bitmap.createBitmap(origin.getWidth(),origin.getHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(origin,0,0,null);

            int width = origin.getWidth();
            int height = origin.getHeight();
            Matrix matrix = new Matrix();
            matrix.setRotate(rotate);
            // 围绕原地进行旋转
            if(width <= 0 || height <= 0)return origin;
            Bitmap newBM = Bitmap.createBitmap(newBitmap, 0, 0, width, height, matrix, true);
            if (origin.equals(newBM)) {
                return newBM;
            }
            origin.recycle();
            newBitmap.recycle();
            return newBM;
        } else {
            return origin;
        }


    }


/*    public static Bitmap rotateBitmap(Bitmap baseBitmap,float degrees) {
        // 创建一个和原图一样大小的图片
        Bitmap afterBitmap = Bitmap.createBitmap(baseBitmap.getWidth(),
                baseBitmap.getHeight(), baseBitmap.getConfig());
        Canvas canvas = new Canvas(afterBitmap);
        Matrix matrix = new Matrix();
        // 根据原图的中心位置旋转
        matrix.setRotate(degrees, baseBitmap.getWidth()/2,
                baseBitmap.getHeight()/2);
        canvas.drawBitmap(baseBitmap, matrix, new Paint());
        baseBitmap.recycle();
        return afterBitmap;
    }*/

    /**
     * 裁剪
     *
     * @param bitmap 原图
     * @param sw     显示区宽
     * @param sh     显示区高
     * @param x      bitmap left_top
     * @param y      bitmap top
     * @return 裁剪后的图像
     */
    public static void clipBitmap(Bitmap bitmap, int sw, int sh, float x, float y, ClipBitmapCallBack clipBitmapCallBack) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

//        //计算视频区域的左上坐标
//        int videoWidth= VideoSettingController.getInstance().getResolutionWidth();
//        int videoHeight=VideoSettingController.getInstance().getResolutionHeight();
//        int videoX=sw/2-videoWidth/2;
//        int videoY=sh/2-videoHeight/2;
        Point.Rect rect = Point.getIntersectionRect(new Point.Rect(new Point(0, 0), new Point(sw, sh)), new Point.Rect(new Point(x, y), new Point(x + w, y + h)));
        Point p1 = rect.p1;
        Point p2 = rect.p2;
        if ((int) (p2.x - p1.x) <= 0 || (int) (p2.y - p1.y) <= 0) {
            return;
        }
        Bitmap result = Bitmap.createBitmap(bitmap, (int) (p1.x - x), (int) (p1.y - y), (int) (p2.x - p1.x), (int) (p2.y - p1.y), null, false);
        bitmap.recycle();
        clipBitmapCallBack.result(result, p1.x, p1.y);


    }
    public static void clipBitmapPos(int width,int height, int sw, int sh, float x, float y, ClipOutCallBack clipOutCallBack) {
        int w = width; // 得到图片的宽，高
        int h = height;

        Point.Rect rect = Point.getIntersectionRect(new Point.Rect(new Point(0, 0), new Point(sw, sh)), new Point.Rect(new Point(x, y), new Point(x + w, y + h)));
        Point p1 = rect.p1;
        Point p2 = rect.p2;
        if ((int) (p2.x - p1.x) <= 0 || (int) (p2.y - p1.y) <= 0) {
            return;
        }
        clipOutCallBack.result((int) (p2.x - p1.x), (int) (p2.y - p1.y), p1.x, p1.y);
    }

    public interface ClipBitmapCallBack {
        void result(Bitmap bitmap, float x, float y);
    }

    public interface ClipOutCallBack {
        void result(int outWidth, int outHeight, float x, float y);
    }

    public static Bitmap getBitmap(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    public static Bitmap compressBitmap(Bitmap image,int targetWidth,int targetHeight){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(isBm, null, newOpts);
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        Bitmap bitmap;
        newOpts.inJustDecodeBounds = false;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;

        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f


        if(targetWidth > 0 && targetHeight > 0){
            hh = targetHeight;
            ww = targetWidth;
        }

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例

        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;
    }


    public static Bitmap compressBitmap(Bitmap image,int minInSampleSize){

        if(image == null)return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(isBm, null, newOpts);
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        Bitmap bitmap;
        newOpts.inJustDecodeBounds = false;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;


        int sampleSize = computeSize(w,h);
        if(sampleSize < minInSampleSize){
            sampleSize = minInSampleSize;
        }
        newOpts.inSampleSize = sampleSize;//设置缩放比例

        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;
    }

    private static int computeSize(int srcWidth,int srcHeight) {

        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide >= 1664 && longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    public static Bitmap setBitmapSize(Bitmap bm, int newWidth ,int newHeight){
        // 获得图片的宽高.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    /**
     * 获取图片旋转角度
     *
     * @param filePath
     * @return
     */
    public static int getRotateAngle(String filePath) {
        int angle = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    angle = 0;
                    break;
            }
        } catch (Throwable ex) {

        }
        return angle;
    }

    /**
     * 旋转图片
     *
     * @param source
     * @param angle
     * @param recycleSource
     * @return
     */
    public static Bitmap rotate(Bitmap source, int angle, boolean recycleSource) {
        Bitmap result = null;

        if (angle != 0) {

            Matrix m = new Matrix();
            m.setRotate(angle);
            try {
                result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), m, true);
            } catch (Throwable ex) {

            }
        }

        if (result != null) {
            if (recycleSource && result != source) {
                source.recycle();
                source = null;
            }
        } else {
            result = source;
        }
        return result;
    }

    /**
     * Return bitmap.
     *
     * @param filePath The path of file.
     * @return bitmap
     */
    public static Bitmap getBitmapOrigin(final String filePath) {
        if (isSpace(filePath)) return null;
        return BitmapFactory.decodeFile(filePath);
    }

   public static Bitmap drawable2Bitmap(Drawable drawable,int width,int height) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else{
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            width,
                            height,
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        }
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the clipped bitmap.
     *
     * @param src     The source of bitmap.
     * @param x       The x coordinate of the first pixel.
     * @param y       The y coordinate of the first pixel.
     * @param width   The width.
     * @param height  The height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the clipped bitmap
     */
    public static Bitmap clip(final Bitmap src,
                              final int x,
                              final int y,
                              final int width,
                              final int height,
                              final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = Bitmap.createBitmap(src, x, y, width, height);
        if (recycle && !src.isRecycled()) src.recycle();
        return ret;
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }



    public static double getScaleWH(Bitmap backBitmap, Bitmap frontBitmap) {
        if ((backBitmap.getWidth() > backBitmap.getHeight() && frontBitmap.getWidth() > frontBitmap.getHeight())
                || (backBitmap.getWidth() < backBitmap.getHeight() && frontBitmap.getWidth() < frontBitmap.getHeight())) {
            if ((double) backBitmap.getWidth() / (double) frontBitmap.getWidth() >= (double) backBitmap.getHeight() / (double) frontBitmap.getHeight()) {
                return (double) frontBitmap.getHeight() / (double) backBitmap.getHeight();
            } else if ((double) backBitmap.getWidth() / (double) frontBitmap.getWidth() < (double) backBitmap.getHeight() / (double) frontBitmap.getHeight()) {
                return (double) frontBitmap.getWidth() / (double) backBitmap.getWidth();
            }
        } else if (backBitmap.getWidth() >= backBitmap.getHeight() && frontBitmap.getWidth() <= frontBitmap.getHeight()) {
            return (double) frontBitmap.getHeight() / (double) backBitmap.getHeight();
        } else if ((backBitmap.getWidth() < backBitmap.getHeight() && frontBitmap.getWidth() >= frontBitmap.getHeight()) ||
                (backBitmap.getWidth() == backBitmap.getHeight() && frontBitmap.getWidth() > frontBitmap.getHeight())) {
            return (double) frontBitmap.getWidth() / (double) backBitmap.getWidth();
        }
        return 1;
    }

    public static Bitmap addBackground(Bitmap backBitmap, Bitmap frontBitmap) {
        Paint paint = new Paint();
        Canvas canvas = new Canvas(backBitmap);
        int b1w = backBitmap.getWidth();
        int b1h = backBitmap.getHeight();
        int b2w = frontBitmap.getWidth();
        int b2h = frontBitmap.getHeight();
        int bx = (b1w - b2w) / 2;
        int by = (b1h - b2h) / 2;
        canvas.drawBitmap(frontBitmap, bx, by, paint);//叠加新图b2 并且居中
        canvas.save();
        canvas.restore();
        return backBitmap;
    }

}
