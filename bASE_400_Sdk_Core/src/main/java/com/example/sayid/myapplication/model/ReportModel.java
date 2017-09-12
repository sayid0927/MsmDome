package com.example.sayid.myapplication.model;

import android.content.Context;

import com.example.sayid.myapplication.common.bean.ReportBasepay;
import com.example.sayid.myapplication.common.bean.ReportBean;
import com.example.sayid.myapplication.common.bean.ReportConfirm;
import com.example.sayid.myapplication.common.bean.ReportSms;
import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.db2.ReportDao;
import com.example.sayid.myapplication.common.listener.OnNetListener;
import com.example.sayid.myapplication.common.listener.OnNetStateChangeCallBack;
import com.example.sayid.myapplication.common.util.NetControlUtil;
import com.example.sayid.myapplication.common.util.ParseKsy;
import com.example.sayid.myapplication.http.HttpJsonThread;

import org.apache.http.HttpEntity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReportModel implements OnNetListener {

    private final static String TAG = "ReportModel";

    private static volatile ReportModel instance;

    /**
     * 监听网络状态改变，Wifi或移动数据网络打开，立刻上报
     */
    public OnNetStateChangeCallBack onNetStateChangeCallBack;

    private Context context;

    /**
     * 状态报告定时上报
     */
    private TimerTask timertask = new TimerTask() {
        public void run() {
            try {
                if (context != null) {
                    //.d(TAG, "report timertask---->");
                    boolean isWifiEnable = NetControlUtil.getInstance(context).isWifiConnected();
                    boolean isGprsEnable = NetControlUtil.getInstance(context).isGprsConnected();
                    checkReport(isWifiEnable, isGprsEnable);
                }
            } catch (Exception e) {
                //.e(TAG, e, "timertask run error:");
            }
        }
    };

    /**
     * 私有构造函数
     *
     * @param context
     */
    private ReportModel(Context context) {
        try {
            this.context = context;
            if (this.context == null) {
                return;
            }

            // 监听网络状态，并上报
            onNetStateChangeCallBack = new OnNetStateChangeCallBack() {
                public void netStateChange(boolean isWifiEnable, boolean isGprsEnable) {
                    checkReport(isWifiEnable, isGprsEnable);
                }
            };

            //计时器，定时上报
            Timer timer = new Timer(true);
            //延时1分钟，10分钟执行一次
            timer.schedule(timertask, 60 * 1000, 10 * 60 * 1000);
        } catch (Exception e) {
            //.e(TAG, e, "ReportModel error:");
        }
    }

    /**
     * 获取单例对象
     *
     * @param context
     * @return
     */
    public static ReportModel getInstanse(Context context) {
        //先检查实例是否存在，如果不存在才进入下面的同步块
        if (instance == null) {
            //同步块，线程安全的创建实例
            synchronized (ReportModel.class) {
                //再次检查实例是否存在，如果不存在才真正的创建实例
                if (instance == null) {
                    instance = new ReportModel(context);
                }
            }
        }
        return instance;
    }


    /**
     * 检查是否需要上报，如果需要则上报
     * 此处会多线程执行，设置同步标识
     *
     * @param isWifiEnable
     * @param isGprsEnable
     */
    private synchronized void checkReport(boolean isWifiEnable, boolean isGprsEnable) {
        //.d(TAG, "checkReport---->");
        try {
            if (isWifiEnable || isGprsEnable) {        // 当wifi或者gprs打开可用的时候 则上报
                ReportDao reportDao = new ReportDao(context);
                List<ReportBean> v_rb = reportDao.selectAll();

                int v_size = v_rb.size();
                //.d(TAG, "缓存---->" + v_size);
                for (ReportBean bean : v_rb) {
                    bean.isCheck = true;
                    report(bean);
                }
            }
        } catch (Exception e) {
            //.e(TAG, e, "checkReport error:");
        }
    }

    /**
     * 短信发送上报
     *
     * @param pay_order_id
     * @param item
     * @param index
     * @param destPhone
     * @param message
     * @param status
     * @param errorCode
     */
    public void addReportSms(String pay_order_id, int item, int index, String destPhone, String message, String status, String errorCode) {
        ReportSms reportBean = new ReportSms();
        reportBean.pay_order_id = pay_order_id;
        reportBean.item = item;
        reportBean.index = index;
        reportBean.channel_port = destPhone;
        reportBean.channel_order = message;
        reportBean.status = status;
        reportBean.error_code = errorCode;
        reportBean.jsonData = reportBean.createJson();

        report(reportBean);
    }

    /**
     * 基地上报
     *
     * @param pay_order_id
     * @param status
     * @param errorStep
     * @param errorCode
     * @param msg
     */
    public void addReportBasePay(String pay_order_id, String status, int errorStep, String errorCode, String msg) {
        ReportBasepay reportBean = new ReportBasepay();
        reportBean.pay_order_id = pay_order_id;
        reportBean.status = status;
        reportBean.errorStep = errorStep;
        reportBean.error_code = errorCode;
        reportBean.response_msg = msg;
        reportBean.jsonData = reportBean.createJson();

        report(reportBean);
    }

    /**
     * 二次确认短信发送上报
     *
     * @param pay_order_id
     * @param item
     * @param status
     * @param error_code
     */
    public void addReportConfirm(String pay_order_id, int item, String status, String error_code, String msg) {
        ReportConfirm reportBean = new ReportConfirm();
        reportBean.pay_order_id = pay_order_id;
        reportBean.item = item;
        reportBean.status = status;
        reportBean.error_code = error_code;
        reportBean.response_msg = msg;
        reportBean.jsonData = reportBean.createJson();

        report(reportBean);
    }

    /**
     * 上报
     */
    private void report(ReportBean reportBean) {
        try {
            //.d(TAG, "report-->pay_order_id = " + reportBean.pay_order_id);

            new HttpJsonThread().startThread(ParseKsy.decode(ConfigConst.PAY_URL_ROOT_DEFAULT_VALUE) + reportBean.url, reportBean.jsonData, this);
        } catch (Exception e) {
            //.e(TAG, e, "report error:");
        }
    }

    /**
     * 上报成功
     */
    @Override
    public void onSuccess(HttpEntity entity, boolean isEnc) {
        try {
//			Logs.d(TAG, "onSuccess = " + reportBean.pay_order_id);
//			if (reportBean.isCheck) {
//				deleteReportBean(reportBean);
//			}
        } catch (Exception e) {
            //.e(TAG, e,  "onSuccess error:");
        }
    }

    /**
     * 上报失败：将上报失败的保存起来，下次再上报
     */
    @Override
    public void onFailed(String exceptionId, String exceptionText) {
//		Logs.d(TAG, "onFailed = " + reportBean.pay_order_id);
//		if (!reportBean.isCheck) {
//			storeReportBean(reportBean);
//		}
    }

    @Override
    public void onTimeout() {
        onFailed(ErrorCode.CODE_111008, ErrorCode.errorMsg.get(ErrorCode.CODE_111008));
    }

    /**
     * 保存，上报
     *
     * @param reportBean
     */
    private void storeReportBean(ReportBean reportBean) {
        try {
            //.d(TAG, "storeReportBean = " + reportBean.pay_order_id);

            ReportDao reportDao = new ReportDao(context);
            reportDao.insert(reportBean);
        } catch (Exception e) {
            //.e(TAG, e, "storeReportBean error:");
        }
    }

    /**
     * 删除，上报
     *
     * @param reportBean
     */
    private void deleteReportBean(ReportBean reportBean) {
        try {
            //.d(TAG, "deleteReportBean = " + reportBean.pay_order_id);

            ReportDao reportDao = new ReportDao(context);
            reportDao.delete(reportBean.id);
        } catch (Exception e) {
            //.e(TAG, e, "deleteReportBean error:");
        }
    }
}