package com.example.sayid.myapplication.common.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.Logs;
import com.example.sayid.myapplication.common.util.TelephonyUtil;
import com.example.sayid.myapplication.pay.AppTache;

public class ReportSms extends ReportBean {

    private final static String TAG = "ReportSms";

    /**
     * 短信 条数
     */
    public int item;
    /**
     * 当前为第几条
     */
    public int index;
    /**
     * imsi
     */
    public String imsi;
    /**
     * 短信 上行端口
     */
    public String channel_port = "";
    /**
     * 短信 上行指令
     */
    public String channel_order = "";

    /**
     * 计费结果(基地上报：成功/失败 )
     */
    public String status = "";
    /**
     * 失败原因代码(基地上报 )
     */
    public String error_code = "";

    public ReportSms() {
        url = "/sms";
    }

    public String createJson() {
        JSONObject jo = new JSONObject();

        try {
            jo.put("pay_order_id", pay_order_id);
            jo.put("item", item);
            jo.put("index", index);
            jo.put("channel_port", channel_port);
            jo.put("channel_order", channel_order);
            jo.put("imsi", TelephonyUtil.getImsi(AppTache.context));
            jo.put("status", status);
            jo.put("error_code", error_code);
        } catch (JSONException e) {
//			Logs.e(TAG, e, "createJson error:");
        }
        return jo.toString();
    }
}
