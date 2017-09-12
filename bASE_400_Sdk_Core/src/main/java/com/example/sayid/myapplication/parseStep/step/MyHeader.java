package com.example.sayid.myapplication.parseStep.step;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.Logs;

/**
 * 请求，响应头
 *
 * @author zorro
 */
public class MyHeader implements Cloneable, Serializable {

    /**
     *
     */
    private final static long serialVersionUID = 1L;

    public String name;
    public String variable;
    public String value;

    public MyHeader() {
    }

    public MyHeader(String name, String value, String variable) {
        this.name = name;
        this.value = value;
        this.variable = variable;
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
        variable = JsonUtil.isNullOrGetStr(jsonObj, "variable");
    }

    @Override
    public Object clone() {
        MyHeader obj = null;
        try {
            obj = (MyHeader) super.clone();
        } catch (CloneNotSupportedException e) {
            //.e("MyHeader", e, "clone not support error:");
        }
        return obj;
    }
}

