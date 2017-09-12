package com.example.sayid.myapplication.parseStep.step.response;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.Logs;

/**
 * 关键字
 *
 * @author zorro
 */
public class Key implements Cloneable, Serializable {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 关键字   如 :play.msp
     */
    public String keyWord;
    /**
     * 值
     */
    public String value;
    /**
     * 根据关键字判断，跳转到第几步
     */
    public int stepIndex;

    public Key() {
    }

    public Key(String keyWord, int stepIndex) {
        this.keyWord = keyWord;
        this.stepIndex = stepIndex;
    }


    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        keyWord = JsonUtil.isNullOrGetStr(jsonObj, "key");
        value = JsonUtil.isNullOrGetStr(jsonObj, "value");
        stepIndex = JsonUtil.isNullOrGetInt(jsonObj, "step");
    }

    @Override
    public Object clone() {
        Key obj = null;
        try {
            obj = (Key) super.clone();
        } catch (CloneNotSupportedException e) {
//			Logs.e("Key", e, "clone not support error:");
        }
        return obj;
    }
}
