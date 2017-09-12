package com.example.sayid.myapplication.parseStep.step.action;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;

/**
 * 联网
 *
 * @author zorro
 */
public class NetWork extends Step {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 类型：wap,net
     */
    public String type;

    public NetWork() {
        actionID = Step.NETWORK;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        type = JsonUtil.isNullOrGetStr(jsonObj, "type");
    }

}
