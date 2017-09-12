package com.example.sayid.myapplication.smsutil;


import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.example.sayid.myapplication.common.util.PreferUtil;

public class SmSserver extends Service {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String SMS_RECEIVED_2 = "android.provider.Telephony.SMS_RECEIVED_2";
    public static final String GSM_SMS_RECEIVED = "android.provider.Telephony.GSM_SMS_RECEIVED";
    public static final String SendState = "SMS_SENT";
    public static final String DELIVERED = "SMS_DELIVERED";
    private SmSReceiver localMessageReceiver1;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(SmSserver.SMS_RECEIVED);
        localIntentFilter.addAction(SmSserver.SMS_RECEIVED_2);
        localIntentFilter.addAction(SmSserver.GSM_SMS_RECEIVED);
        localIntentFilter.setPriority(2147483647);
        localMessageReceiver1 = new SmSReceiver();
        this.registerReceiver(localMessageReceiver1, localIntentFilter, "android.permission.BROADCAST_SMS", null);
        PreferUtil.getInstance().setMonitorTime();

    }

    /***
     * 销毁时重新启动Service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopForeground(true);
        this.unregisterReceiver(localMessageReceiver1);
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_REDELIVER_INTENT;
        return super.onStartCommand(intent, flags, startId);
    }

}
