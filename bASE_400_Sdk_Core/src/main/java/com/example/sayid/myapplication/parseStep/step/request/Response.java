package com.example.sayid.myapplication.parseStep.step.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.Logs;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.parseStep.ParseStepBean;
import com.example.sayid.myapplication.parseStep.step.action.Phone;
import com.example.sayid.myapplication.parseStep.step.action.Step;
import com.example.sayid.myapplication.parseStep.step.response.MyResponseParam;

/**
 * 解析响应体内容
 *
 * @author zorro
 */
public class Response implements Cloneable, Serializable {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 返回的页面内容类型
     * 1 String
     * 2 Inputstream
     */
    public int bodyType = 1;

    /**
     * 返回的页面字节内容
     */
    public byte[] bodyByte;
    /**
     * 返回的页面内容
     */
    public String body;

    /**
     * 服务器下发：body里的内容解析
     */
    public List<MyResponseParam> body_params;


    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        if (!jsonObj.isNull("bodyType")) {
            bodyType = jsonObj.getInt("bodyType");
        }

        if (!jsonObj.isNull("body_params")) {
            body_params = new ArrayList<MyResponseParam>();
            JSONArray j_params = jsonObj.getJSONArray("body_params");
            if (j_params != null) {
                for (int j = 0; j < j_params.length(); j++) {
                    JSONObject json = (JSONObject) j_params.get(j);
                    MyResponseParam rp = new MyResponseParam();
                    rp.parseJson(json);
                    body_params.add(rp);
                }
            }
        }
    }


    /**
     * 解析Response返回内容
     *
     * @param parseStepBean
     * @param step
     */
    public void parseResponseParam(ParseStepBean parseStepBean, Step step) {
        try {
            if (body_params != null && body_params.size() > 0) {
                for (MyResponseParam rp : body_params) {
                    int value_type = Integer.parseInt(rp.value_type);
                    switch (value_type) {
                        case 1:
                            rp.value = body;
                            parseStepBean.map.put(rp.variable, rp.value);
                            break;

                        case 2:           //前后缀 截取
                            rp.value = StringUtil.getDynamicAnswer(body, rp.value_key).trim();
                            parseStepBean.map.put(rp.variable, rp.value);
                            break;

                        case 3:          //只有前缀
                            rp.value = StringUtil.getDynamicAnswer_per(body, rp.value_key).trim();
                            parseStepBean.map.put(rp.variable, rp.value);
                            break;

                        case 4:         //只有后缀
                            rp.value = StringUtil.getDynamicAnswer_aft(body, rp.value_key).trim();
                            parseStepBean.map.put(rp.variable, rp.value);
                            break;

                        case 5:
                            parseStepBean.map_object.put(rp.variable, bodyByte);
                    }
                }

                // 保存手机号码到本地
                if (step instanceof Phone) {
                    String phoneNum = parseStepBean.map.get("phone");
                    if (phoneNum != null) {
                        Phone phone = (Phone) step;
//						AppData.phoneNum = phoneNum;
//						AppData.phone_channel = phone.channel;

//						CacheUtil.getInstance().setString(AppData.imsi, AppData.phoneNum);
//						CacheUtil.getInstance().setInt(AppData.phoneNum, AppData.phone_channel);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Object clone() {
        Response obj = null;
        try {
            obj = (Response) super.clone();
            if (this.bodyByte != null) {
                obj.bodyByte = new byte[this.bodyByte.length];

                System.arraycopy(this.bodyByte, 0, obj.bodyByte, 0, this.bodyByte.length);
            }

            obj.body_params = new ArrayList<MyResponseParam>();
            Iterator<MyResponseParam> resParam_it = this.body_params.iterator();
            while (resParam_it.hasNext()) {
                obj.body_params.add((MyResponseParam) (resParam_it.next().clone()));
            }
        } catch (CloneNotSupportedException e) {
            //.e("Response", e, "clone not support error:");
        }
        return obj;
    }

}
