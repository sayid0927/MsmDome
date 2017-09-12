package com.example.sayid.myapplication.common.listener;

public interface SendSmsListener {
    /**
     * 短信发送成功
     *
     * @param user_order_id
     * @param pay_order_id
     * @param destPhone
     * @param message
     * @param items
     */
    public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int items);

    /**
     * 短信发送失败
     *
     * @param user_order_id
     * @param pay_order_id
     * @param destPhone
     * @param message
     * @param items
     * @param errorCode
     */
    public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode);
}
