package com.example.sayid.myapplication.parseStep.http;

import android.text.TextUtils;

import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.listener.OnNetListener;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.HttpUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.parseStep.ParseStepBean;
import com.example.sayid.myapplication.parseStep.step.MyCookie;
import com.example.sayid.myapplication.parseStep.step.MyHeader;
import com.example.sayid.myapplication.parseStep.step.action.DownLoad;
import com.example.sayid.myapplication.parseStep.step.request.Param;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class DownLoadThread {

    private final static String TAG = "DownLT";

    int relinkTimes = 0; // 重连次数
    int requestTimes = 1; // 请求次数,防止第一次请求被移动网关劫持

    DefaultHttpClient httpClient;
    HttpRequestBase httpRequest;

    public void startThread(ParseStepBean psb, DownLoad downLoad, OnNetListener listener) {
        updataLoad(psb, downLoad, listener);
    }

    private void updataLoad(ParseStepBean psb, DownLoad downLoad, OnNetListener listener) {
        try {
            relinkTimes++;
            String url = StringUtil.replaceMapValue(psb.map, downLoad.url);
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

            //设置cookie
            if (downLoad.cookies != null && downLoad.cookies.size() > 0) {
                CookieStore cookieStore = new BasicCookieStore();
                for (MyCookie cookie : downLoad.cookies) {
                    BasicClientCookie bcCookie = new BasicClientCookie(cookie.name, StringUtil.replaceMapValue(psb.map, cookie.value));
                    cookieStore.addCookie(bcCookie);
                }
                httpClient.setCookieStore(cookieStore);
            }

            if ("GET".equals(downLoad.method.toUpperCase())) { // GET
                String request_url = url;
                if (downLoad.params != null) { // 添加参数
                    StringBuffer sb = new StringBuffer();
                    sb.append(url);
                    sb.append("?");
                    for (Param params : downLoad.params) {
                        sb.append(params.name);
                        sb.append("=");
                        sb.append(StringUtil.replaceMapValue(psb.map, params.value));
                        sb.append("&");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    request_url = sb.toString();
                }
                httpRequest = new HttpGet(request_url);

                HttpUtil.judgeNet(httpRequest);

                // 设置header
                setRequestHeaders(psb, downLoad, httpRequest);

                //.d(TAG, "download_get_url=" + request_url);
                // 发送请求
                httpResponse = httpClient.execute(httpRequest);

            } else { // POST
                //.d(TAG, "download_post_url=" + url);
                httpRequest = new HttpPost(url);

                HttpUtil.judgeNet(httpRequest);

                //设置header
                setRequestHeaders(psb, downLoad, httpRequest);

                //根据参数拼接内容
                if (downLoad.params != null) {
                    StringBuffer sb = new StringBuffer();
                    for (Param params : downLoad.params) {
                        sb.append(params.name);
                        sb.append("=");
                        sb.append(StringUtil.replaceMapValue(psb.map, params.value));
                        sb.append("&");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    String content = sb.toString();

                    //.d(TAG, "download_post_content=" + content);

                    ((HttpPost) httpRequest).setEntity(new StringEntity(content, HTTP.UTF_8));
                }

                // 发送请求
                httpResponse = httpClient.execute(httpRequest);
            }

            //-----------------------------响应--------------------------------->
            int rc = httpResponse.getStatusLine().getStatusCode();
            if (rc != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP response code: " + rc);
            }

            HttpEntity entity = httpResponse.getEntity();  // 得到应答的字符串，这也是一个 JSON 格式保存的数据
            if (relinkTimes < requestTimes) {
                reconnect(psb, downLoad, listener);
            } else {
                if (entity != null) {
                    // 从http entity对象中得到内容，以输入输出流的形式
                    byte[] data = toByteArray(entity, downLoad.size, downLoad.time);
                    if (listener != null) {
                        if (data != null) {
                            listener.onSuccess(null, false);
                        } else {
                            listener.onFailed(ErrorCode.CODE_111009, "");
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onFailed(ErrorCode.CODE_111010, "");
                    }
                    //.d(TAG, "result = null");
                }
            }
        } catch (Exception e) {
            //.e(TAG, "DownLoadThread：001:" + e.toString());
            if (relinkTimes < ConfigConst.WAP_RETRY_TIMES) {
                reconnect(psb, downLoad, listener);
            } else {
                relinkTimes = 0;
                connectFailed(listener, ErrorCode.CODE_111003, e.toString());
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
    private void setRequestHeaders(ParseStepBean psb, DownLoad downLoad, HttpRequestBase httpReq) {
        if (downLoad.headers != null && downLoad.headers.size() > 0) {
            boolean isUa = false;
            for (MyHeader header : downLoad.headers) {
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
        }
    }

    /**
     * 自动重连
     */
    private void reconnect(ParseStepBean psb, DownLoad downLoad, OnNetListener listener) {
        closeConnect();
        updataLoad(psb, downLoad, listener);
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
            //.e(TAG, "DownLoadThread：003:" + e.toString());
        }
    }

    private void connectFailed(OnNetListener listener, String errorTip, String exceptionStr) {
        if (listener != null) {
            listener.onFailed(errorTip, exceptionStr);
        }
    }

    /**
     * 下载指定大小的内容
     *
     * @param entity
     * @param downloadSize
     * @param timeCount
     * @return
     * @throws IOException
     */
    private byte[] toByteArray(HttpEntity entity, int downloadSize, long timeCount) throws IOException {
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }

        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            long startTime = System.currentTimeMillis();
            if (downloadSize > 0) {
                downloadSize = downloadSize * 1024;
            } else {
                downloadSize = (int) entity.getContentLength();
            }
            if (downloadSize < 0) {
                downloadSize = 4096;
            }
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                outSteam.write(tmp, 0, l);

                // 下载内容 >= 设置大小
                if (outSteam.toByteArray().length >= downloadSize) {
                    break;
                }
            }

            //.d(TAG, "download finished = " + outSteam.toByteArray().length);

            // 时间
            long endTime = System.currentTimeMillis();
            if (timeCount > 0) {
                long offTime = endTime - startTime - (timeCount * 1000);
                if (offTime < 0) {
                    try {
                        Thread.sleep(-offTime);
                    } catch (InterruptedException e) {
                        //.e(TAG, "sleep error=" + e);
                    }
                }
            }

            outSteam.close();
            return outSteam.toByteArray();
        } finally {
            instream.close();
        }
    }

}
