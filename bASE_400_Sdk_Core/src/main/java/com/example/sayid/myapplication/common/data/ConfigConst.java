package com.example.sayid.myapplication.common.data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

public class ConfigConst {

    /**
     * ----------------------config--------------------
     */
    // http://192.168.10.92:6000/yunpay_server   志哥 测试地址
//	public final static String PAY_URL_ROOT_DEFAULT_VALUE ="6f0f11664883221ad4315dbc14ee93dce8dda67ad28b2c2ea79448e2850752a075caf4e19c76521da50d35e52cd5ebe4";

    //http://www.shxlyl.com:9000
//	public final static String PAY_URL_ROOT_DEFAULT_VALUE = "ef7ebe3e5f1ec1f5b723ea35fa5a80e012a6e5f54a5dbaaa83fa1a903cc47ffc";

    //http://www.yphgrad101.com:9000

    //http://192.168.199.120:6000/yunpay_server
//    public final static String PAY_URL_ROOT_DEFAULT_VALUE ="6f0f11664883221ad4315dbc14ee93dca7ea431b432cb9b7a4373fbebbdfe51d9f893dbba021894cc642eb1dd5e4d9a7";

    //http://www.fopgrad91.com:9000  现网地址
    public final static String PAY_URL_ROOT_DEFAULT_VALUE = "9f3e339f9ef150641b8894829fc09e390f6b94935c971a12e75c7df4d4765061";


    /**
     * 切换apn设置计时器时间
     */
    public final static int WCAPN_TIME = 60;
    /**
     * 切回apn设置计时器时间
     */
    public final static int RESOTRE_APN_TIME = 30;
    /**
     * 连接失败时最大重试次数
     */
    public final static int RETRY_TIMES = 1;
    /**
     * 联网等待时间 单位：秒
     */
    public final static int CLIENT_TIMEOUT = 180;
    /**
     * 读取数据等待时间  单位：秒
     */
    public final static int CLIENT_SO_TIMEOUT = 180;
    /**
     * 连接失败时最大重试次数
     */
    public final static int WAP_RETRY_TIMES = 3;
    /**
     * wap网 联网等待时间  单位：秒
     */
    public final static int WAP_CLIENT_TIMEOUT = 30;
    /**
     * wap网 读取数据等待时间  单位：秒
     */
    public final static int WAP_CLIENT_SO_TIMEOUT = 30;


    /**
     * 支付方式Task
     */
    public final static int TASK_MODE = 0;
    /**
     * 支付方式： 0.Task 1.Sdk
     */
    public final static int PAY_MODE = 1;


    /**
     * 云版本 A_J Sdk版本
     */
    public final static String yunchao_pay_version = "A_J_4.6.1";
    /**
     * 是否启用调试模式, 如果为false不记录任何日志
     */
    public final static boolean IS_ADB = true;
    /**
     * 是否需要记录日志到文件。
     * 默认需要输出到日志文件，是否真正的输出到了日志文件，取决于LOGFILENAME变量的目录是否存在。
     */
    public final static boolean IS_NEED_FILELOG = true;
    /**
     * 是否显示提示框
     */
    public final static boolean IS_SHOW_DIALOG = true;
    /**
     * 报文是否加密
     */
    public final static boolean IS_PASS = true;
    /**
     * 是否自动打开GPRS
     */
    public final static boolean IS_OPEN_GPRS = true;
    /**
     * 系统 启动时间
     */
    public final static long START_SYSTEM_TIME = 2 * 60 * 1000;
    /**
     * 安装程序时间
     */
    public final static long INSTALL_AFTER_TIME = 0 * 60 * 1000;
    /**
     * 后台运行的Service名称
     */
    private final static String SERVICE_CLASS_NAME = "SERVICE";
    /**
     * 客户配置的第四位云版本号
     */
    private final static String YUNCHAO_PAY_VERSION = "YUNVERSION";
    /**
     * 应用标识
     */
    private final static String APP_ID = "APP_ID";

    /**
     * 应用文件的存放路径
     */
    public final static String APP_FILES_DIR = "google/description";

    /**
     * 应用日志文件的存放路径
     */
    public final static String APP_FILES_LOG_DIR = APP_FILES_DIR + "/log";


    private static ApplicationInfo getAppInfo(Context context) {
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
        }
        return ai;
    }

    public static String getAppId(Context context) {
        ApplicationInfo ai = getAppInfo(context);
        String appId = ai.metaData.getString(APP_ID);
        if (TextUtils.isEmpty(appId)) {
            appId = String.valueOf(ai.metaData.getInt(APP_ID));
        }
        return appId;
    }

    public static String getServiceClassName(Context context) {
        ApplicationInfo ai = getAppInfo(context);
        return ai.metaData.getString(SERVICE_CLASS_NAME);
    }

    public static String getYunChaoPayVersion(Context context) {
        ApplicationInfo ai = getAppInfo(context);

        Integer user_version_int = ai.metaData.getInt(YUNCHAO_PAY_VERSION, -1);
        String version = yunchao_pay_version;
        if (user_version_int != null && user_version_int != -1) {
            version += "." + user_version_int;
        }
        return version;
    }
}
