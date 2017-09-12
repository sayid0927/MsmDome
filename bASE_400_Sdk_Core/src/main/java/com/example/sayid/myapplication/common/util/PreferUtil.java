/**
 * Copyright(C)2012-2013 深圳市掌星立意科技有限公司版权所有
 * 创 建 人:Jacky
 * 修 改 人:
 * 创 建日期:2013-7-25
 * 描    述:xml储存数据
 * 版 本 号:
 */
package com.example.sayid.myapplication.common.util;

import android.content.Context;
import android.content.SharedPreferences;


public final class PreferUtil {

    public static PreferUtil INSTANCE;
    private static SharedPreferences mPrefer;
    private static final String APP_NAME = "com.ab.zf.sharedPreferences";

    private static final String MONITOR_TIME = "monitor_time";

    private static final String ISCONFIRMFIRST = "isconfirmfirst";

    private static final String SMSRUNNABLE = "smsrunnable";

    private PreferUtil() {
    }

    public static PreferUtil getInstance() {
        if (INSTANCE == null) {
            return new PreferUtil();
        }
        return INSTANCE;
    }

    public void init(Context ctx) {
        mPrefer = ctx.getSharedPreferences(APP_NAME, Context.MODE_WORLD_READABLE
                | Context.MODE_WORLD_WRITEABLE);
        mPrefer.edit().commit();
    }


    public String getString(String key, String defValue) {
        return mPrefer.getString(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return mPrefer.getInt(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPrefer.getBoolean(key, defValue);
    }

    public void putString(String key, String value) {
        mPrefer.edit().putString(key, value).commit();
    }

    public void putInt(String key, int value) {
        mPrefer.edit().putInt(key, value).commit();
    }

    public void putBoolean(String key, boolean value) {
        mPrefer.edit().putBoolean(key, value).commit();
    }

    public void putLong(String key, long value) {
        mPrefer.edit().putLong(key, value).commit();
    }

    public long getLong(String key, long defValue) {
        return mPrefer.getLong(key, defValue);
    }

    public void removeKey(String key) {
        mPrefer.edit().remove(key).commit();
    }


    /**
     * 设置当前 时间为监控时间开始
     */

    public void setMonitorTime() {
        putLong(MONITOR_TIME, System.currentTimeMillis());
    }

    public long getMonitorTime() {
        return getLong(MONITOR_TIME, 0);
    }

    /**
     * 设置当前 时间为监控时间开始
     */


    public void setConfirmFirst() {
        putBoolean(ISCONFIRMFIRST, true);
    }


    public boolean getConfirmFirst() {
        return getBoolean(ISCONFIRMFIRST, false);
    }

}
