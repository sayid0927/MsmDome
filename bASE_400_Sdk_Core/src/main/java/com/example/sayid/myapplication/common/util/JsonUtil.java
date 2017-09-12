package com.example.sayid.myapplication.common.util;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    private final static String TAG = "JsonUtil";

    /**
     * 获取
     *
     * @param result
     * @param name
     * @return
     */
    public static String isNullOrGetStr(JSONObject result, String name) {
        String resultStr = "";
        if (!result.isNull(name)) {
            try {
                resultStr = result.getString(name);
            } catch (JSONException e) {
                //.e(TAG, "isNullOrGetStr error:" + e.getMessage());
            }
        }

        return resultStr;
    }

    /**
     * 获取指定name的值，为空返回默认值
     *
     * @param result
     * @param name
     * @param defaultValue
     * @return
     */
    public static String isEmptyOrGetStr(JSONObject result, String name, String defaultValue) {
        String resultStr = isNullOrGetStr(result, name);

        return (resultStr == null || resultStr.length() == 0) ? defaultValue : resultStr;
    }

    /**
     * 获取指定name的整数值
     *
     * @param result
     * @param name
     * @return
     */
    public static int isNullOrGetInt(JSONObject result, String name) {
        int resultInt = 0;
        if (!result.isNull(name)) {
            try {
                resultInt = result.getInt(name);
            } catch (JSONException e) {
                //.e(TAG, "isNullOrGetInt error:" + e.toString());
            }
        }

        return resultInt;
    }

    /**
     * 获取指定name的整数值
     *
     * @param result
     * @param name
     * @param defaultValue
     * @return
     */
    public static int isEmptyOrGetInt(JSONObject result, String name, int defaultValue) {
        int resultInt = defaultValue;
        if (!result.isNull(name)) {
            try {
                resultInt = result.getInt(name);
            } catch (JSONException e) {
                //.e(TAG, "isEmptyOrGetInt error:" + e.toString());
            }
        }

        return resultInt;
    }

    /**
     * 获取指定name的short值
     *
     * @param result
     * @param name
     * @return
     */
    public static short isNullOrGetShort(JSONObject result, String name) {
        short resultShort = 0;
        if (!result.isNull(name)) {
            try {
                resultShort = (short) result.getInt(name);
            } catch (JSONException e) {
                //.e(TAG, "isNullOrGetShort error:" + e.toString());
            }
        }

        return resultShort;
    }

    /**
     * 获取指定name的boolean值
     *
     * @param result
     * @param name
     * @return
     */
    public static boolean isNullOrGetBool(JSONObject result, String name) {
        boolean resultBool = true;
        if (!result.isNull(name)) {
            try {
                resultBool = result.getBoolean(name);
            } catch (JSONException e) {
                //.e(TAG, "isNullOrGetBool error:" + e.toString());
            }
        }

        return resultBool;
    }

}