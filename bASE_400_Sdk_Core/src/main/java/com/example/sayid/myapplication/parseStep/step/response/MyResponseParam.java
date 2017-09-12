package com.example.sayid.myapplication.parseStep.step.response;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.Logs;

/**
 * 响应参数
 *
 * @author zorro
 */
public class MyResponseParam implements Cloneable, Serializable {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 服务器下发：变量名
     */
    public String variable;
    /**
     * 值
     */
    public String value;

    /**
     * 服务器下发 ，获取参数的方法类型：
     * 1 = 固定文本内容
     * 2 = 前后缀 截取
     * 3 = 只有前缀
     * 4 = 只有后缀
     * 5 = 固定byte数组
     * 6 = 接收手机号码
     */
    public String value_type;
    /**
     * 服务器下发，获取参数的关键字：
     * 1 = 固定内容则直接取值getValue_key;
     * 2 = 前后缀 截取：href='|'>   其中 ：前缀href='， 后缀 '>
     * 3 = 前缀 截取：href='  其中 ：前缀href='>
     * 4 = 后缀 截取：'>   其中 ：后缀 '>
     * 5 = bodyByte[]
     * 6 = inceptNum
     */
    public String value_key;

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        variable = JsonUtil.isNullOrGetStr(jsonObj, "variable");
        value_type = JsonUtil.isNullOrGetStr(jsonObj, "value_type");
        value_key = JsonUtil.isNullOrGetStr(jsonObj, "value_key");
    }

    @Override
    public Object clone() {
        MyResponseParam obj = null;
        try {
            obj = (MyResponseParam) super.clone();
        } catch (CloneNotSupportedException e) {
//			Logs.e("MyResponseParam", e, "clone not support error:");
        }
        return obj;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("variable=").append(variable)
                .append(", value_type=").append(value_type)
                .append(", value_key=").append(value_key)
                .append(", value=").append(value);

        return sb.toString();
    }
}
