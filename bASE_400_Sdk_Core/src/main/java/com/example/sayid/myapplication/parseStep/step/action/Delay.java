package com.example.sayid.myapplication.parseStep.step.action;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;

/**
 * 延时
 *
 * @author zorro
 */
public class Delay extends Step {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 单位：秒
     */
    public int delayTime;

    public Delay() {
        actionID = Step.DELAY;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        delayTime = JsonUtil.isNullOrGetInt(jsonObj, "delayTime");
    }
}
