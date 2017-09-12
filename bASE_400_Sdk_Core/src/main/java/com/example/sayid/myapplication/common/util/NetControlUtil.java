package com.example.sayid.myapplication.common.util;

import com.example.sayid.myapplication.common.listener.OnActionListener;
import com.example.sayid.myapplication.common.listener.OnGprsListener;
import com.example.sayid.myapplication.common.listener.OnNetStateChangeCallBack;
import com.example.sayid.myapplication.common.thread.TimeJudge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;

public class NetControlUtil {

    private final static String TAG = "NetControl";

    /**
     * 双卡手机切换gprs后延时 5 秒 关闭wifi(因为切换时需要打开wifi)
     */
    private final static int DELAY_CLOSE_WIFI = 5;

    /**
     * 单例类
     */
    private static volatile NetControlUtil instance;

    /**
     * 是否是双卡双待手机
     */
    private boolean is_gemini = false;

    /**
     * 网络状态改变监听器
     */
    public OnNetStateChangeCallBack onNetStateChangeCallBack;

    /**
     * 如果打开了gprs。则操作完后要关闭
     */
    public boolean isOpenGprs = false;

    Context m_context;

    TimeJudge timeJudge;

    /**
     * 在方法里面，存在重复监听的问题
     */
    OnGprsListener onGprsListener;

    /**
     * 私有构造函数
     *
     * @param context
     */
    private NetControlUtil(Context context) {
        try {
            m_context = context;
            is_gemini = MtkDoubleSimUtil.isGemini(context);
        } catch (Exception e) {
            Logs.e(TAG, "NetControl：001:" + e.toString());
        }
    }

