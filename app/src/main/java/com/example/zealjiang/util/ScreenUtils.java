package com.example.zealjiang.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.example.zealjiang.MyApplication;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 屏幕相关工具类（屏幕信息、dp，px，sp转换，截图等）
 */
public final class ScreenUtils {

    private static float density;

    /**
     * Don't let anyone instantiate this class.
     */
    private ScreenUtils() {
        throw new Error("Do not need instantiate!");
    }

    // == ----------------------------------------- ==

    /**
     * 通过上下文获取屏幕宽度
     *
     * @param mContext
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context mContext) {
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
            if (dMetrics != null) {
                return dMetrics.widthPixels;
            }
            // 这种也可以获取，不过已经提问过时(下面这段可以注释掉)
            WindowManager wManager = ProUtils.getWindowManager(mContext);
            if (wManager != null) {
                return wManager.getDefaultDisplay().getWidth();
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public static int getScreenRealHeight(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getScreenHeight(context);
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return getScreenHeight(context);
        }

        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        return Math.max(point.x, point.y);
    }

    @SuppressWarnings("deprecation")
    public static int getScreenWidthDP(Context mContext) {
        int width = getScreenWidth(mContext);
        return pxConvertDip(mContext,width);
    }


    @SuppressLint("PrivateApi")
    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public static int getStatusBarHeight2(Context context) {
        if(context == null)return -1;
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 通过上下文获取屏幕高度
     *
     * @param mContext
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context mContext) {
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = mContext.getResources().getDisplayMetrics();
            if (dMetrics != null) {
                return dMetrics.heightPixels;
            }
            // 这种也可以获取，不过已经提示过时(下面这段可以注释掉)
            WindowManager wManager = ProUtils.getWindowManager(mContext);
            if (wManager != null) {
                return wManager.getDefaultDisplay().getHeight();
            }
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 通过上下文获取屏幕宽度高度
     *
     * @param mContext
     * @return int[] 0 = 宽度，1 = 高度
     */
    @SuppressWarnings("deprecation")
    public static int[] getScreenWidthHeight(Context mContext) {
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
            if (dMetrics != null) {
                return new int[]{dMetrics.widthPixels, dMetrics.heightPixels};
            }
            // 这种也可以获取，不过已经提示过时(下面这段可以注释掉)
            WindowManager wManager = ProUtils.getWindowManager(mContext);
            if (wManager != null) {
                int width = wManager.getDefaultDisplay().getWidth();
                int height = wManager.getDefaultDisplay().getHeight();
                return new int[]{width, height};
            }
        } catch (Exception e) {
        }
        return null;
    }


    public static int getRealScreenHeight(Context mContext){
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager wManager = ProUtils.getWindowManager(mContext);
        if (wManager != null) {
            wManager.getDefaultDisplay().getRealMetrics(outMetrics);
            int widthPixel = outMetrics.widthPixels;
            int heightPixel = outMetrics.heightPixels;

            return heightPixel;
        }

        return -1;
    }

    /**
     * 通过上下文获取屏幕密度
     *
     * @param mContext
     * @return
     */
    public static float getDensity(Context mContext) {
        if(density > 0){
            return density;
        }
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
            if (dMetrics != null) {
                // 屏幕密度（0.75 / 1.0 / 1.5 / 2.0）
                return dMetrics.density;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 通过上下文获取屏幕密度Dpi
     *
     * @param mContext
     * @return
     */
    public static int getDensityDpi(Context mContext) {
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
            if (dMetrics != null) {
                // 屏幕密度DPI（120 / 160 / 240 / 320）
                return dMetrics.densityDpi;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param mContext
     * @param dpValue
     */
    public static int dipConvertPx(Context mContext, float dpValue) {
        try {
            float scale = mContext.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } catch (Exception e) {
        }
        return -1;
    }

    public static int dipConvertPx(float dpValue) {
        try {
            float scale = MyApplication.getContext().getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param mContext
     * @param pxValue
     */
    public static int pxConvertDip(Context mContext, float pxValue) {
        try {
            float scale = mContext.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
     *
     * @param mContext
     * @param pxValue
     */
    public static int pxConvertSp(Context mContext, float pxValue) {
        try {
            float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
            return (int) (pxValue / scale + 0.5f);
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px
     *
     * @param mContext
     * @param spValue
     */
    public static int spConvertPx(Context mContext, float spValue) {
        try {
            float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
            return (int) (spValue * scale + 0.5f);
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 第二种
     *
     * @param mContext
     * @param dpValue
     */
    public static int dipConvertPx2(Context mContext, float dpValue) {
        try {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, mContext.getResources().getDisplayMetrics());
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px 第二种
     *
     * @param mContext
     * @param spValue
     */
    public static int spConvertPx2(Context mContext, float spValue) {
        try {
            // android.util.TypedValue
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, mContext.getResources().getDisplayMetrics());
        } catch (Exception e) {
        }
        return -1;
    }

    // == ----------------------------------------- ==

    /**
     * 获得状态栏的高度(无关 android:theme 获取状态栏高度)
     *
     * @param mContext
     * @return
     */
    public static int getStatusHeight(Context mContext) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            return mContext.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 获取应用区域 TitleBar 高度 （顶部灰色TitleBar高度，没有设置 android:theme 的 NoTitleBar 时会显示）
     *
     * @param mContext
     * @return
     */
//    public static int getStatusBarHeight(Activity activity) {
//        try {
//            Rect rect = new Rect();
//            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//            return rect.top;
//        } catch (Exception e) {
//        }
//        return -1;
//    }

    /**
     * 获取当前屏幕截图，包含状态栏 （顶部灰色TitleBar高度，没有设置 android:theme 的 NoTitleBar 时会显示）
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        try {
            View view = activity.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bmp = view.getDrawingCache();
            int[] sParams = getScreenWidthHeight(activity);
            Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, sParams[0], sParams[1]);
            view.destroyDrawingCache();
            return bitmap;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏 (如果 android:theme 全屏了，则截图无状态栏)
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        try {
            View view = activity.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bmp = view.getDrawingCache();
            int[] sParams = getScreenWidthHeight(activity);

            int statusBarHeight = getStatusBarHeight(activity);
            if (statusBarHeight == -1) {
                statusBarHeight = 0;
            }
            Bitmap bitmap = Bitmap.createBitmap(bmp, 0, statusBarHeight, sParams[0], sParams[1] - statusBarHeight);
            view.destroyDrawingCache();
            return bitmap;
        } catch (Exception e) {
        }
        return null;
    }

    // == ----------------------------------------- ==

    /**
     * 计算视频宽高大小，视频比例xxx*xxx按屏幕比例放大或者缩小
     *
     * @param mContext 上下文
     * @param width    高度比例
     * @param height   宽度比例
     * @return 返回宽高 0 = 宽，1 = 高
     */
    public static int[] reckonVideoWidthHeight(float width, float height, Context mContext) {
        try {
            // 获取屏幕宽度
            int sWidth = ScreenUtils.getScreenWidth(mContext);
            // 判断宽度比例
            float wRatio = 0.0f;
            // 计算比例
            wRatio = (sWidth - width) / width;
            // 等比缩放
            int nWidth = sWidth;
            int nHeight = (int) (height * (wRatio + 1));
            return new int[]{nWidth, nHeight};
        } catch (Exception e) {
        }
        return null;
    }


    /**
     * 判断某个界面是否在前台
     *
     * @param activity 要判断的Activity
     * @return 是否在前台显示
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isForeground(Activity activity) {
        return isForeground(activity, activity.getClass().getName());
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className))
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName()))
                return true;
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isTopActivity(Context  mContext){
        ActivityManager  activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String  packageName = mContext.getPackageName();
        List<ActivityManager.RunningTaskInfo>  tasksInfo = activityManager.getRunningTasks(1);
        if(tasksInfo.size() > 0){
            //应用程序位于堆栈的顶层
            if(packageName.equals(tasksInfo.get(0).topActivity.getPackageName())){
                mContext=null;
                return true;
            }
        }
        mContext=null;
        return false;
    }

    /**
     * 屏幕防止息屏
     */
    public static void setScreenLight(Activity activity){
        if(activity == null)return;
        //保持屏幕高亮
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 解除 禁止屏幕息屏
     */
    public static void setScreenLightOff(Activity activity){
        if(activity == null)return;
        //解除屏幕高亮
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

