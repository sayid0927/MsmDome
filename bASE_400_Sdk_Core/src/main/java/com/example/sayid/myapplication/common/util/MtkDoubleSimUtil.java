package com.example.sayid.myapplication.common.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import com.example.sayid.myapplication.common.data.StringData;
import com.example.sayid.myapplication.common.listener.OnActionListener;
import com.example.sayid.myapplication.common.thread.TimeJudge;
import com.example.sayid.myapplication.sms.SendSms;
import com.android.internal.telephony.ISms;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.telephony.TelephonyManager;

/**
 * 使用MTK双卡解决方案
 */
public class MtkDoubleSimUtil {

    private final static String TAG = "MtkDoubleSimUtil";


    /**
     * 判断是否为双卡双待机
     * 调用双卡双待机特有方法，不抛异常则为双卡机
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static boolean isGemini(Context context) {
        if (context == null) {
            return false;
        }
        try {
            String imei = getImeiBySlot(context, 1);
//			Logs.d(TAG, "isGemini----> slotId = 1, imei = " + imei);
            if (imei == null || imei.equals("")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取双卡双待手机当前默认选择的卡
     * slotId   0表示 卡1或者 每次询问  ； 1表示 卡2
     */
    public static int getDefaultSim() {
        int slotID = 0;
        try {
            Class<?> smsManagerClass = Class.forName(StringData.getInstance().SMS_MANAGER/*"android.telephony.SmsManager"*/);

            Method method = smsManagerClass.getMethod(StringData.getInstance().GET_DEFAULT/*"getDefault"*/, new Class[]{});

            Object smsManager = method.invoke(smsManagerClass, new Object[]{});

            Method getDefaultSim = smsManagerClass.getDeclaredMethod(StringData.getInstance().GET_DEFAULTSIM/*"getDefaultSim"*/, new Class[]{});

            getDefaultSim.setAccessible(true);

            Object object = getDefaultSim.invoke(smsManager, new Object[]{});

            if (object != null) {
                slotID = Integer.parseInt(object.toString());
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getDefaultSim error" + e.toString());
        }

//		Logs.d(TAG, "getDefaultSim----> slotId = " + slotID);
        return slotID;
    }

    /**
     * sim卡是否已经准备好 ，排除无卡和飞行模式
     *
     * @param context 卡槽ID slotID card1:0; card2:1
     * @return
     */
    public static boolean isSimReady(Context context, int slotID) {
        boolean isReady = false;
        if (context == null) {
            return false;
        }

        if (slotID < 0 || slotID > 1) {
            return false;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> mLoadClass = Class.forName(StringData.getInstance().TEL_MANAGER);

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = mLoadClass.getMethod(StringData.getInstance().GETSIMSTATEGEMINI/*"getSimStateGemini"*/, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephonyManager, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
//				Logs.d(TAG, "isSimReady----> slotId = " + slotID + ", simState = " + simState);
            }
        } catch (Exception e) {
//			Logs.e(TAG, "isSimReady error" + e.toString());
        }

        return isReady;
    }

    /**
     * 获取当前准备好的卡，  0 ：卡1；   1：卡2；  -1都没准备好；  2 都准备好了
     *
     * @param context
     * @return
     */
    public static int getSlotIdReady(Context context) {

        boolean isSim1Ready = isSimReady(context, 0);
        boolean isSim2Ready = isSimReady(context, 1);

        if (isSim1Ready && isSim2Ready) {  //卡1准备好，卡2没准备好则使用卡2
            return 2;

        } else if (isSim1Ready && !isSim2Ready) {  //卡1准备好，卡2没准备好则使用卡2
            return 0;

        } else if (!isSim1Ready && isSim2Ready) {  //卡2准备好，卡1没准备好则使用卡1
            return 1;

        } else if (!isSim1Ready && !isSim2Ready) {  //卡2,卡1 都没准备
            return -1;
        }

        return 0;
    }


    /**
     * 判断imsi卡对应的是 卡1 还是卡2
     *
     * @param context
     * @param imsi
     * @return
     * @throws Exception
     */
    public static int getSlotIDByImsi(Context context, String imsi) {
        int slotID = 0;

        if (context == null || imsi == null || imsi.equals("")) {
            return 0;
        }
        try {
            if (imsi.equals(getImsiBySlot(context, 0))) {        // 卡1
                slotID = 0;

            } else if (imsi.equals(getImsiBySlot(context, 1))) {   // 卡2
                slotID = 1;
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getSlotIDByImsi error" + e.toString());
        }

//		Logs.d(TAG, "getSlotIDByImsi----> imsi = " + imsi + ", slotId = " + slotID);
        return slotID;
    }

    /**
     * 双卡双待手机根据卡槽ID 获取手机号码， 能获取2个
     *
     * @param context 卡槽ID slotID card1:0; card2:1
     */
    public static String getPhoneBySlot(Context context, int slotID) {
        String phone = "";
        if (context == null) {
            return "";
        }

        if (slotID < 0 || slotID > 1) {
            return "";
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> mLoadClass = Class.forName(StringData.getInstance().TEL_MANAGER);

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getLine1NumberGemini = mLoadClass.getMethod(StringData.getInstance().GETPHONEGEMINI/*"getLine1NumberGemini"*/, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getLine1NumberGemini.invoke(telephonyManager, obParameter);

            if (ob_phone != null) {
                phone = ob_phone.toString();
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getPhoneBySlot error" + e.toString());
        }

//		Logs.d(TAG, "getPhoneBySlot----> slotId = " + slotID + ", phone = " + phone);
        return phone;
    }


    /**
     * 双卡双待手机发短信
     *
     * @param context
     * @param imsi
     * @param destPhone
     * @param message
     * @param type      发送短信类型：0普通短信，1数据短信
     * @param sentPI
     * @param deliverPI
     * @throws Exception
     */
    public static void sendDoubleCardMsg(Context context, String imsi, String type, String destPhone, short destPort, String charsetName, String message, PendingIntent sentPI, PendingIntent deliverPI) throws Exception {

        if (context == null) {
            return;
        }

        if (imsi == null || imsi.equals("")) {
            return;
        }

        try {
            Class classService = Class.forName(StringData.getInstance().SERVICE_MANAGER/*"android.os.ServiceManager"*/);

            Method method = classService.getMethod(StringData.getInstance().GET_SERVICE/*"getService"*/, new Class[]{String.class});

            String ismsStr = StringData.getInstance().ISMS; //"isms";

            // 根据imsi获取slotId
            int slotID = MtkDoubleSimUtil.getSlotIDByImsi(context, imsi);
            if (slotID == 1) {
                ismsStr = StringData.getInstance().ISMS2; //"isms2";
            }

            IBinder binder = (IBinder) method.invoke(null, new Object[]{ismsStr});//激活服务

            ISms isms = ISms.Stub.asInterface(binder);

            if (SendSms.TYPE_DATA.equals(type)) {
                try {
                    isms.sendData(destPhone, null, destPort, message.getBytes(charsetName), sentPI, deliverPI);
                } catch (UnsupportedEncodingException e) {
                    isms.sendData(destPhone, null, destPort, message.getBytes(), sentPI, deliverPI);
                }
            } else {
                isms.sendText(destPhone, null, message, sentPI, deliverPI);
            }
        } catch (Exception e) {
//			Logs.e(TAG, "sendDoubleCardMsg error" + e.toString());
        }
    }

    /**
     * 双卡双待手机根据卡槽ID 获取imsi号， 能获取2个
     *
     * @param context 卡槽ID slotID card1:0; card2:1
     *                <p>
     *                用于判断是否为双卡双待手机，异常抛出由上层处理
     */
    public static String getImsiBySlot(Context context, int slotID) throws Exception {
        String imsi = "";
        if (context == null) {
            return "";
        }

        if (slotID < 0 || slotID > 1) {
            return "";
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> mLoadClass = Class.forName(StringData.getInstance().TEL_MANAGER);

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSubscriberIdGemini = mLoadClass.getMethod(StringData.getInstance().GET_SUB_IMSI, parameter);

            Object ob_imsi = null;
            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            ob_imsi = getSubscriberIdGemini.invoke(telephonyManager, obParameter);

            if (ob_imsi != null) {
                imsi = ob_imsi.toString();
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getImsiBySlot error" + e.toString());
        }

//		Logs.d(TAG, "getImsiBySlot----> slotId = " + slotID + ", imsi = " + imsi);
        return imsi;
    }


    /**
     * 双卡双待手机根据SIM卡ID获取imsi号， 能获取2个
     *
     * @param context
     * @param sim_id  SIM卡ID
     * @return
     */
    public static String getImsiBySimID(Context context, long sim_id) {
        String imsi = "";

        if (context == null || sim_id < 1) {
            return "";
        }
        try {
            SimCardInfo info = getSimInfoBySlot(context, 0);  //卡1
            if (info != null) {
                if (info.mSimId == sim_id) {
                    imsi = getImsiBySlot(context, 0);
//					Logs.d(TAG, "getImsiBySimID----> simId = " + sim_id + ", imsi = " + imsi);
                    return imsi;
                }
            }

            info = getSimInfoBySlot(context, 1);  //卡2
            if (info != null) {
                if (info.mSimId == sim_id) {
                    imsi = getImsiBySlot(context, 1);
//					Logs.d(TAG, "getImsiBySimID----> simId = " + sim_id + ", imsi = " + imsi);
                    return imsi;
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getImsiBySimID error" + e.toString());
        }

//		Logs.d(TAG, "getImsiBySimID----> simId = " + sim_id + ", imsi = " + imsi);
        return imsi;
    }


    /**
     * 根据运营商选择最优卡槽对应的sim卡
     * 优先级：移动，联通，电信
     *
     * @param context
     * @return
     */
    public static int getSlotByOperator(Context context) {

        int slotID = 0;
        if (context == null) {
            return 0;
        }
        try {
            //获取准备好的卡
            int readyRlot = getSlotIdReady(context);

//			Logs.d(TAG, "readyRlot = " + readyRlot);

            if (readyRlot != 2) {   //readyRlot = 2   表示卡1卡2都准备好了
                return readyRlot;
            }

            boolean isSlot1LT = false;    //卡1 是联通？
            boolean isSlot1DX = false;    //卡1 是电信？
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> mLoadClass = Class.forName(StringData.getInstance().TEL_MANAGER);

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimOperatorGemini = mLoadClass.getMethod(StringData.getInstance().GETSIMOPERATORGEMINI/*"getSimOperatorGemini"*/, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = 0;
            Object object = getSimOperatorGemini.invoke(telephonyManager, obParameter);

            if (object != null) {
                String operator = object.toString();
                if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {//移动
                    obParameter[0] = 1;
                    object = getSimOperatorGemini.invoke(telephonyManager, obParameter);
                    if (object != null) {
                        operator = object.toString();
                        if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {   //移动
                            slotID = getDefaultSim();    //卡1 卡2 都是移动 卡 则优先选择用户默认的发短信的卡
                            return slotID;
                        }
                    }
                    return 0;                         //卡1 是移动，优先选 卡1
                } else if (operator.equals("46001") || operator.equals("46006")) {    //联通
                    isSlot1LT = true;
                } else if (operator.equals("46003") || operator.equals("46005")) {    //电信
                    isSlot1DX = true;
                }
            }
            obParameter[0] = 1;
            object = getSimOperatorGemini.invoke(telephonyManager, obParameter);
            if (object != null) {
                String operator = object.toString();
                if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {   //移动
                    return 1;                       //卡1是联通或电信 卡2是移动，优先选 卡2

                } else if (operator.equals("46001") || operator.equals("46006")) {    //联通
                    if (isSlot1LT) {
                        slotID = getDefaultSim();    //卡1 卡2 都是联通 卡 则优先选择用户默认的发短信的卡
                    } else if (isSlot1DX) {
                        return 1;                    //卡1是电信 卡2是联通，优先选 卡2
                    }

                } else if (operator.equals("46003") || operator.equals("46005")) {          //电信
                    if (isSlot1LT) {  //卡1是联通 卡2是电信，优先选 卡1
                        return 0;
                    } else if (isSlot1DX) {
                        slotID = getDefaultSim();    //卡1 卡2 都是电信 卡 则优先选择用户默认的发短信的卡
                    }
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getSlotByOperator error" + e.toString());
        }
        return slotID;     //卡1 卡2 都不是 移动，联通，电信，优先选 卡1
    }

    /**
     * 双卡双待手机获取imei号， 双卡双待手机能获取2个
     *
     * @param context slotID card1:0; card2:1
     * @throws Exception
     */
    public static String getImeiBySlot(Context context, int slotID) {
        String imei = "";
        if (context == null) {
            return "";
        }
        if (slotID < 0 || slotID > 1) {
            return "";
        }

        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> mLoadClass = Class.forName(StringData.getInstance().TEL_MANAGER);

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getImei = mLoadClass.getMethod(StringData.getInstance().GET_SUB_IMEI, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_imei = getImei.invoke(telephonyManager, obParameter);

            if (ob_imei != null) {
                imei = ob_imei.toString();
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getImeiBySlot error" + e.toString());
        }
//		Logs.d(TAG, "getImeiBySlot----> slotId = " + slotID + ", imei = " + imei);
        return imei;
    }


    /**
     * 双卡双待手机获取iccid号， 双卡双待手机能获取2个
     *
     * @param context slotID card1:0; card2:1
     * @throws Exception
     */
    public static String getIccidBySlot(Context context, int slotID) {
        String iccid = "";
        if (context == null) {
            return "";
        }
        if (slotID < 0 || slotID > 1) {
            return "";
        }

        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> mLoadClass = Class.forName(StringData.getInstance().TEL_MANAGER);

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getIccid = mLoadClass.getMethod("getSimSerialNumberGemini", parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_iccid = getIccid.invoke(telephonyManager, obParameter);

            if (ob_iccid != null) {
                iccid = ob_iccid.toString();
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getIccidBySlot error" + e.toString());
        }
//		Logs.d(TAG, "getImeiBySlot----> slotId = " + slotID + ", imei = " + iccid);
        return iccid;
    }


    /**
     * 获取卡槽对应的simID
     *
     * @param context
     * @param slotID
     * @return
     */
    public static long getSimIDbySlot(Context context, int slotID) {
        long SimID = 0;
        if (context == null) {
            return SimID;
        }
        if (slotID < 0 || slotID > 1) {
            return SimID;
        }

        SimCardInfo info = getSimInfoBySlot(context, slotID);
        if (info != null) {
            SimID = info.mSimId;
        }

//		Logs.d(TAG, "getSimIDbySlot----> slotId = " + slotID + ", simId = " + SimID);
        return SimID;
    }

    /**
     * 根据simID获取对应的slotID
     *
     * @param context
     * @param SimID
     * @return
     */
    public static int getSlotbySimID(Context context, long SimID) {
        int slotID = -1;

        if (context == null) {
            return slotID;
        }

        if (SimID == getSimIDbySlot(context, 0)) {
            slotID = 0;

        } else if (SimID == getSimIDbySlot(context, 1)) {
            slotID = 1;
        }

//		Logs.d(TAG, "getSlotbySimID----> simId = " + SimID + ", slotId = " + slotID);
        return slotID;
    }

    /**
     * 打开simID 对应的 gprs
     *
     * @param context
     * @param simID
     * @param closeWifi wifi是否要关闭
     * @param delayTime 延时多久后关闭wifi    单位：  秒
     * @return
     */
    public static boolean enableGprs(final Context context, int slotID, boolean closeWifi, int delayTime) {
        boolean isOpened = false;

        if (context == null) {
            return isOpened;
        }
        if (slotID < 0 || slotID > 1) {
            return isOpened;
        }

        //获取simID
        long simID = getSimIDbySlot(context, slotID);

        if (simID < 1) {
            return isOpened;
        }

        try {
            ContentValues values = new ContentValues();
            values.put("value", simID);
            //			values.put("name", StringData.getInstance().GPRS_GEMINI/*"gprs_connection_sim_setting"*/);

            context.getContentResolver().update(Uri.parse(
                    StringData.getInstance().SETTING_SYSTEM + "/" + StringData.getInstance().GPRS_GEMINI), values, null, null);

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            Class<?>[] parameter = new Class[2];
            parameter[0] = int.class;
            parameter[1] = boolean.class;
            Method setRadio = ConnectivityManager.class.getMethod(StringData.getInstance().SET_RADIO/*"setRadio"*/, parameter);

            Object[] obParameter = new Object[2];
            obParameter[0] = TelephonyManager.NETWORK_TYPE_GPRS;
            obParameter[1] = true;
            Object ob_phone = setRadio.invoke(connManager, obParameter);

            if (ob_phone != null) {
//				Logs.d(TAG, "bool = " + ob_phone.toString());
            }

            if (closeWifi) {          //关闭wifi
                //因为setRadio会打开wifi，所以在等待10秒后关闭wifi
                TimeJudge timeJudge = new TimeJudge(delayTime * 1000, new OnActionListener() {
                    public void onActionFinished(int actionCode, int resultCode, Object data) {
//						Logs.d(TAG, "双卡机  -----------close WIFI ------------>");
                        NetControlUtil.getInstance(context).setWifiEnabled(false);
                    }
                }, 0);
                timeJudge.start();              //设置计时器
            }

        } catch (Exception e) {
//			Logs.e(TAG, "enableGprs error" + e.toString());
        }
        return isOpened;
    }

    /**
     * 关闭 gprs
     *
     * @param context
     * @param closeWifi wifi是否要关闭
     * @param delayTime 延时多久后关闭wifi  单位：  秒
     * @return
     */
    public static boolean disableGprs(final Context context, boolean closeWifi, int delayTime) {
        boolean isOpened = false;

        if (context == null) {
            return isOpened;
        }

        try {
            ContentValues values = new ContentValues();
            values.put("value", 0);
            //			values.put("name", StringData.getInstance().GPRS_GEMINI/*"gprs_connection_sim_setting"*/);
            context.getContentResolver().update(Uri.parse(
                    StringData.getInstance().SETTING_SYSTEM + "/" + StringData.getInstance().GPRS_GEMINI), values, null, null);

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            Class<?>[] parameter = new Class[2];
            parameter[0] = int.class;
            parameter[1] = boolean.class;
            Method setRadio = ConnectivityManager.class.getMethod(StringData.getInstance().SET_RADIO/*"setRadio"*/, parameter);

            Object[] obParameter = new Object[2];
            obParameter[0] = TelephonyManager.NETWORK_TYPE_GPRS;
            obParameter[1] = true;
            Object ob_phone = setRadio.invoke(connManager, obParameter);

            if (ob_phone != null) {
//				Logs.d(TAG, "bool = " + ob_phone.toString());
            }

            if (closeWifi) {           //关闭wifi
                //因为setRadio会打开wifi，所以在等待10秒后关闭wifi
                TimeJudge timeJudge = new TimeJudge(delayTime * 1000, new OnActionListener() {
                    public void onActionFinished(int actionCode, int resultCode, Object data) {
                        NetControlUtil.getInstance(context).setWifiEnabled(false);
                    }
                }, 0);
                timeJudge.start();              //设置计时器
            }

        } catch (Exception e) {
//			Logs.e(TAG, "disableGprs error" + e.toString());
        }
        return isOpened;
    }


    /**
     * 获取当前打开的gprs对应的simid
     *
     * @param context
     * @return
     */
    public static long getGprsSimID(Context context) {
        long simID = 0;
        try {
            Cursor cursor = context.getContentResolver().query(Uri.parse(StringData.getInstance().SETTING_SYSTEM), null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if (name != null && name.equals(StringData.getInstance().GPRS_GEMINI)) {
                        String sim = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                        if (sim != null && !sim.equals("")) {
                            simID = Long.parseLong(sim);
                            break;
                        }
                    }
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getGprsSimID error" + e.toString());
        }

        return simID;
    }

    /**
     * 根据卡槽ID 获取对应sim卡信息    0：卡1  1：卡2
     *
     * @param ctx
     * @param slotID
     * @return The SIM-Info, maybe null
     */
    public static SimCardInfo getSimInfoBySlot(Context context, int slotID) {
        if (context == null || slotID < 0) {
            return null;
        }

        try {
            Cursor cursor = context.getContentResolver().query(SimInfo.CONTENT_URI,
                    null, SimInfo.SLOT + "=?", new String[]{String.valueOf(slotID)}, null);
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return fromCursor(cursor);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, "getSimInfoBySlot error" + e.toString());
        }
        return null;
    }

    /**
     * 获取sim卡信息
     *
     * @param cursor
     * @return
     */
    public static SimCardInfo fromCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        SimCardInfo info = new SimCardInfo();
        try {
            info.mSimId = cursor.getLong(cursor.getColumnIndexOrThrow(SimInfo._ID));
            info.mICCId = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.ICC_ID));
            info.mDisplayName = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.DISPLAY_NAME));
            info.mNumber = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.NUMBER));
            info.mDispalyNumberFormat = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.DISPLAY_NUMBER_FORMAT));
            info.mColor = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.COLOR));
            info.mDataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.DATA_ROAMING));
            info.mSlot = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.SLOT));
        } catch (Exception e) {
//			Logs.e(TAG, "fromCursor error" + e.toString());
        }
        return info;
    }


    public final static class SimInfo implements BaseColumns {
        public final static Uri CONTENT_URI = Uri.parse("content://telephony/siminfo");

        public final static String DEFAULT_SORT_ORDER = "name ASC";
        public final static String ICC_ID = "icc_id";
        public final static String DISPLAY_NAME = "display_name";
        public final static int DEFAULT_NAME_MIN_INDEX = 01;
        public final static int DEFAULT_NAME_MAX_INDEX = 99;
        public final static String NUMBER = "number";

        /**
         * 0:none, 1:the first four digits, 2:the last four digits.
         */
        public final static String DISPLAY_NUMBER_FORMAT = "display_number_format";
        public final static int DISPALY_NUMBER_NONE = 0;
        public final static int DISPLAY_NUMBER_FIRST = 1;
        public final static int DISPLAY_NUMBER_LAST = 2;
        public final static int DISLPAY_NUMBER_DEFAULT = DISPLAY_NUMBER_FIRST;

        /**
         * Eight kinds of colors. 0-7 will represent the eight colors.
         * Default value: any color that is not in-use.
         */
        public final static String COLOR = "color";
        public final static int COLOR_1 = 0;
        public final static int COLOR_2 = 1;
        public final static int COLOR_3 = 2;
        public final static int COLOR_4 = 3;
        public final static int COLOR_5 = 4;
        public final static int COLOR_6 = 5;
        public final static int COLOR_7 = 6;
        public final static int COLOR_8 = 7;
        public final static int COLOR_DEFAULT = COLOR_1;

        /**
         * 0: Don't allow data when roaming, 1:Allow data when roaming
         */
        public final static String DATA_ROAMING = "data_roaming";
        public final static int DATA_ROAMING_ENABLE = 1;
        public final static int DATA_ROAMING_DISABLE = 0;
        public final static int DATA_ROAMING_DEFAULT = DATA_ROAMING_DISABLE;


        public final static String SLOT = "slot";
        public final static int SLOT_NONE = -1;

        public final static int ERROR_GENERAL = -1;
        public final static int ERROR_NAME_EXIST = -2;

    }

}
