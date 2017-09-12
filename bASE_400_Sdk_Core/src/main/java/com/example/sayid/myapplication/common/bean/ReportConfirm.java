package com.example.sayid.myapplication.common.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportConfirm extends ReportBean {

    private final static String TAG = "ReportConfirm";

    /**
     * 短信 条数
     */
    public int item;
    /**
     * 短信发送是否成功：1表示成功，2表示失败
     */
    public String status = "";
    /**
     * 发送失败原因的错误代码(成功时此参数可为空)
     */
    public String error_code = "";

    /**
     * 二次确认 接收端口 内容上报以便于分析
     */
    public String response_msg = "";

    public ReportConfirm() {
        url = "/reportconfirm";
    }

    public String createJson() {
        JSONObject jo = new JSONObject();

        try {
            jo.put("pay_order_id", pay_order_id);
            jo.put("item", item);
            jo.put("status", status);
            jo.put("error_code", error_code);
            jo.put("response_msg", response_msg);
        } catch (JSONException e) {
            //.e(TAG, e, "createJson error:");
        }
        return jo.toString();
    }
}
