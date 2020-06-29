package com.example.zealjiang.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import java.util.LinkedHashSet;
import java.util.Set;

public class PermissionUtil {

    public final static int REQUEST_PERMISSION = 1;
    static Set<String> permissionsSet = new LinkedHashSet<>();
    static Set<String> permissionsSet_now = new LinkedHashSet<>();

    static {
        permissionsSet.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionsSet.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //permissionsSet.add(Manifest.permission.INTERNET);
    }

    public boolean checkPermission(Context mContext) {
        for (String permission_str : permissionsSet) {
            boolean mPermission = checkPermissionGranted(mContext, permission_str);
            if (mPermission) {

            } else {
                permissionsSet_now.add(permission_str);
            }
        }

        if (permissionsSet_now.size() <= 0) {
            return true;
        }

        if(permissionsSet_now.size() > 0) {
            String[] permission_str_array = permissionsSet_now.toArray(new String[]{});
            //请求权限
            try {
                ActivityCompat.requestPermissions((Activity) mContext,
                        permission_str_array,
                        REQUEST_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static boolean checkPermissionGranted(Context context, String permission) {
        // Android 6.0 以前，全部默认授权
        boolean result = true;
        int targetSdkVersion = 21;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= 23, 使用Context#checkSelfPermission
                result = context.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < 23, 需要使用 PermissionChecker
                result = PermissionChecker.checkSelfPermission(context, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }
        return result;
    }

    public boolean onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (permissions.length > 0 && grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (i < grantResults.length) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                permissionsSet_now.remove(permissions[i]);
                            }
                        }
                    }
                }

                //发现报错只发生在6.0这个版本和部分6.0.1版本中。
                //This was an Android 6.0 bug. It's fixed in Android 6.0.1, requestNetwork() can be called if you request CHANGE_NETWORK_STATE in the manifest. No need to call requestPermissions(), it's a normal permission.
                //--if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (android.os.Build.VERSION.SDK_INT == 23 || android.os.Build.VERSION.SDK_INT == 24) {
                    if (permissionsSet_now.size() <= 2) {
                        try {
                            permissionsSet_now.remove("android.permission.WRITE_SETTINGS");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            permissionsSet_now.remove("android.permission.CHANGE_NETWORK_STATE");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //XLog.debug("mtest","  permissionsSet_now.size()： "+permissionsSet_now.size()+"  isShowGuide： "+isShowGuide);
                if (permissionsSet_now.size() <= 0) {
                    //已授权
                    return true;
                } else {
                    //未授权
                    return false;
                }
            default:
                return false;
        }
    }
}
