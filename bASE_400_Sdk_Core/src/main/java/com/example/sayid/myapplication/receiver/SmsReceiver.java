package com.example.sayid.myapplication.receiver;


import com.example.sayid.myapplication.common.util.Logs;
import com.example.sayid.myapplication.sms.SmsReceiveModel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * 静态广播-用于监听广播，删除短信
 *
 * @author xukejun
 * @version [V1.0.0, 2012-10-16]
 */
public class SmsReceiver extends BroadcastReceiver {
    private final static String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//		Logs.d(TAG, "start receive--->");
        // 4.4 19 广播拦截无效，使用监听数据库方式
//		if (Build.VERSION.SDK_INT < 19) {
        new SmsReceiveModel().dealMsg(context, this, intent);
//		}
    }

}
