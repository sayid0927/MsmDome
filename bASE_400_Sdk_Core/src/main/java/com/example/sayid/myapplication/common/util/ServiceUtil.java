package com.example.sayid.myapplication.common.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.example.sayid.myapplication.common.data.ConfigConst;

import java.util.ArrayList;

public class ServiceUtil {
    private final static String TAG = "ServiceUtil";

    /**
     * 定时启动服务
     *
     * @param context
     * @param loopTime
     */
    public static void taskService(Context context, long loopTime) {
        try {
            String serviceClassName = ConfigConst.getServiceClassName(context);
            if (serviceClassName != null && !"".equals(serviceClassName)) {
                Class<Service> classObject = (Class<Service>) Class.forName(serviceClassName);
                //.d(TAG, "ServiceClassName--------------->"+ classObject.getName());

                PendingIntent sender = PendingIntent.getService(context, 0, new Intent(context, classObject), 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                //.d(TAG, "获取后台的轮询时间为：" + loopTime / 60 / 1000 + "分钟后");
                long pollingtime = System.currentTimeMillis() + loopTime;
                alarmManager.set(AlarmManager.RTC_WAKEUP, pollingtime, sender);
            }
        } catch (Exception e) {
            //.e(TAG, e, "定时启动服务 error:");
        }
    }

    /**
     * 动态注册服务
     *
     * @param context
     */
    public static void startService(Context context) {
        try {
            String serviceClassName = ConfigConst.getServiceClassName(context);
            if (serviceClassName != null && !"".equals(serviceClassName)) {
                if (!ServiceUtil.isServiceAlive(context, serviceClassName)) {
                    Class<Service> classObject = (Class<Service>) Class.forName(serviceClassName);
                    //.d(TAG, "ServiceClassName--------------->" + classObject.getName());

                    Intent service = new Intent(context, classObject);
                    context.startService(service);
                } else {
                    //.d(TAG, "接收到广播，但是Service已启动");
                }
            }
        } catch (Exception e) {
            //.e(TAG, e, "startService error:");
        }
    }

    /**
     * 判断某个Service是否在运行
     *
     * @param context
     * @return
     */
    public static boolean isServiceAlive(Context context, String serviceName) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager.getRunningServices(300);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

}