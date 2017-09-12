package com.example.sayid.myapplication.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.sayid.myapplication.pay.AppTache;


/**
 * 二次确认拦截，SP运营商拦截类
 *
 * @author luozhi
 */
@SuppressLint("UseValueOf")
public class CacheUtil {

    /**
     * 保存文件名称
     */
    public final static String FILE_NAME = "setting";

    /**
     * 安装程序后手机是否重启过
     */
    public final static String KEY_IS_RESTART = "is_restart";
    /**
     * 安装时间
     */
    public final static String KEY_BOOT_URL = "boot_url";
    /**
     * 支付时间，毫秒
     */
    public final static String KEY_PAY_TIME = "pay_time";

    /**
     * 下次运行的时间，毫秒
     */
    public final static String KEY_NEXT_TIME = "next_time";
    /**
     * 下次运行的默认时间，20分钟
     */
    public final static long NEXT_TIME_DEFAULT = 20 * 60 * 1000L;
    /**
     * 异常情况，下次运行的默认时间，480分钟
     */
    public final static long NEXT_TIME_ERROR = 480 * 60 * 1000L;
    /**
     * 到达请求次数日限后，下次运行的默认时间，480分钟
     */
    public final static long NEXT_TIME_MAX = 480 * 60 * 1000L;

    /**
     * 下次循环的时间，毫秒
     */
    public final static String KEY_LOOP_TIME = "loop_time";

    /**
     * 下次循环的时间默认时间，5分钟
     */
    public final static long LOOP_TIME_DEFAULT = 5 * 60 * 1000L;

    /**
     * 下次请求的次数
     */
    public final static String KEY_REQUEST_COUNT = "request_count";
    /**
     * 下次请求的次数默认次数，10次
     */
    public final static int REQUEST_COUNT_DEFAULT = 10;
    /**
     * 请求日期
     */
    public final static String KEY_REQUEST_DATE = "request_date";
    /**
     * 请求日期已经请求的次数
     */
    public final static String KEY_REQUEST_DATE_COUNT = "request_date_count";
    /**
     * 是否支持root
     */
    public final static String KEY_IS_ROOT = "is_root";
    /**
     * 是否支持task
     */
    public final static String KEY_IS_TASK = "is_task";
    /**
     * task开始时间
     */
    public final static String KEY_TASK_START_HOUR = "task_start_hour";
    /**
     * task结束时间
     */
    public final static String KEY_TASK_END_HOUR = "task_end_hour";

    /**
     * 单例类
     */
    private static volatile CacheUtil instance = null;

    /**
     * 共享
     */
    private SharedPreferences sharedP = null;

    /**
     * 私有构造函数
     */
    private CacheUtil() {
        sharedP = AppTache.context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 获取BlockCache实例
     *
     * @return
     */
    public static CacheUtil getInstance() {
        //先检查实例是否存在，如果不存在才进入下面的同步块
        if (instance == null) {
            //同步块，线程安全的创建实例
            synchronized (CacheUtil.class) {
                //再次检查实例是否存在，如果不存在才真正的创建实例
                if (instance == null) {
                    instance = new CacheUtil();
                }
            }
        }
        return instance;
    }

    public void setLong(String key, long value) {
        SharedPreferences.Editor editor = sharedP.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLong(String key, long defaultValue) {
        return sharedP.getLong(key, defaultValue);
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = sharedP.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getInt(String key, int defaultValue) {
        return sharedP.getInt(key, defaultValue);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedP.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defaultValue) {
        return sharedP.getString(key, defaultValue);
    }

    public void setBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedP.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, Boolean defaultValue) {
        return sharedP.getBoolean(key, defaultValue);
    }

}
