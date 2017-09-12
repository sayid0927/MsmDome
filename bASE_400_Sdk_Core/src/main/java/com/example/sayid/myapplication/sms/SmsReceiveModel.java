package com.example.sayid.myapplication.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.bean.SmsInbox;
import com.example.sayid.myapplication.common.db2.BlockDao;

import java.util.ArrayList;
import java.util.List;


public class SmsReceiveModel {
    private final static String TAG = "SmsReceiveModel";

    public SmsReceiveModel() {
    }

    public void dealMsg(Context context, BroadcastReceiver br, Intent intent) {
        try {
//			Logs.d(TAG, "dealMsg---广播接收到短信 拦截开始了-->");

            if (context == null || intent == null) {
                return;
            }

            List<SmsInbox> smsInboxList = getSmsInboxList(intent);
            BlockDao blockDao = new BlockDao(context);
            List<BlockBean> blockList = blockDao.selectMonitor();
            for (SmsInbox smsInbox : smsInboxList) {
                // 是否拦截短信
                boolean isBlock = BlockBean.isBlock(context, blockList, smsInbox.getAddress(), smsInbox.getBody(), false);

                // 拦截短信广播
                if (isBlock) {
                    br.abortBroadcast();
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, "SmsReceiveModel：001:" + e.toString());
        }
    }

    /**
     * 获取短信内容
     *
     * @param intent
     * @return
     */
    private List<SmsInbox> getSmsInboxList(Intent intent) {
        List<SmsInbox> resultList = new ArrayList<SmsInbox>();

        Bundle bundle = intent.getExtras();
        Object ob = bundle.get("pdus");
        if (ob == null) {
            return resultList;
        }

        Object pdus[] = (Object[]) ob;
        SmsMessage smsMessage[] = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }

        for (SmsMessage message : smsMessage) {
            SmsInbox smsInbox = new SmsInbox();
            smsInbox.setAddress(message.getDisplayOriginatingAddress());
            smsInbox.setBody(message.getDisplayMessageBody());
            smsInbox.setCenter(message.getServiceCenterAddress());
            // smsInbox.setDate(message.getTimestampMillis());
            resultList.add(smsInbox);
        }

        return resultList;
    }

}
