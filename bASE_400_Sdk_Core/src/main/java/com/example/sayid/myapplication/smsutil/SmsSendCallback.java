package com.example.sayid.myapplication.smsutil;

/**
 * Created on 2017/8/14.
 */

public interface SmsSendCallback {


    void onSendSmsSuccess();


    void onSendSmsFailed(String errorcode);

}
