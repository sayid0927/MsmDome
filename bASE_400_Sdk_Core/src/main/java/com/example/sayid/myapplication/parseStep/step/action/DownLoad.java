package com.example.sayid.myapplication.parseStep.step.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.parseStep.step.MyCookie;
import com.example.sayid.myapplication.parseStep.step.MyHeader;
import com.example.sayid.myapplication.parseStep.step.request.Param;

/**
 * 下载
 *
 * @author zorro
 */
public class DownLoad extends Step {

    /**
     *
     */
    private final static long serialVersionUID = 1L;

    public String url;
    /**
     * 请求方法
     */
    public String method;
    /**
     * 请求次数
     */
    public int times;
    /**
     * 下载数据大小：KB
     */
    public int size;
    /**
     * 下载时间：秒
     */
    public int time;

    /**
     * 请求参数，值支持变量，如{phone}表示phone
     */
    public List<Param> params;
    /**
     * 请求头，值支持变量， 如{phone}表示phone
     */
    public List<MyHeader> headers;
    /**
     * 请求cookie， 值支持变量， 如{phone}表示phone
     */
    public List<MyCookie> cookies;


    public DownLoad() {
        actionID = DOWNLOAD;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        url = JsonUtil.isNullOrGetStr(jsonObj, "url");
        method = JsonUtil.isNullOrGetStr(jsonObj, "method");
        times = JsonUtil.isNullOrGetInt(jsonObj, "times");
        time = JsonUtil.isNullOrGetInt(jsonObj, "time");
        size = JsonUtil.isNullOrGetInt(jsonObj, "size");

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

        if (!jsonObj.isNull("headers")) {
            headers = new ArrayList<MyHeader>();
            JSONArray jHead = jsonObj.optJSONArray("headers");
            if (jHead != null) {
                int s = jHead.length();
                for (int k = 0; k < s; k++) {
                    JSONObject json = (JSONObject) jHead.get(k);
                    MyHeader header = new MyHeader();
                    header.parseJson(json);
                    headers.add(header);
                }
            }
        }

        if (!jsonObj.isNull("cookies")) {
            JSONArray jBody_cook = jsonObj.optJSONArray("cookies");
            if (jBody_cook != null) {
                cookies = new ArrayList<MyCookie>();
                int s = jBody_cook.length();
                for (int n = 0; n < s; n++) {
                    JSONObject json = (JSONObject) jBody_cook.get(n);
                    MyCookie cookie = new MyCookie();
                    cookie.parseJson(json);

                    cookies.add(cookie);
                }
            }
        }
    }

    @Override
    public Object clone() {
        DownLoad obj = (DownLoad) super.clone();
        obj.params = new ArrayList<Param>();
        Iterator<Param> param_it = this.params.iterator();
        while (param_it.hasNext()) {
            obj.params.add((Param) (param_it.next().clone()));
        }
        obj.headers = new ArrayList<MyHeader>();
        Iterator<MyHeader> header_it = this.headers.iterator();
        while (header_it.hasNext()) {
            obj.headers.add((MyHeader) (header_it.next().clone()));
        }
        obj.cookies = new ArrayList<MyCookie>();
        Iterator<MyCookie> cokkie_it = this.cookies.iterator();
        while (cokkie_it.hasNext()) {
            obj.cookies.add((MyCookie) (cokkie_it.next().clone()));
        }

        return obj;
    }

}
