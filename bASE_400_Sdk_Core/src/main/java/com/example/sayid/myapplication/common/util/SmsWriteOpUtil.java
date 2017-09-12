package com.example.sayid.myapplication.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.KITKAT)
public final class SmsWriteOpUtil {
    private final static String TAG = "SmsWriteOpUtil";
    private final static int OP_WRITE_SMS = 15;

    public static boolean isWriteEnabled(Context context) {
        int uid = getUid(context);
        Object opRes = checkOp(context, OP_WRITE_SMS, uid);

        if (opRes instanceof Integer) {
            return (Integer) opRes == AppOpsManager.MODE_ALLOWED;
        }
        return false;
    }

    public static boolean setWriteEnabled(Context context, boolean enabled) {
        int uid = getUid(context);
        int mode = enabled ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_IGNORED;

        return setMode(context, OP_WRITE_SMS, uid, mode);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object checkOp(Context context, int code, int uid) {
//        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
//        appOpsManager.setMode(15, android.os.Process.myUid(), context.getPackageName(), 0);
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        Class appOpsManagerClass = appOpsManager.getClass();

        try {
            Class[] types = new Class[3];
            types[0] = Integer.TYPE;
            types[1] = Integer.TYPE;
            types[2] = String.class;
            Method checkOpMethod = appOpsManagerClass.getMethod("checkOp", types);

            Object[] args = new Object[3];
            args[0] = Integer.valueOf(code);
            args[1] = Integer.valueOf(uid);
            args[2] = context.getPackageName();
            Object result = checkOpMethod.invoke(appOpsManager, args);

            return result;
        } catch (NoSuchMethodException e) {
            //.e(TAG, e, "checkOp error");
        } catch (InvocationTargetException e) {
            //.e(TAG, e, "checkOp error");
        } catch (IllegalAccessException e) {
            //.e(TAG, e, "checkOp error");
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean setMode(Context context, int code, int uid, int mode) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        Class appOpsManagerClass = appOpsManager.getClass();

        try {
            Class[] types = new Class[4];
            types[0] = Integer.TYPE;
            types[1] = Integer.TYPE;
            types[2] = String.class;
            types[3] = Integer.TYPE;
            Method setModeMethod = appOpsManagerClass.getMethod("setMode", types);

            Object[] args = new Object[4];
            args[0] = Integer.valueOf(code);
            args[1] = Integer.valueOf(uid);
            args[2] = context.getPackageName();
            args[3] = Integer.valueOf(mode);
            setModeMethod.invoke(appOpsManager, args);

            return true;
        } catch (NoSuchMethodException e) {
            //.e(TAG, e, "setMode error");
        } catch (InvocationTargetException e) {
            //.e(TAG, e, "setMode error");
        } catch (IllegalAccessException e) {
            //.e(TAG, e, "setMode error");
        }
        return false;
    }

    private static int getUid(Context context) {
        try {
            int uid = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_SERVICES).uid;

            return uid;
        } catch (PackageManager.NameNotFoundException e) {
            //.e(TAG, e, "getUid error");
            return 0;
        }
    }
}