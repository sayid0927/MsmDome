package com.example.sayid.myapplication.common.data;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode {


    /**
     * 黑名单
     */
    public final static String CODE_110007 = "110007";
    /**
     * 用户取消充值
     */
    public final static String CODE_110009 = "110009";
    /**
     * 正在处理更新
     */
    public final static String CODE_110012 = "110012";


    /**
     * 智能问答网络连接失败
     */
    public final static String CODE_111000 = "111000";
    /**
     * 智能问答网络连接超时
     */
    public final static String CODE_111001 = "111001";

    /**
     * 下载网络连接失败
     */
    public final static String CODE_111003 = "111003";
    /**
     * 下载网络连接超时
     */
    public final static String CODE_111004 = "111004";
    /**
     * 获取号码网络连接失败
     */
    public final static String CODE_111005 = "111005";
    /**
     * 获取号码网络连接超时
     */
    public final static String CODE_111006 = "111006";
    /**
     * 请求网络连接失败
     */
    public final static String CODE_111007 = "111007";
    /**
     * 请求网络连接超时
     */
    public final static String CODE_111008 = "111008";
    /**
     * 下载内容失败
     */
    public final static String CODE_111009 = "111009";
    /**
     * 下载返回内容为空
     */
    public final static String CODE_111010 = "111010";
    /**
     * 获取号码返回内容为空
     */
    public final static String CODE_111011 = "111011";
    /**
     * 请求返回内容为空
     */
    public final static String CODE_111012 = "111012";
    /**
     * Json返回内容为空
     */
    public final static String CODE_111013 = "111013";


    /**
     * 请求连接拒绝
     */
    public final static String CODE_111403 = "111403";
    /**
     * 请求IO异常
     */
    public final static String CODE_111404 = "111404";


    /**
     * 下载地址获取失败
     */
    public final static String CODE_113002 = "113002";
    /**
     * 没有RT
     */
    public final static String CODE_113003 = "113003";
    /**
     * IVR通话失败
     */
    public final static String CODE_113005 = "113005";
    /**
     * Key不存在
     */
    public final static String CODE_113006 = "113006";
    /**
     * 步骤下标越界
     */
    public final static String CODE_113007 = "113007";
    /**
     * 步骤下标越界
     */
    public final static String CODE_113008 = "113008";
    /**
     * 用户取消设置wap网
     */
    public final static String CODE_113009 = "113009";
    /**
     * 设置wap网失败
     */
    public final static String CODE_113010 = "113010";
    /**
     * Md5加密失败
     */
    public final static String CODE_113011 = "113011";
    /**
     * Base64加解密失败
     */
    public final static String CODE_113012 = "113012";

    /**
     * 发送号码或内容为空
     */
    public final static String CODE_114000 = "114000";
    /**
     * 注册短信发送监听失败
     */
    public final static String CODE_114001 = "114001";
    /**
     * 短信发送超时
     */
    public final static String CODE_114002 = "114002";
    /**
     * 短信发送失败
     */
    public final static String CODE_114003 = "114003";
    /**
     * 模拟器短信发送失败
     */
    public final static String CODE_114004 = "114004";
    /**
     * 接收方接收短信失败
     */
    public final static String CODE_114005 = "114005";
    /**
     * 短信读取失败
     */
    public final static String CODE_114006 = "114006";
    /**
     * 模拟器计费失败
     */
    public final static String CODE_114007 = "114007";
    /**
     * Sim卡没准备好
     */
    public final static String CODE_114008 = "114008";
    /**
     * 短信发送失败异常
     */
    public final static String CODE_114013 = "114013";
    /**
     * 短信读取等待
     */
    public final static String CODE_114014 = "114014";
    /**
     * 二次确认无回复
     */
    public final static String CODE_114015 = "114015";
    /**
     * 短信验证码失败
     */
    public final static String CODE_114016 = "114016";

    /**
     * 您点击得太快了
     */
    public final static String CODE_114020 = "114020";
    /**
     * goods_id太长
     */
    public final static String CODE_114021 = "114021";
    /**
     * goods_name太长
     */
    public final static String CODE_114022 = "114022";
    /**
     * user_order_id不能为空
     */
    public final static String CODE_114023 = "114023";
    /**
     * user_order_id太长
     */
    public final static String CODE_114024 = "114024";


    /**
     * 未知错误
     */
    public final static String CODE_119999 = "119999";


    public final static Map<String, String> errorMsg = new HashMap<String, String>();

    static {
        errorMsg.put(CODE_110007, "黑名单");
        errorMsg.put(CODE_110009, "用户取消充值");
        errorMsg.put(CODE_110012, "正在处理更新");
        errorMsg.put(CODE_111000, "智能问答网络连接失败");
        errorMsg.put(CODE_111001, "智能问答网络连接超时");
        errorMsg.put(CODE_111003, "下载网络连接失败");
        errorMsg.put(CODE_111004, "下载网络连接超时");
        errorMsg.put(CODE_111005, "获取号码网络连接失败");
        errorMsg.put(CODE_111006, "获取号码网络连接超时");
        errorMsg.put(CODE_111007, "请求网络连接失败");
        errorMsg.put(CODE_111008, "请求网络连接超时");
        errorMsg.put(CODE_111009, "下载内容失败");
        errorMsg.put(CODE_111010, "下载返回内容为空");
        errorMsg.put(CODE_111011, "获取号码返回内容为空");
        errorMsg.put(CODE_111012, "请求返回内容为空");
        errorMsg.put(CODE_111013, "Json返回内容为空");


        errorMsg.put(CODE_111403, "请求连接拒绝");
        errorMsg.put(CODE_111404, "请求IO异常");

        errorMsg.put(CODE_113002, "下载地址获取失败");
        errorMsg.put(CODE_113003, "没有RT");
        errorMsg.put(CODE_113005, "IVR通话失败");
        errorMsg.put(CODE_113006, "Key不存在");
        errorMsg.put(CODE_113007, "步骤下标越界");
        errorMsg.put(CODE_113008, "步骤下标越界");
        errorMsg.put(CODE_113009, "用户取消设置wap网");
        errorMsg.put(CODE_113010, "设置wap网失败");
        errorMsg.put(CODE_113011, "Md5加密失败");
        errorMsg.put(CODE_113012, "Base64加解密失败");

        errorMsg.put(CODE_114000, "发送号码或内容为空");
        errorMsg.put(CODE_114001, "注册短信发送监听失败");
        errorMsg.put(CODE_114002, "短信发送超时");
        errorMsg.put(CODE_114003, "短信发送失败  没有权限 ");
        errorMsg.put(CODE_114004, "模拟器短信发送失败");
        errorMsg.put(CODE_114005, "接收方接收短信失败");
        errorMsg.put(CODE_114006, "短信读取失败");
        errorMsg.put(CODE_114007, "模拟器计费失败");
        errorMsg.put(CODE_114008, "Sim卡没准备好");
        errorMsg.put(CODE_114013, "短信发送失败异常");
        errorMsg.put(CODE_114014, "短信读取等待");
        errorMsg.put(CODE_114015, "二次确认无回复");
        errorMsg.put(CODE_114020, "您点击得太快了");
        errorMsg.put(CODE_114021, "goods_id太长");
        errorMsg.put(CODE_114022, "goods_name太长");
        errorMsg.put(CODE_114023, "user_order_id不能为空");
        errorMsg.put(CODE_114024, "user_order_id太长");


        errorMsg.put(CODE_119999, "未知错误");
    }
}
