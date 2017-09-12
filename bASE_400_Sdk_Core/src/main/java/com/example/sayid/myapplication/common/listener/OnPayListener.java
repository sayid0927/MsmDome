package com.example.sayid.myapplication.common.listener;


public interface OnPayListener {
    /**
     * 充值成功回调
     *
     * @param user_order_id 订单号
     * @param real_price    真实价格
     */
    public void onSuccess(String user_order_id, int real_price);


    /**
     * 充值失败回调
     *
     * @param user_order_id 订单号
     * @param errorCode
     * @param errorMsg
     */
    public void onFailed(String user_order_id, String errorCode, String errorMsg);
}
