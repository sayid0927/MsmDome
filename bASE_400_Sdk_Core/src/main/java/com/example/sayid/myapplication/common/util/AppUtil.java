package com.example.sayid.myapplication.common.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.example.sayid.myapplication.common.bean.AppRunningInfo;
import com.example.sayid.myapplication.common.bean.ApplicationInfoList;

import java.util.List;

public class AppUtil {
    private final static String TAG = "AppUtil";

    /**
     * 判断是否本机已安装了packageName制定的应用
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstalledApk(Context context, String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return false;
        }

        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (Exception e) {
//			Logs.e(TAG, "isInstalledApk error:" + e.toString());
            return false;
        }
        return true;
    }

    /**
     * 获取 监听短信广播的 应用
     *
     * @param context
     * @return
     */
    public static List<AppRunningInfo> getSmsPermissionApp(Context context) {
        ApplicationInfoList ai = new ApplicationInfoList(context);
        List<AppRunningInfo> list = ai.getHasPermissionRunningAppInfo("android.permission.RECEIVE_SMS");
        return list;
    }

    /**
     * 判断当前应用是否有某权限
     *
     * @param context
     * @param permission android.permission.WRITE_APN_SETTINGS
     * @return
     */
    public static boolean checkPermission(Context context, String permission) {
        try {
            context.enforceCallingOrSelfPermission(permission, "No permission to write APN settings");
        } catch (Exception e) {
//			Logs.e(TAG, "checkPermission error:" + e.toString());
            return false;
        }
        return true;
    }


    /**
     * 通过包名检查应用是否安装在 SD卡
     *
     * @param context
     * @param packageName
     * @return true:存在; false:不存在
     */
    public static boolean isInstalledApkOnSD(Context context, String packageName) {
        if (context == null || packageName == null || "".equals(packageName))
            return false;

        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            // 应用安装在sd卡
            if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                return true;
            }

            return false;
        } catch (Exception e) {
//			Logs.e(TAG, "isInstalledApkOnSD error:" + e.toString());
            return false;
        }
    }

    /**
     * 通过包名检查应用是否安装在 系统目录下
     *
     * @param context
     * @param packageName
     * @return true:存在; false:不存在
     */
    public static boolean isInstalledApkOnSystem(Context context, String packageName) {
        if (context == null || packageName == null || "".equals(packageName))
            return false;

        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            // 应用安装在system目录,或者是系统应用的更新
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    || (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true;
            }

            return false;
        } catch (Exception e) {
//			Logs.e(TAG, "isInstalledApkOnSystem error:" + e.toString());
            return false;
        }
    }
}