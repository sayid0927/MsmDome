package com.example.sayid.myapplication.parseStep.step.action;

import java.io.Serializable;

public class Step implements Cloneable, Serializable {

    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * 请求
     */
    public final static int REQUEST = 1;
    /**
     * 下载
     */
    public final static int DOWNLOAD = 2;
    /**
     * 延时
     */
    public final static int DELAY = 3;
    /**
     * 结束
     */
    public final static int END = 4;
    /**
     * 打电话
     */
    public final static int CALL = 5;
    /**
     * 发短信
     */
    public final static int SMS = 6;
    /**
     * 输入，暂不支持
     */
    public final static int INPUT = 7;
    /**
     * 获取手机号码
     */
    public final static int PHONE = 8;
    /**
     * 切换网络
     */
    public final static int NETWORK = 9;
    /**
     * 获取短信
     */
    public final static int GETSMS = 10;
    /**
     * Base64加解密
     */
    public final static int BASE64 = 11;
    /**
     * Md5加解密
     */
    public final static int MD5 = 12;

    /**
     * 字符编码名称
     */
    public final static String CHARSET_NAME = "UTF-8";

    public int actionID;

    @Override
    public Object clone() {
        Step obj = null;
        try {
            obj = (Step) super.clone();
        } catch (CloneNotSupportedException e) {
            //.e("Step", e, "clone not support error:");
        }
        return obj;
    }
}
