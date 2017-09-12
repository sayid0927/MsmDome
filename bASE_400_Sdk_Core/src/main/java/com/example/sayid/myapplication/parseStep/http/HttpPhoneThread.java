package com.example.sayid.myapplication.parseStep.http;

import android.text.TextUtils;

import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.listener.OnNetListener;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.HttpUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.parseStep.ParseStepBean;
import com.example.sayid.myapplication.parseStep.step.MyHeader;
import com.example.sayid.myapplication.parseStep.step.action.Phone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpPhoneThread {

    private final static String TAG = "HttpPhoneThread";

    static CookieStore cookieStore = null; // 保存第一次获取验证码时的Cookie信息

    int relinkTimes = 0; // 重连次数
    int requestTimes = 1; // 请求次数,防止第一次请求被移动网关劫持

    DefaultHttpClient httpClient;
    HttpRequestBase httpRequest;

    public void startThread(ParseStepBean psb, Phone phone, OnNetListener listener) {
        updataLoad(psb, phone, listener);
    }

    private void updataLoad(ParseStepBean psb, Phone phone, OnNetListener listener) {
        try {
            relinkTimes++;
            String url = StringUtil.replaceMapValue(psb.map, phone.url);
            url = url.replaceAll("&amp;", "&");

            httpClient = new DefaultHttpClient();
            //请求超时
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, ConfigConst.WAP_CLIENT_TIMEOUT * 1000);
            // 读取超时
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, ConfigConst.WAP_CLIENT_SO_TIMEOUT * 1000);
            HttpResponse httpResponse = null;

            // 301,302重定向
            httpClient.setRedirectHandler(new DefaultRedirectHandler() {
                @Override
                public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                    boolean isRedirect = super.isRedirectRequested(response, context);
                    if (!isRedirect) {
                        int responseCode = response.getStatusLine().getStatusCode();
                        if (responseCode == 301 || responseCode == 302) {
                            relinkTimes--;
                            requestTimes = 1;

                            return true;
                        }
                    }
                    return isRedirect;
                }
            });

            if ("GET".equals(phone.method.toUpperCase())) { // get
                httpRequest = new HttpGet(url);

                HttpUtil.judgeNet(httpRequest);

                // 设置header
                setRequestHeaders(psb, phone, httpRequest);

//				Logs.d(TAG, "request_get_url=" + url);

                // 发送请求
                httpResponse = httpClient.execute(httpRequest);
            } else {
//				Logs.d(TAG, "request_post_url=" + url);

                httpRequest = new HttpPost(url);

                HttpUtil.judgeNet(httpRequest);

                // 设置header
                setRequestHeaders(psb, phone, httpRequest);

//				Logs.d(TAG, "request_post_content=" + phone.content);

                ((HttpPost) httpRequest).setEntity(new StringEntity(phone.content, HTTP.UTF_8));

                // 发送请求
                httpResponse = httpClient.execute(httpRequest);
            }

            //-----------------------------响应--------------------------------->
            int rc = httpResponse.getStatusLine().getStatusCode();
            if (rc != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP response code: " + rc);
            }

            // 得到应答的数据
            HttpEntity entity = httpResponse.getEntity();
            if (relinkTimes < requestTimes) {
                reconnect(psb, phone, listener);
            } else {
                if (listener != null) {
                    if (entity != null) {
                        listener.onSuccess(entity, false);
                    } else {
                        listener.onFailed(ErrorCode.CODE_111011, "");
                    }
                }
            }
        } catch (Exception e) {
//			Logs.e(TAG, "HttpPhoneThread：002:" + e.toString());

            if (relinkTimes < ConfigConst.WAP_RETRY_TIMES) {
                reconnect(psb, phone, listener);
            } else {
                relinkTimes = 0;
                connectFailed(listener, ErrorCode.CODE_111005, e.toString());
            }
        } finally {
            closeConnect();
        }
    }

    /**
     * 设置请求Header
     *
     * @param httpReq
     */
    private void setRequestHeaders(ParseStepBean psb, Phone phone, HttpRequestBase httpReq) {
        if (phone.request_headers != null && phone.request_headers.size() > 0) {
            boolean isUa = false;
            for (MyHeader header : phone.request_headers) {
                httpReq.setHeader(header.name, StringUtil.replaceMapValue(psb.map, header.value));
                if (header.name.toUpperCase().equals("USER-AGENT")) {
                    isUa = true;
                }
            }

            // 没有设置UA，设置系统UA
            if (!isUa) {
                String ua = CacheUtil.getInstance().getString("ua", null);
                if (!TextUtils.isEmpty(ua)) {
                    httpReq.setHeader("User-Agent", ua);
                }
            }
        } else {
            String ua = CacheUtil.getInstance().getString("ua", null);
            if (!TextUtils.isEmpty(ua)) {
                httpReq.setHeader("User-Agent", ua);
            }
            httpReq.setHeader("Content-Type", "text/html");
            httpReq.setHeader("Accept-Charset", "iso-8859-1, utf-8; q=0.7, *; q=0.7");
            httpReq.setHeader("Accept-Language", "zh-cn, zh;q=1.0,en;q=0.5");
            httpReq.setHeader("Accept", "*/*");
        }
    }

    /**
     * 自动重连
     */
    private void reconnect(ParseStepBean psb, Phone phone, OnNetListener listener) {
        closeConnect();
        updataLoad(psb, phone, listener);
    }

    /**
     * 关闭连接
     */
    private void closeConnect() {
        try {
            if (httpRequest != null) {
                if (!httpRequest.isAborted()) {
                    httpRequest.abort();
                }
                httpRequest = null;
            }

            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
                httpClient = null;
            }
        } catch (Exception e) {
//			Logs.e(TAG, "HttpPhoneThread：003:" + e.toString());
        }
    }

    private void connectFailed(OnNetListener listener, String errorTip, String exceptionStr) {
        if (listener != null) {
            listener.onFailed(errorTip, exceptionStr);
        }
    }

}
