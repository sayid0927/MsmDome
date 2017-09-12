package com.example.sayid.myapplication.common.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;


public class ApplicationInfoList {

    private PackageManager pm;
    private Context context;

    public ApplicationInfoList(Context context) {
        this.context = context;
    }

    /**
     * 获取所有正在运行的应用 程序信息
     *
     * @return
     */
    public List<AppRunningInfo> getAllRunningAppInfo() {
        //		try{
        return getRunningAppInfo(0, null);
        //		} catch (Exception e) {
        //			T.warn("ApplicationInfoList：001:" + e.toString());
        //		}
        //		return null;
    }

    /**
     * 获取系统正在运行的应用 程序信息
     *
     * @return
     */
    public List<AppRunningInfo> getSystemRunningAppInfo() {
        //		try{
        return getRunningAppInfo(1, null);
        //		} catch (Exception e) {
        //			T.warn("ApplicationInfoList：002:" + e.toString());
        //		}
        //		return null;
    }

    /**
     * 获取第三方正在运行的应用 程序信息
     *
     * @return
     */
    public List<AppRunningInfo> getThirdPartyRunningAppInfo() {
        //		try{
        return getRunningAppInfo(2, null);
        //		} catch (Exception e) {
        //			T.warn("ApplicationInfoList：003:" + e.toString());
        //		}
        //		return null;
    }

    /**
     * 获取与某项权限有关的并且正在运行中的应用程序信息 (其中过滤了本应用程序，即不包含本应用程序)
     *
     * @param permissionName 权限的名称
     * @return
     */
    public List<AppRunningInfo> getHasPermissionRunningAppInfo(String permissionName) {
        //		try{
        return getRunningAppInfo(3, permissionName);
        //		} catch (Exception e) {
        //			T.warn("ApplicationInfoList：004:" + e.toString());
        //		}
        //		return null;
    }

    /**
     * 获取对应条件的正在运行的应用程序
     *
     * @param position ( 0:所有应用程序；1:系统的应用程序； 2:第三方应用程序；3:与某权限有关的应用程序 )
     * @return
     */
    private List<AppRunningInfo> getRunningAppInfo(int position, String permissionName) {
        pm = context.getPackageManager();
        // 获取所有已经安装的应用程序
        List<PackageInfo> listAppcations = pm.getInstalledPackages(0);

        // 保存所有正在运行的包名 以及它所在的进程信息
        Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
        List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            // 获得运行在该进程里的所有应用程序包
            String[] pkgNameList = appProcess.pkgList;

            // 输出所有应用程序的包名
            for (int i = 0; i < pkgNameList.length; i++) {
                String pkgName = pkgNameList[i];
                // 加入至map对象里
                pgkProcessAppMap.put(pkgName, appProcess);
            }
        }

        // 保存所有正在运行的应用程序信息
        List<AppRunningInfo> runningAppInfos = new ArrayList<AppRunningInfo>(); // 保存过滤查到的AppInfo

        // 获取相应条件的应用程序(0：所有/1：系统/2:第三方/3:与某权限有关的)
        switch (position) {
            case 0:
                //			for (PackageInfo app : listAppcations) {
                //				// 如果该包名存在 则构造一个RunningAppInfo对象
                //				if (pgkProcessAppMap.containsKey(app.packageName)) {
                //					runningAppInfos.add(getAppInfo(app));
                //				}
                //			}
                break;
            case 1:
                for (PackageInfo app : listAppcations) {
                    if (isSystemApp(app)) { // 过滤第三方应用
                        // 如果该包名存在 则构造一个RunningAppInfo对象
                        if (pgkProcessAppMap.containsKey(app.packageName)) {
                            runningAppInfos.add(getAppInfo(app));
                        }
                    }
                }
                break;
            case 2:
                //			for (PackageInfo app : listAppcations) {
                //				if (!isSystemApp(app)) { // 过滤系统应用
                //					// 如果该包名存在 则构造一个RunningAppInfo对象
                //					if (pgkProcessAppMap.containsKey(app.packageName)) {
                //						runningAppInfos.add(getAppInfo(app));
                //					}
                //				}
                //			}
                break;
            case 3:
                String packageName = context.getPackageName();
                for (PackageInfo app : listAppcations) {
                    try {
                        app = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
                    } catch (NameNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (isHave(app.requestedPermissions, permissionName.toLowerCase())
                    /*&& !packageName.equals(app.packageName)*/) { // 获取与某权限有关的应用
                        // 如果该包名存在 则构造一个RunningAppInfo对象
                        if (pgkProcessAppMap.containsKey(app.packageName)) {
                            boolean ismyapp = false;
                            if (packageName.equals(app.packageName)) {
                                ismyapp = true;
                            }
                            runningAppInfos.add(getAppInfo(app, ismyapp));
                        }
                    }
                }
                break;
        }

        return runningAppInfos;
    }

