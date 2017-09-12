package com.example.sayid.myapplication.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.sayid.myapplication.pay.AppBaseService;


/**
 * 静态广播-用于监听广播，启动应用service
 *
 * @author zorro
 * @version [V1.0.0, 2016-05-10]
 */
public class ConnectionReceiver extends BroadcastReceiver {
    private final static String TAG = "ConnectionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//		Logs.d(TAG, "start service--->");

        // 启动服务
        Intent serviceIntent = new Intent(context, AppBaseService.class);
        serviceIntent.putExtra("data_type", "conection");
        context.startService(serviceIntent);
    }

}
