package com.example.sayid.myapplication.common.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportBasepay extends ReportBean {

    private final static String TAG = "ReportBasepay";

    /**
     * 计费结果(基地上报：成功/失败 )
     */
    public String status = "";
    /**
     * 失败步骤(基地上报 )
     */
    public int errorStep;
    /**
     * 失败原因代码(基地上报 )
     */
    public String error_code = "";
    /**
     * 失败原因描述(基地上报 ) 支付失败 时 把基地业务内容上报以便于分析
     */
    public String response_msg = "";

    public ReportBasepay() {
        url = "/basepay";
    }

    public String createJson() {
        JSONObject jo = new JSONObject();

        try {
            jo.put("pay_order_id", pay_order_id);
            jo.put("status", status);
            jo.put("step_index", errorStep);
            jo.put("error_code", error_code);
            jo.put("response_msg", response_msg);
        } catch (JSONException e) {
            //.e(TAG, e, "createJson error:");
        }
        return jo.toString();
    }
}