    /**
     * 构造一个RunningAppInfo对象 ，并赋值
     *
     * @param app
     * @return
     */
    private AppRunningInfo getAppInfo(PackageInfo app) {
        AppRunningInfo appInfo = new AppRunningInfo();
        appInfo.appName = (String) app.applicationInfo.loadLabel(pm);
        appInfo.packageName = app.packageName;
        appInfo.versionCode = app.versionCode;
        appInfo.versionName = app.versionName;
        return appInfo;
    }

    /**
     * 构造一个RunningAppInfo对象 ，并赋值
     *
     * @param app
     * @param isMyApp
     * @return
     */
    private AppRunningInfo getAppInfo(PackageInfo app, boolean isMyApp) {
        // 获取sdk版本
        int sdk = Integer.valueOf(Build.VERSION.SDK);

        AppRunningInfo appInfo = new AppRunningInfo();
        appInfo.appName = (String) app.applicationInfo.loadLabel(pm);
        appInfo.packageName = app.packageName;
        appInfo.versionCode = app.versionCode;
        appInfo.versionName = app.versionName;
        appInfo.isInstallSdcard = isSdcardApp(app, sdk);
        appInfo.isMyApp = isMyApp;

        if (sdk < 10) {
            appInfo.firstInstallTime = new Date(new File(app.applicationInfo.sourceDir).lastModified()).getTime();
            // appInfo.lastUpdateTime = 0;
        } else {    //sdk2.3(含)以上可用
            //			appInfo.firstInstallTime = app.firstInstallTime;
            //			appInfo.lastUpdateTime = app.lastUpdateTime;
        }

        return appInfo;
    }

    /**
     * 判断是否是系统软件或者是系统软件的更新软件
     *
     * @param pInfo
     * @return
     */
    private boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }


    /**
     * 此方法有两个参数，第一个是要查找的字符串数组，第二个是要查找的字符或字符串
     *
     * @param strs
     * @param s
     * @return
     */
    private boolean isHave(String[] strs, String s) {
        if (strs != null && strs.length > 0) {
            for (int i = 0; i < strs.length; i++) {
                if (strs[i].toLowerCase().indexOf(s) != -1) {// 循环查找字符串数组中的每个字符串中是否包含所有查找的内容
                    return true;// 查找到了就返回真，不在继续查询
                }
            }
        }
        return false;// 没找到返回false
    }

    /**
     * 判断应用是否是安装在SD卡上面，是返回true
     *
     * @param pInfo
     * @param sdk
     * @return
     */
    private boolean isSdcardApp(PackageInfo pInfo, int sdk) {
        String installLocation = pInfo.applicationInfo.sourceDir.toLowerCase();
        if (sdk < 8) {
            if (installLocation.startsWith("/system/app/")
                    || installLocation.startsWith("/data/app/")
                    || installLocation.startsWith("/system/framework/")) {
                return false;
            }
            return true;
        }
        //sdk2.2(含)以上可用
        return false;
        //		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0);
    }

}
