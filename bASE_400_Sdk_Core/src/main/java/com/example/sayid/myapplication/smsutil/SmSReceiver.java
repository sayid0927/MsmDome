package com.example.sayid.myapplication.smsutil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SmSReceiver extends BroadcastReceiver {

    public static SmsSendCallback mySendSmsListener;

    public void setSendSmsListener(SmsSendCallback mySendSmsListener) {
        this.mySendSmsListener = mySendSmsListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(SmSserver.SMS_RECEIVED)
                || intent.getAction().equals(SmSserver.SMS_RECEIVED_2)
                || intent.getAction().equals(SmSserver.GSM_SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                SmSutils.getInstance().abortBroadcastSms(bundle, this);
            }
        }

        if (intent.getAction().equals(SmSserver.SendState)) {
            SmSutils.SmsSendCallback(this, mySendSmsListener);
        }
    }
}
