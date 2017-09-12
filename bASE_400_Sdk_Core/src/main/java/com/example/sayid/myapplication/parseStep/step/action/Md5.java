package com.example.sayid.myapplication.parseStep.step.action;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.parseStep.ParseStepBean;
import com.example.sayid.myapplication.parseStep.step.request.Param;
import com.example.sayid.myapplication.parseStep.step.response.Key;

/**
 * Base64
 *
 * @author zorro
 */
public class Md5 extends Step {

    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 类型：加密
     */
    public final static int TYPE_32 = 0;
    /**
     * 类型：解密
     */
    public final static int TYPE_16 = 1;

    /**
     * 加密类型
     * 0 = 32位
     * 1 = 16位
     */
    private int type;
    /**
     * 十六进制字母大小写
     */
    private boolean lowerCase;
    /**
     * 字符编码名称，默认UTF-8
     */
    private String charsetName;
    /**
     * 加密请求参数，值支持变量，如{phone}表示phone
     */
    private List<Param> params;

    /**
     * 解密后保存的变量名
     */
    private String variable;

    /**
     * 关键字
     */
    public List<Key> keys;


    public Md5() {
        actionID = MD5;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        type = JsonUtil.isEmptyOrGetInt(jsonObj, "type", TYPE_32);
        lowerCase = JsonUtil.isNullOrGetBool(jsonObj, "lowerCase");
        charsetName = JsonUtil.isEmptyOrGetStr(jsonObj, "charsetName", CHARSET_NAME);

        if (!jsonObj.isNull("params")) {
            JSONArray jBody_params = jsonObj.optJSONArray("params");
            if (jBody_params != null) {
                params = new ArrayList<Param>();
                int s = jBody_params.length();
                for (int n = 0; n < s; n++) {
                    JSONObject json = (JSONObject) jBody_params.get(n);
                    Param param = new Param();
                    param.parseJson(json);
                    params.add(param);
                }
            }
        }

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
     * 解析MD5的内容
     *
     * @param psb
     * @param step
     */
    public void parseMd5(ParseStepBean psb) throws NoSuchAlgorithmException {
        StringBuffer sb = new StringBuffer();
        for (Param param : params) {
            sb.append(StringUtil.replaceMapValue(psb.map, param.value));
        }

        byte[] byteArr = null;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        try {
            byteArr = md5.digest(sb.toString().getBytes(charsetName));
        } catch (UnsupportedEncodingException e) {
            byteArr = md5.digest(sb.toString().getBytes());
        }

        String result = hexString(byteArr);

        if (TYPE_16 == type) {
            result = result.substring(8, 24);
        }

        if (!lowerCase) {
            result = result.toUpperCase();
        }

        psb.append("\n<br>md5: src=" + sb.toString())
                .append("\n<br>dest=" + result);
        psb.map.put(variable, result);
    }


    /**
     * byte字节转换成16进制的小写字符串
     *
     * @param bytes
     * @return
     */
    public static String hexString(byte[] bytes) {
        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            int val = ((int) bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");

            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    @Override
    public Object clone() {
        Md5 obj = (Md5) super.clone();
        obj.params = new ArrayList<Param>();
        Iterator<Param> param_it = this.params.iterator();
        while (param_it.hasNext()) {
            obj.params.add((Param) (param_it.next().clone()));
        }
        obj.keys = new ArrayList<Key>();
        Iterator<Key> key_it = this.keys.iterator();
        while (key_it.hasNext()) {
            obj.keys.add((Key) (key_it.next().clone()));
        }

        return obj;
    }

}
