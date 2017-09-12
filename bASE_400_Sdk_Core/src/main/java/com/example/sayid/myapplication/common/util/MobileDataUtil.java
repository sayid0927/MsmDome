package com.example.sayid.myapplication.common.util;

import android.net.ConnectivityManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MobileDataUtil {
    private final static String TAG = "MobileDataUtil";
    private static Method sMethodGetMobileDataEnabled;
    private static Method sMethodSetMobileDataEnabled;

    static {
        initReflectionMethod();
    }

    private static void initReflectionMethod() {
        Class<ConnectivityManager> clazz = ConnectivityManager.class;
        try {
            sMethodGetMobileDataEnabled = clazz.getMethod("getMobileDataEnabled", new Class[0]);
            sMethodGetMobileDataEnabled.setAccessible(true);
            sMethodSetMobileDataEnabled = clazz.getMethod("setMobileDataEnabled", new Class[]{Boolean.TYPE});
            sMethodSetMobileDataEnabled.setAccessible(true);
        } catch (SecurityException e) {
//			Logs.e(TAG, "MobileDataUtil：001:" + e.toString());
        } catch (NoSuchMethodException e) {
//			Logs.e(TAG, "MobileDataUtil：002:" + e.toString());
        }
    }

    /**
     * 获取当前GPRS开关是否打开
     */
    public static boolean getMobileDataEnabled(ConnectivityManager manager) {
        try {
            return (Boolean) sMethodGetMobileDataEnabled.invoke(manager, new Object[0]);
        } catch (IllegalArgumentException e) {
//			Logs.e(TAG, "MobileDataUtil：003:" + e.toString());
        } catch (IllegalAccessException e) {
//			Logs.e(TAG, "MobileDataUtil：004:" + e.toString());
        } catch (InvocationTargetException e) {
//			Logs.e(TAG, "MobileDataUtil：005:" + e.toString());
        }
        return false;
    }

    /**
     * 打开，关闭 GPRS开关
     */
    public static void setMobileDataEnabled(ConnectivityManager manager, boolean enabled) {
//		System.out.println(" manager "+ manager);
        try {

//			System.out.println(" sMethodSetMobileDataEnabled "+ sMethodSetMobileDataEnabled);
            sMethodSetMobileDataEnabled.invoke(manager, new Object[]{Boolean.valueOf(enabled)});
        } catch (IllegalArgumentException e) {
//			System.out.println(" 打开，关闭 GPRS开关 "+ e.toString());
//			Logs.e(TAG, "MobileDataUtil：006:" + e.toString());
        } catch (IllegalAccessException e) {
//			System.out.println(" 打开，关闭 GPRS开关 "+ e.toString());
//			Logs.e(TAG, "MobileDataUtil：007:" + e.toString());
        } catch (InvocationTargetException e) {
//			System.out.println(" 打开，关闭 GPRS开关 "+ e.toString());
//			Logs.e(TAG, "MobileDataUtil：008:" + e.toString());
        }
    }
}
