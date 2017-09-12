package com.example.sayid.myapplication.parseStep.step.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.parseStep.step.MyHeader;
import com.example.sayid.myapplication.parseStep.step.request.MyResponse;

/**
 * 获取手机号码
 *
 * @author zorro
 */
public class Phone extends MyResponseStep implements Cloneable, Serializable {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     *
     */
    public String url;
    /**
     * 请求方法
     */
    public String method = "get";
    /**
     * 请求次数
     */
    public int times;
    /**
     * 请求内容
     */
    public String content;

    /**
     * 等级，越高，可信度越高
     */
    public int channel;
    /**
     * 变量名称，获取的手机号存入此变量中
     */
    public String variable;

    /**
     * 请求头，值支持变量， 如{phone}表示phone
     */
    public List<MyHeader> request_headers;


    public Phone() {
        super();
        actionID = PHONE;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        url = JsonUtil.isNullOrGetStr(jsonObj, "url");
        method = JsonUtil.isNullOrGetStr(jsonObj, "method");
        times = JsonUtil.isNullOrGetInt(jsonObj, "times");
        content = JsonUtil.isNullOrGetStr(jsonObj, "content");
        channel = JsonUtil.isNullOrGetInt(jsonObj, "channel");
        variable = JsonUtil.isNullOrGetStr(jsonObj, "variable");

        if (!jsonObj.isNull("request_headers")) {
            JSONArray jrHead = jsonObj.optJSONArray("request_headers");
            if (jrHead != null) {
                request_headers = new ArrayList<MyHeader>();
                int s = jrHead.length();
                for (int k = 0; k < s; k++) {
                    JSONObject json = (JSONObject) jrHead.get(k);
                    MyHeader header = new MyHeader();
                    header.parseJson(json);
                    request_headers.add(header);
                }
            }
        }

        // 解析response
        super.parseJson(jsonObj);
    }

    @Override
    public Object clone() {
        Phone obj = (Phone) super.clone();
        obj.request_headers = new ArrayList<MyHeader>();
        Iterator<MyHeader> header_it = this.request_headers.iterator();
        while (header_it.hasNext()) {
            obj.request_headers.add((MyHeader) (header_it.next().clone()));
        }
        obj.response = (MyResponse) this.response.clone();

        return obj;
    }
}
