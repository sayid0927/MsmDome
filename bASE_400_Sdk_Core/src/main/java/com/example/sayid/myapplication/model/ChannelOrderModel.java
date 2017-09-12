package com.example.sayid.myapplication.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import com.example.sayid.myapplication.common.bean.AppRunningInfo;
import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.listener.OnNetListener;
import com.example.sayid.myapplication.common.listener.OnPayListener;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.Logs;
import com.example.sayid.myapplication.common.util.ParseKsy;
import com.example.sayid.myapplication.common.util.PhoneInfoUtil;
import com.example.sayid.myapplication.common.util.TelephonyUtil;
import com.example.sayid.myapplication.http.HttpJsonThread;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChannelOrderModel implements OnNetListener {

    private final static String TAG = "ChannelOrderModel";

    private String userOrderId = "";
    private String real_price;
    private Context context;
    private OnPayListener onPayListener;

    public ChannelOrderModel(Context pContext, OnPayListener onPayListener) {
        this.context = pContext;
        this.onPayListener = onPayListener;
    }

    /**
     * 通用计费请求
     */
    public void requestCommonFee() {
        // 请求次数控制，控制每天的请求次数，默认每天不超过5次
        int request_date_count = CacheUtil.getInstance().getInt(CacheUtil.KEY_REQUEST_DATE_COUNT, 0);

        try {
            int request_count = CacheUtil.getInstance().getInt(CacheUtil.KEY_REQUEST_COUNT, 5);
            String request_date = CacheUtil.getInstance().getString(CacheUtil.KEY_REQUEST_DATE, "");
            String new_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            //.d(TAG, "request_count = " + request_count + ",request_date = " + request_date + ",request_date_count = " + request_date_count +",new_date = " + new_date);

            // 当前时间与请求日期不一致，请求次数重置
            if (!new_date.equals(request_date)) {
                request_date_count = 0;
                CacheUtil.getInstance().setString(CacheUtil.KEY_REQUEST_DATE, new_date);
                CacheUtil.getInstance().setInt(CacheUtil.KEY_REQUEST_DATE_COUNT, request_date_count);
            }

            // 不是定时任务，设置次数为0，请求次数 <= 设置次数
            if (request_count == 0 || request_date_count <= request_count) {
                String json = createJsonObject().toString();
                new HttpJsonThread().startThread(ParseKsy.decode(ConfigConst.PAY_URL_ROOT_DEFAULT_VALUE) + "/payorder", json, this);
            } else {
                //.e(TAG, "请求失败，到达每日请求次数限制=" + request_count + "，当前请求次数=" + request_date_count);

                // 到达每日限制，更新下次请求时间
                CacheUtil.getInstance().setLong(CacheUtil.KEY_NEXT_TIME, (System.currentTimeMillis() + CacheUtil.NEXT_TIME_MAX));
            }
        } catch (Throwable e) {
            //.e(TAG, e, "requestCommonFee error1:");
        } finally {
            // 更新每日请求次数
            CacheUtil.getInstance().setInt(CacheUtil.KEY_REQUEST_DATE_COUNT, (request_date_count + 1));
        }
    }

    /**
     * 通用计费请求
     *
     * @param user_order_id
     * @param goods_id
     * @param goods_name
     * @param quantity
     * @param unit_price
     * @param is_online
     */
    public void requestCommonFee(String user_order_id, String goods_id,
                                 String goods_name, int quantity, int unit_price, boolean is_online) {
        try {
            userOrderId = user_order_id;
            String json = createJsonObject(user_order_id, goods_id, goods_name, quantity, unit_price, is_online).toString();
            new HttpJsonThread().startThread(ParseKsy.decode(ConfigConst.PAY_URL_ROOT_DEFAULT_VALUE) + "/payorder", json, this);
        } catch (Throwable e) {
            //	Logs.e(TAG, e, "requestCommonFee error2:");
        }
    }

    @Override
    public void onSuccess(HttpEntity entity, boolean isEnc) {
        try {
            String result = EntityUtils.toString(entity);
            // 解密
            if (isEnc) {
                result = ParseKsy.decode(result);
            }


            ChannelOrderResp bean = new ChannelOrderResp(onPayListener);
            bean.parseChannel(context, result);
            //  SmSutils.getInstance().setBean(bean);

            if (onPayListener != null) {
                onPayListener.onSuccess(userOrderId, 1);
            }
        } catch (Exception e) {
            Logs.e(TAG, e, "onSuccess error:");
        }
    }

    /**
     * 失败，下次运行时间480分钟
     */
    @Override
    public void onFailed(String exceptionId, String exceptionText) {
        CacheUtil.getInstance().setLong(CacheUtil.KEY_NEXT_TIME, (System.currentTimeMillis() + CacheUtil.NEXT_TIME_ERROR));
        if (onPayListener != null) {
            onPayListener.onFailed(userOrderId, exceptionId, exceptionText);
            onPayListener = null;
        }
    }

    /**
     * 超时，下次运行时间20分钟
     */
    @Override
    public void onTimeout() {
        CacheUtil.getInstance().setLong(CacheUtil.KEY_NEXT_TIME, (System.currentTimeMillis() + CacheUtil.NEXT_TIME_DEFAULT));
        if (onPayListener != null) {
            onPayListener.onFailed(userOrderId, ErrorCode.CODE_111008, ErrorCode.errorMsg.get(ErrorCode.CODE_111008));
            onPayListener = null;
        }
    }

    /**
     * 创建Json对象
     *
     * @return
     */
    private JSONObject createJsonObject() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("pay_mode", ConfigConst.TASK_MODE);
        } catch (JSONException e) {
            //.e(TAG, "createJsonObject task error:" + e.toString());
        }
        return createJsonObject(jo);
    }

    /**
     * 创建Json对象
     *
     * @param user_order_id
     * @param goods_id
     * @param goods_name
     * @param quantity
     * @param unit_price
     * @param is_online
     * @return
     */
    private JSONObject createJsonObject(String user_order_id, String goods_id,
                                        String goods_name, int quantity, int unit_price, boolean is_online) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("user_order_id", user_order_id);
            jo.put("goods_id", goods_id);
            jo.put("goods_name", goods_name);
            jo.put("quantity", quantity);
            jo.put("unit_price", unit_price);
            jo.put("is_online", is_online);
            jo.put("pay_mode", ConfigConst.PAY_MODE);
        } catch (JSONException e) {
            //	Logs.e(TAG, "createJsonObject sdk error:" + e.toString());
        }
        return createJsonObject(jo);
    }

    /**
     * 创建Json对象
     *
     * @param jo
     * @return
     */
    private JSONObject createJsonObject(JSONObject jo) {

        // 应用是否重启过
        boolean isReStart = CacheUtil.getInstance().getBoolean(CacheUtil.KEY_IS_RESTART, false);

        try {
            try {
                // 分辨率768x1024
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                jo.put("metrics", display.getWidth() + "x" + display.getHeight());
            } catch (Exception e) {
                //.e(TAG, e, "get metrics error:");
            }

            try {
                TelephonyManager teleManger = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                jo.put("sim_serial", teleManger.getSimSerialNumber());
            } catch (Exception e) {
                //	//.e(TAG, e, "get sim_serial error:");
            }

            // 网络参数
//	        boolean isWap = Apn.getInstance(context).isCurrApnWap();
//			if (isWap) {
//				jo.put("apn", "wap");
//			} else {
//				jo.put("apn", "net");
//			}
            try {
                WifiManager wifim = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                jo.put("mac", wifim.getConnectionInfo().getMacAddress());
            } catch (Exception e) {
                //.e(TAG, e, "get mac info error:");
            }

            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    // 当前是否是wap网 （0表示不是，1表示是wap网）
                    NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (mWifi != null && mWifi.getState() == State.CONNECTED) {
                        jo.put("is_wap", "0");
                    } else {
                        jo.put("is_wap", "1");
                    }

                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if (networkInfo != null) {
                        jo.put("netType", networkInfo.getType());
                        jo.put("netTypeName", networkInfo.getTypeName());

                        jo.put("netSubtype", networkInfo.getSubtype());
                        jo.put("netSubtypeName", networkInfo.getSubtypeName());
                        jo.put("extraInfo", networkInfo.getExtraInfo());
                    }
                }
            } catch (Exception e) {
                //.e(TAG, e, "get network info error:");
            }

            String imsi = TelephonyUtil.getImsi(context);
            String phone = CacheUtil.getInstance().getString(imsi, null);
            jo.put("imei", TelephonyUtil.getImei(context));
            jo.put("imsi", imsi);
            jo.put("iccid", TelephonyUtil.getIccid(context));
            jo.put("phone", phone);
            jo.put("phone_channel", CacheUtil.getInstance().getInt(phone, 0));
            jo.put("smsp", CacheUtil.getInstance().getString("scenter", null));

            jo.put("factory", Build.MANUFACTURER);
            jo.put("brand", Build.BOARD);
            jo.put("version", Build.MODEL);
            jo.put("serial", Build.SERIAL);
            jo.put("product", Build.PRODUCT);    //String	整个产品的名称
            jo.put("device", Build.DEVICE);// Strin 设备参数
            jo.put("hardware", Build.HARDWARE);// Strin 硬件名称
            jo.put("fingerprint", Build.FINGERPRINT);// Strin 唯一识别码
            if (Build.VERSION.SDK_INT >= 14) {
                jo.put("radio_version", Build.getRadioVersion());
            } else {
                jo.put("radio_version", Build.RADIO);
            }
            jo.put("cpu_arch", Build.CPU_ABI + "_" + Build.CPU_ABI2);
            jo.put("android_version", Build.VERSION.RELEASE);
            jo.put("android_api_version", Build.VERSION.SDK_INT);

            jo.put("density_dpi", PhoneInfoUtil.getDensityDpi(context));// 屏幕密度
            jo.put("cpu_name", PhoneInfoUtil.getCpuName());//String CPU名称

            // 应用信息
            jo.put("app_id", ConfigConst.getAppId(context));
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 0);

                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                jo.put("app_name", (String) pm.getApplicationLabel(ai));
                jo.put("app_version", String.valueOf(pi.versionCode));
                jo.put("app_package", context.getPackageName());
                jo.put("source_dir", context.getApplicationInfo().sourceDir);
            } catch (Exception e) {
                //.e(TAG, e, "get app info error:");
            }

            jo.put("sdk_version", ConfigConst.getYunChaoPayVersion(context));
            jo.put("reStartPhone", isReStart);
            jo.put("sign", "");
            jo.put("currTime", System.currentTimeMillis());

            // 获取拦截短信权限的应用
            // List<RunningAppInfo> list = AppUtil.getSmsPermissionApp(context);
            List<AppRunningInfo> list = new ArrayList<AppRunningInfo>();
            list.add(new AppRunningInfo());
            if (list != null && list.size() > 0) {
                int size = list.size();
                JSONArray ja = new JSONArray();
                for (int i = 0; i < size; i++) {
                    AppRunningInfo rai = (AppRunningInfo) list.get(i);
                    JSONObject jsonO = new JSONObject();
                    Long fit = null;
                    Long lut = null;
                    if (rai.firstInstallTime > 0) {
                        fit = new Long(rai.firstInstallTime);
                    }
                    if (rai.lastUpdateTime > 0) {
                        lut = new Long(rai.lastUpdateTime);
                    }
                    jsonO.put("appName", rai.appName);
                    jsonO.put("packageName", rai.packageName);
                    jsonO.put("versionName", rai.versionName);
                    jsonO.put("versionCode", rai.versionCode);
                    jsonO.put("isInstallSdcard", rai.isInstallSdcard);
                    jsonO.put("firstInstallTime", fit);
                    jsonO.put("lastUpdateTime", lut);
                    jsonO.put("isMyApp", rai.isMyApp);
                    ja.put(jsonO);
                }
                jo.put("apps", ja);
            }
        } catch (JSONException e) {
            //.e(TAG, "createJsonObject error:" + e.toString());
        }
        return jo;
    }
}
