package com.example.sayid.myapplication.common.bean;

/**
 * 应用程序信息
 *
 * @author
 */
public class AppRunningInfo {
    /**
     * 应用程序名称
     */
    public String appName;
    /**
     * 应用程序所对应的包名
     */
    public String packageName;
    /**
     * 版本名称
     */
    public String versionName;
    /**
     * 版本号
     */
    public int versionCode;
    /**
     * 判断应用程序是否安装在SD卡上面 ,是为true
     */
    public boolean isInstallSdcard;
    /**
     * 第一次安装时间
     */
    public long firstInstallTime;
    /**
     * 最后一次更新时间
     */
    public long lastUpdateTime;
    /**
     * 是否为自己的应用
     */
    public boolean isMyApp;
}
