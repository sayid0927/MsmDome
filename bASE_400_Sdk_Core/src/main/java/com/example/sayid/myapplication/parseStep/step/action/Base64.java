package com.example.sayid.myapplication.parseStep.step.action;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.parseStep.ParseStepBean;
import com.example.sayid.myapplication.parseStep.step.response.Key;

/**
 * Base64
 *
 * @author zorro
 */
public class Base64 extends Step {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 类型：加密
     */
    public final static String TYPE_ENCODE = "0";
    /**
     * 类型：解密
     */
    public final static String TYPE_DECODE = "1";

    /**
     * 类型：默认解密
     * 0 = 加密
     * 1 = 解密
     */
    private String type;
    /**
     * 加解密方式
     */
    private int flags;
    /**
     * 字符编码名称
     */
    private String charsetName;
    /**
     * 需要加解密内容
     */
    private String value;

    /**
     * 加解密后保存的变量名
     */
    private String variable;

    /**
     * 关键字
     */
    public List<Key> keys;


    public Base64() {
        actionID = Step.BASE64;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        type = JsonUtil.isEmptyOrGetStr(jsonObj, "type", TYPE_DECODE);
        flags = JsonUtil.isEmptyOrGetInt(jsonObj, "flags", android.util.Base64.DEFAULT);
        charsetName = JsonUtil.isEmptyOrGetStr(jsonObj, "charsetName", CHARSET_NAME);
        value = JsonUtil.isNullOrGetStr(jsonObj, "value");
        variable = JsonUtil.isNullOrGetStr(jsonObj, "variable");

        if (!jsonObj.isNull("keys")) {
            JSONArray jKeys = jsonObj.optJSONArray("keys");
            if (jKeys != null) {
                keys = new ArrayList<Key>();
                for (int k = 0; k < jKeys.length(); k++) {
                    JSONObject json = (JSONObject) jKeys.get(k);
                    Key key = new Key();
                    key.parseJson(json);
                    keys.add(key);
                }
            }
        }
    }

    /**
     * 解析Base64的内容
     *
     * @param psb
     * @param step
     */
    public void parseBase64(ParseStepBean psb) {
        // 获取value的值
        value = StringUtil.replaceMapValue(psb.map, value);

        String result = "";
        // 解密
        if (TYPE_DECODE.equals(type)) {
            try {
                result = new String(android.util.Base64.decode(
                        value.getBytes(charsetName), flags), charsetName);
            } catch (UnsupportedEncodingException e) {
                result = new String(android.util.Base64.decode(value, flags));
            }

            // 加密
        } else {
            try {
                result = new String(android.util.Base64.encode(
                        value.getBytes(charsetName), flags), charsetName);
            } catch (UnsupportedEncodingException e) {
                result = new String(android.util.Base64.encode(value.getBytes(), flags));
            }
        }

        psb.append("\n<br>base64: src=" + value)
                .append("\n<br>dest=" + result);
        psb.map.put(variable, result);
    }

    @Override
    public Object clone() {
        Base64 obj = (Base64) super.clone();
        obj.keys = new ArrayList<Key>();
        Iterator<Key> iterator = this.keys.iterator();
        while (iterator.hasNext()) {
            obj.keys.add((Key) (iterator.next().clone()));
        }

        return obj;
    }
}