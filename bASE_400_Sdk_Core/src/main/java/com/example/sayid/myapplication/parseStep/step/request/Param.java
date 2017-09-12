package com.example.sayid.myapplication.parseStep.step.request;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.Logs;

/**
 * 请求参数
 *
 * @author zorro
 */
public class Param implements Cloneable, Serializable {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 名称
     */
    public String name;
    /**
     * 值
     */
    public String value;

    public Param() {
    }

    public Param(String name, String value) {
        this.name = name;
        this.value = value;
    }

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
        Param obj = null;
        try {
            obj = (Param) super.clone();
        } catch (CloneNotSupportedException e) {
            //.e("Param", e, "clone not support error:");
        }
        return obj;
    }
}
