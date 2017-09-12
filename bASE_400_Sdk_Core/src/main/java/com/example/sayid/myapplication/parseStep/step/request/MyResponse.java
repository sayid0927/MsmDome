package com.example.sayid.myapplication.parseStep.step.request;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.parseStep.step.MyCookie;
import com.example.sayid.myapplication.parseStep.step.MyHeader;
import com.example.sayid.myapplication.parseStep.step.response.Key;

/**
 * 请求响应
 *
 * @author zorro
 */
public class MyResponse extends Response {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 服务器下发请求头
     */
    public List<MyHeader> headers;
    /**
     * 服务器下发cookie
     */
    public List<MyCookie> cookies;
    /**
     * 关键字
     */
    public List<Key> keys;


    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {

        if (!jsonObj.isNull("headers")) {
            JSONArray jHead = jsonObj.optJSONArray("headers");
            if (jHead != null) {
                headers = new ArrayList<MyHeader>();
                for (int k = 0; k < jHead.length(); k++) {
                    JSONObject json = (JSONObject) jHead.get(k);
                    MyHeader header = new MyHeader();
                    header.parseJson(json);
                    headers.add(header);
                }
            }
        }

        if (!jsonObj.isNull("cookies")) {
            cookies = new ArrayList<MyCookie>();
            JSONArray jBody_cookies = jsonObj.optJSONArray("cookies");
            if (jBody_cookies != null) {
                for (int l = 0; l < jBody_cookies.length(); l++) {
                    JSONObject json = (JSONObject) jBody_cookies.get(l);
                    MyCookie cookie = new MyCookie();
                    cookie.parseJson(json);
                    cookies.add(cookie);
                }
            }
        }

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

        // 解析Response
        super.parseJson(jsonObj);
    }

    @Override
    public Object clone() {
        MyResponse obj = null;
        obj = (MyResponse) super.clone();
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
        obj.keys = new ArrayList<Key>();
        Iterator<Key> key_it = this.keys.iterator();
        while (key_it.hasNext()) {
            obj.keys.add((Key) (key_it.next().clone()));
        }

        return obj;
    }
}
