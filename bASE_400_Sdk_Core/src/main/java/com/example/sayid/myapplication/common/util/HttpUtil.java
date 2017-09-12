package com.example.sayid.myapplication.common.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.example.sayid.myapplication.common.data.StringData;
import com.example.sayid.myapplication.pay.AppTache;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRoutePNames;

public class HttpUtil {
    private final static String TAG = "HttpUtil";


    /**
     * 根据url获取主机地址
     *
     * @param url
     * @return
     */
    public static String getHost(String url) {
        String host = null;
        if (url == null || url.equals("")) {
            return host;
        }

        int index = url.indexOf("http://");
        if (index < 0) {
            return host;
        }

        String sub = url.substring(7);
        index = sub.indexOf("/");
        if (index < 0) {
            return host;
        }

        host = url.substring(0, index + 7);
        return host;
    }

    /**
     * 根据location获取url
     *
     * @param befUrl
     * @param location
     * @return
     */
    public static String getLocationUrl(String befUrl, String location) {
        String url = location;
        try {
            if (!location.startsWith("http")) {
                int index = befUrl.indexOf("://");
                String head = befUrl.substring(0, index + 3);
                String Strbuf = befUrl.substring(index + 3);
                index = Strbuf.indexOf("/");
                Strbuf = Strbuf.substring(0, index);
                url = head + Strbuf + location;
            }

            url = url.replaceAll("%2CUNTRUSTED", "");
            url = url.replaceAll(" ", "+");
        } catch (Exception e) {
//			Logs.e(TAG, "getLocationUrl error:" + e.toString());
        }

        return url;
    }

    /**
     * 判断网络，设置代理：get
     *
     * @param httpRequest
     */
    public static void judgeNet(HttpRequest httpRequest) {
        judgeNet(AppTache.context, httpRequest);
    }

    /**
     * 判断网络，设置代理：get
     *
     * @param context
     * @param httpRequest
     */
    public static void judgeNet(Context context, HttpRequest httpRequest) {
        if (context == null || httpRequest == null) {
            return;
        }

        try {
            String uriString = StringData.getInstance().APN;
            // 双卡双待
            if (MtkDoubleSimUtil.isGemini(context)) {
                // 当前使用的的gprs的simID
                long simID = MtkDoubleSimUtil.getGprsSimID(context);
                int slotID = MtkDoubleSimUtil.getSlotbySimID(context, simID);
//				Logs.d(TAG, "judgeNet-->slotID = " + slotID);
                // 卡2
                if (slotID == 1) {
                    uriString = StringData.getInstance().APN_GEMINI;
                }
            }

//			Logs.d(TAG, "judgeNet-->uriString = " + uriString);
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            boolean isWifiEnabled = wifiManager.isWifiEnabled();
//			Logs.d(TAG, "judgeNet-->isWifiEnabled = " + isWifiEnabled);

            // 设置代理
            if (!isWifiEnabled) {
                Uri uri = Uri.parse(uriString);
//				Logs.d(TAG, "judgeNet-->uri = " + uri);
                Cursor mCursor = context.getContentResolver().query(uri, null, null, null, null);
                if (null != mCursor) {
//					Logs.d(TAG, "judgeNet-->mCursorCount = " + mCursor.getCount());

                    mCursor.moveToNext();
                    String proxyStr = mCursor.getString(mCursor.getColumnIndex(StringData.getInstance().PROXY));
//					Logs.d(TAG, "judgeNet-->proxyStr = " + proxyStr);
                    // 如果网关不为空，则使用cmwap
                    if (!TextUtils.isEmpty(proxyStr)) {
                        HttpHost proxy = new HttpHost(proxyStr, 80);
                        httpRequest.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    }
                    mCursor.close();
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, "judgeNet error:" + e.toString());
        }
    }
}