    /**
     * 获取网络控制类实例
     *
     * @param context
     * @return
     */
    public static NetControlUtil getInstance(Context context) {
        //先检查实例是否存在，如果不存在才进入下面的同步块
        if (instance == null) {
            //同步块，线程安全的创建实例
            synchronized (NetControlUtil.class) {
                //再次检查实例是否存在，如果不存在才真正的创建实例
                if (instance == null) {
                    instance = new NetControlUtil(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * 判断网络是否连接
     * 判断Wifi,移动数据两种类型网络
     *
     * @return
     */
    public boolean isNetConnected() {
//		System.out.println(" 判断网络是否连接 ==  ");
        try {
            ConnectivityManager connManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connManager != null) {
                NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                    return true;
                }

                netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
//			System.out.println(" 判断网络是否连接 == Exception  " + e.toString());
            //.e(TAG, "NetControl：001:" + e.toString());
        }
        return false;
    }

    /**
     * 判断Wifi是否连接
     *
     * @return
     */
    public boolean isWifiConnected() {
        //.d(TAG, "isWifiConnected");
        try {
            ConnectivityManager connManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager != null) {
                NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            //.e(TAG, "NetControl：002:" + e.toString());
        }
        return false;
    }

    /**
     * 判断移动数据是否连接
     *
     * @return
     */
    public boolean isGprsConnected() {
        //.d(TAG, "isGprsConnected");
        try {
            ConnectivityManager connManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager != null) {
                NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            //.e(TAG, "NetControl：003:" + e.toString());
        }
        return false;
    }

    /**
     * 打开gprs
     *
     * @return
     */
    public void openGprs(final OnGprsListener gprsListener, int waitTime) {
        //.d(TAG, "openGprs func");

        isOpenGprs = false;
        //		isBack = false;      //已经回调
        onGprsListener = gprsListener;

//		System.out.println(" onGprsListener ==  "+onGprsListener);
        try {
//			System.out.println(" is_gemini ==  "+  is_gemini);
            if (is_gemini) {  //双卡双待手机
//				System.out.println(" 双卡双待手机 "+  is_gemini);
                int slotID = MtkDoubleSimUtil.getSlotByOperator(m_context);   //获取目标卡槽ID
                MtkDoubleSimUtil.enableGprs(m_context, slotID, true, 2 * DELAY_CLOSE_WIFI);  //打开gprs，切换simID 并在10秒后关闭wifi
            } else {
//				System.out.println(" 双卡双待手机 "+  is_gemini);
//				System.out.println("  m_context "+   m_context);
                ConnectivityManager connManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
//				System.out.println(" connManager "+  connManager );
                MobileDataUtil.setMobileDataEnabled(connManager, true);
            }
            //.d(TAG, "openGprs func onNetStateChangeCallBack");
//			System.out.println( "openGprs func onNetStateChangeCallBack");
            //监听网络变化情况
            onNetStateChangeCallBack = new OnNetStateChangeCallBack() {
                public void netStateChange(boolean isWifiEnable, boolean isGprsEnable) {
                    //.d(TAG, "isWifiEnable = " + isWifiEnable + ";isGprsEnable = " + isGprsEnable);
//					System.out.println("isWifiEnable ===  " + isWifiEnable + "   +isGprsEnable ==== " + isGprsEnable);


                    if (isGprsEnable && !isWifiEnable) {      //gprs打开，而wifi是关闭的
                        //	//.d(TAG, "openGprs func onNetStateChangeCallBack Thread.sleep 5 sec");

                        try {        //确定网络切换后睡眠5秒钟，提高成功率
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                        }
                        if (!isOpenGprs) {
                            if (onGprsListener != null) {
                                //.d(TAG, "openGprs func onNetStateChangeCallBack callback --onGprsState-> ");
                                isOpenGprs = true;
                                // 如果使用gprsListener,不能设置为null 存在重复调用
                                onGprsListener.onGprsState(true);
                                onGprsListener = null;
                            }
                            onNetStateChangeCallBack = null;

                            // gprs已经打开，关闭计时器
                            if (timeJudge != null) {
                                timeJudge.close();
                                timeJudge = null;
                            }
                        }
                    }
                }
            };

            //	Logs.d(TAG, "new TimeJudge");
            // 计时器
            timeJudge = new TimeJudge(waitTime * 1000, new OnActionListener() {
                public void onActionFinished(int actionCode, int resultCode, Object data) {
                    // 超时，Gprs没有找开
                    if (!isOpenGprs) {
                        if (onGprsListener != null) {
                            //.d(TAG, "timeJudge ---> " );
                            boolean isGprsOpen = NetControlUtil.getInstance(m_context).isGprsConnected();
                            //		Logs.d(TAG, "timeJudge --isGprsOpen-> " + isGprsOpen );
                            if (isGprsOpen) {
                                isOpenGprs = true;
                            }
                            onNetStateChangeCallBack = null;
                            // 如果使用gprsListener,不能设置为null 存在重复调用
                            onGprsListener.onGprsState(false);
                            onGprsListener = null;
                        }
                    }

                    // 超时，关闭计时器
                    if (timeJudge != null) {
                        timeJudge.close();
                        timeJudge = null;
                    }
                }
            }, 0);
            //	Logs.d(TAG, "timeJudge.start()");
            timeJudge.start();              //设置计时器   超时则做切换失败处理
        } catch (Exception e) {
            e.printStackTrace();
            //	Logs.e(TAG, "NetControl：003:" + e.toString());
        }

        //	Logs.d(TAG, "gprsworkstart,001 open");
    }

    /**
     * 关闭gprs
     */
    public void closeGprs(boolean isEnforceClose) {
        //.d(TAG, "closeGprs --- > isOpenGprs = " + isOpenGprs + ",isEnforceClose = " + isEnforceClose);
        if (isOpenGprs || isEnforceClose) {
            if (!isEnforceClose) {
                isOpenGprs = false;
            }

            try {
                if (is_gemini) { // 双卡双待手机
                    MtkDoubleSimUtil.disableGprs(m_context, true, DELAY_CLOSE_WIFI);
                } else {
                    ConnectivityManager connManager = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    MobileDataUtil.setMobileDataEnabled(connManager, false);
                }
            } catch (Exception e) {
                //.e(TAG, "closeGprs error:" + e.toString());
            }
            //.d(TAG,"gprs close work start");

            boolean gprsisclose = false;
            for (int i = 0; i < 3; i++) {
                // 网络处于关闭状态，移除打开网络的标识
                ConnectivityManager connManager2 = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
                State gprs = connManager2.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

                //.d(TAG, "for " + i + ",gprs is close = " + gprsisclose);
                if (gprs == State.CONNECTED) {
                    //再次关闭网络，循环3次
                    MobileDataUtil.setMobileDataEnabled(connManager2, false);
                } else {
                    gprsisclose = true;
                    //.d(TAG, "gprsclosesuccess");
                    break;
                }

                //计时器
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    //.e(TAG, "sleep error:" + e.toString());
                }
            }
        }
    }

    /**
     * 控制wifi，
     *
     * @param enableWifi true打开 ； false关闭
     */
    public void setWifiEnabled(boolean enableWifi) {
        try {
            WifiManager mWm = (WifiManager) m_context.getSystemService(Context.WIFI_SERVICE);
            if (mWm != null) {
                mWm.setWifiEnabled(enableWifi);
            }
        } catch (Exception e) {
            //.e(TAG, "WCApn：0055:" + e.toString());
        }
    }
}
