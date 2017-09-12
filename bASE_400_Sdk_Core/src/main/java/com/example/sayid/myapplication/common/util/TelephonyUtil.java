package com.example.sayid.myapplication.common.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.WebView;

import com.example.sayid.myapplication.common.data.StringData;
import com.example.sayid.myapplication.pay.AppTache;

public class TelephonyUtil {

    private final static String TAG = "ImsiIU";

    /**
     * 判断是否模拟器。如果返回TRUE，则当前是模拟器
     *
     * @param context
     * @return
     */
    public static boolean isEmulator(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (imei == null || imei.equals("000000000000000")) {
                return true;
            }
        } catch (Exception ioe) {
            //.e(TAG, "isEmulator error:" + ioe.toString());
        }

        return false;
    }

    /**
     * 判断SIM卡是否准备好
     *
     * @param context
     * @return
     */
    public static boolean isSimReady(Context context) {
        try {
            // 判断是否为双卡双待
            if (MtkDoubleSimUtil.isGemini(context)) {
                if (MtkDoubleSimUtil.getSlotIdReady(context) != -1) {
                    return true;
                }
            }

            //
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = mTelephonyManager.getSimState();
            if (simState == TelephonyManager.SIM_STATE_READY) {
                return true;
            }

        } catch (Exception e) {
            //.e(TAG, "isSimReady error:" + e.toString());
        }

        return false;
    }

    /**
     * 获取imsi号
     * 双卡根据运营商选择，选择顺序：移动，联通，电信
     *
     * @param context
     */
    public static String getImsi(Context context) {
        String imsi = "";
        try {
            if (MtkDoubleSimUtil.isGemini(context)) {      //双卡双待手机
                int slotId = MtkDoubleSimUtil.getSlotByOperator(context);  //根据运营商选择
                imsi = MtkDoubleSimUtil.getImsiBySlot(context, slotId);
                //.d(TAG, "slotId = " + slotId + ";imsi = " + imsi);
            }

            // 为空
            if (TextUtils.isEmpty(imsi)) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                imsi = tm.getSubscriberId();
            }
        } catch (Exception e) {
            //.e(TAG, "getImsi error:" + e.toString());
        }

        return imsi;
    }


    /**
     * 获取imei号
     * 双卡根据运营商选择，选择顺序：移动，联通，电信
     *
     * @param context
     */
    public static String getImei(Context context) {
        String imei = "";
        try {
            if (MtkDoubleSimUtil.isGemini(context)) {      //双卡双待手机
                int slotId = MtkDoubleSimUtil.getSlotByOperator(context);  //根据运营商选择
                imei = MtkDoubleSimUtil.getImeiBySlot(context, slotId);
                //.d(TAG, "slotId = " + slotId + ";imei = " + imei);
            }

            // 为空
            if (TextUtils.isEmpty(imei)) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                imei = tm.getDeviceId();
            }
        } catch (Exception e) {
            //.e(TAG, "getImei error:" + e.toString());
        }
        return imei;
    }

    /**
     * 获取iccid号
     * 双卡根据运营商选择，选择顺序：移动，联通，电信
     *
     * @param context
     */
    public static String getIccid(Context context) {
        String iccid = "";
        try {
            if (MtkDoubleSimUtil.isGemini(context)) {      //双卡双待手机
                int slotId = MtkDoubleSimUtil.getSlotByOperator(context);  //根据运营商选择
                iccid = MtkDoubleSimUtil.getIccidBySlot(context, slotId);
                //.d(TAG, "slotId = " + slotId + ";imei = " + iccid);
            }

            // 为空
            if (TextUtils.isEmpty(iccid)) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                iccid = tm.getSimSerialNumber();
            }
        } catch (Exception e) {
            //.e(TAG, "getIccid error:" + e.toString());
        }
        return iccid;
    }


    /**
     * 获取手机号码
     *
     * @param context
     * @param imsi
     * @return
     */
    public static void getPhoneNum(final Context context, final String imsi) {
        try {
            if (context == null || TextUtils.isEmpty(imsi)) {
                return;
            }

            String phone = "";

            boolean isGemini = MtkDoubleSimUtil.isGemini(context);         //判断是否为双卡双待手机
            // 系统函数直接获取手机号码 -------------channel 10
            //.d(TAG, "isGemini = " + isGemini);
            if (isGemini) {
                int operatorID = MtkDoubleSimUtil.getSlotByOperator(context);  //根据运营商选择
                phone = MtkDoubleSimUtil.getPhoneBySlot(context, operatorID);
            }

            // 为空
            if (TextUtils.isEmpty(phone)) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                phone = StringUtil.clearPrefix86(tm.getLine1Number());
            }

            if (StringUtil.length(phone) > 0) {
                CacheUtil.getInstance().setString(imsi, phone);
                CacheUtil.getInstance().setInt(phone, 9);
            }
        } catch (Exception e) {
            //.e(TAG, "getPhoneNum error:" + e.toString());
        }


        return;
    }

    /**
     * 从收件箱的短信里获取短信中心号
     *
     * @param imsi
     * @return
     */
    public static void getSmscBox(final Context context, String imsi) {
        if (context == null || TextUtils.isEmpty(imsi)) {
            return;
        }

        String service_center = "";
        Cursor cursor = context.getContentResolver().query(Uri.parse(StringData.getInstance().URI_SMS_INBOX), null,
                null, null, "date desc limit 10");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                //.d(TAG, "address = " + address);
                if (address.startsWith("10")) {
                    if (MtkDoubleSimUtil.isGemini(context)) {
                        try {
                            String sim_id = cursor.getString(cursor.getColumnIndexOrThrow("sim_id"));
                            int simID = Integer.parseInt(sim_id);
                            String imsi_d = MtkDoubleSimUtil.getImsiBySimID(context, simID);

                            if (imsi != null && !imsi.equals("") && imsi_d != null && imsi_d.equals(imsi)) {
                                service_center = cursor.getString(cursor.getColumnIndexOrThrow("service_center"));

                                if (service_center != null && !service_center.equals("")) {
                                    break;
                                }
                            }
                        } catch (Exception e) {
//							Logs.e(TAG, "getSmscBox error:" + e.toString());
                        }
                    } else {
                        service_center = cursor.getString(cursor.getColumnIndexOrThrow("service_center"));
                        if (service_center != null && !service_center.equals("")) {
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        CacheUtil.getInstance().setString("scenter" + imsi, StringUtil.clearPrefix86(service_center));
        return;
    }

    /**
     * 获取本机UA
     *
     * @param context
     * @return
     */
    public static void getUserAgent(final Context context) {
        if (context == null) {
            return;
        }

        CacheUtil.getInstance().setString("ua", new WebView(AppTache.context).getSettings().getUserAgentString());
    }
}