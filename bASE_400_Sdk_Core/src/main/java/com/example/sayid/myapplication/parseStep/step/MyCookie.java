package com.example.sayid.myapplication.parseStep.step;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.Logs;

/**
 * 请求，响应 cookie
 *
 * @author zorro
 */
public class MyCookie implements Cloneable, Serializable {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    public String name;
    public String variable;
    public String value;


    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        name = JsonUtil.isNullOrGetStr(jsonObj, "name");
        value = JsonUtil.isNullOrGetStr(jsonObj, "value");
    }

    @Override
    public Object clone() {
        MyCookie obj = null;
        try {
            obj = (MyCookie) super.clone();
        } catch (CloneNotSupportedException e) {
            //.e("MyCookie", e, "clone not support error:");
        }
        return obj;
    }
}
