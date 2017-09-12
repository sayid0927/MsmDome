package com.example.sayid.myapplication.parseStep.step.action;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.parseStep.step.request.MyResponse;

public abstract class MyResponseStep extends Step {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 请求响应
     */
    public MyResponse response;

    public MyResponseStep() {
        response = new MyResponse();
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        JSONObject jResponse = jsonObj.optJSONObject("response");
        if (jResponse != null) {
            response.parseJson(jResponse);
        }
    }
}
