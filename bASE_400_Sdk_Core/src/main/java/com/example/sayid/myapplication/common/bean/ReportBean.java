package com.example.sayid.myapplication.common.bean;

public class ReportBean {
    /**
     * Id
     */
    public int id;
    /**
     * 订单号
     */
    public String pay_order_id = "";
    /**
     * 请求路径
     */
    public String url;
    /**
     * json字符串
     */
    public String jsonData;
    /**
     * 是否加密
     */
    public boolean secret = true;
    /**
     * 是否检查的上报
     */
    public boolean isCheck = false;

    /**
     * 创建Json字符
     *
     * @return
     */
    public String createJson() {
        return "";
    }
}