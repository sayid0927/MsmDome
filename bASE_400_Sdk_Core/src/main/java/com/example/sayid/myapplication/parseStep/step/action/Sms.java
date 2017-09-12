package com.example.sayid.myapplication.parseStep.step.action;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.sms.SendSms;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 发短信
 *
 * @author zorro
 */
public class Sms extends Step {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 发送短信类型：
     * 0 = 普通短信
     * 1 = 数据短信
     */
    public String type;
    /**
     * 数据短信发送端口
     */
    public short destPort;
    /**
     * 数据短信字符编码名称
     */
    public String charsetName;

    /**
     * 接收手机号码
     */
    public String receiver;
    /**
     * 发送的内容
     */
    public String msg;


    public Sms() {
        actionID = SMS;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        type = JsonUtil.isEmptyOrGetStr(jsonObj, "type", SendSms.TYPE_NORMA);
        destPort = JsonUtil.isNullOrGetShort(jsonObj, "destPort");
        charsetName = JsonUtil.isEmptyOrGetStr(jsonObj, "charsetName", CHARSET_NAME);
        receiver = JsonUtil.isNullOrGetStr(jsonObj, "receiver");
        msg = JsonUtil.isNullOrGetStr(jsonObj, "msg");
    }

}
