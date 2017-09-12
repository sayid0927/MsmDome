package com.example.sayid.myapplication.http;

import com.example.sayid.myapplication.common.data.ConfigConst;
import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.common.listener.OnNetListener;
import com.example.sayid.myapplication.common.util.HttpUtil;
import com.example.sayid.myapplication.common.util.ParseKsy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpJsonThread implements Runnable {
    private final static String TAG = "HttpJsonThread";

    boolean secret = true;
    int relinkTimes = 0; // 计数重连次数
    HttpPost httpPost;
    DefaultHttpClient httpClient;

    String url;
    String content;
    OnNetListener onNetListener;

    public void startThread(String url, String content, OnNetListener onNetListener) {
        this.url = url;
        this.content = content;
        this.onNetListener = onNetListener;
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        uploadData(url);
    }

    private int uploadData(String url) {
        try {
            relinkTimes++;
//			Logs.d(TAG, "url:" + url);

            httpPost = new HttpPost(url);
            HttpUtil.judgeNet(httpPost);

            // 请求内容
            String msg = "";
            if (content != null) {
//				Logs.d(TAG, " src post request:" + content);
                // 加密
                if (secret) {
                    msg = ParseKsy.encode(content);
//					Logs.d(TAG, " encode post request:" + msg);
                }
            }

            httpPost.setEntity(new StringEntity(msg, HTTP.UTF_8));
            httpClient = new DefaultHttpClient();
            // 请求超时
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, ConfigConst.CLIENT_TIMEOUT * 1000);
            // 读取超时
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, ConfigConst.CLIENT_SO_TIMEOUT * 1000);
            // 定义重试策略
//			httpClient.setHttpRequestRetryHandler(requestRetryHandler);

            // 发送请求
            HttpResponse httpResponse = httpClient.execute(httpPost);

            int rc = httpResponse.getStatusLine().getStatusCode();
//			Logs.d(TAG, "result code = " + rc);
            if (rc != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP response code: " + rc);
            }

            // 得到应答的字符串，这也是一个 JSON 格式保存的数据
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                if (onNetListener != null) {
                    onNetListener.onSuccess(entity, secret);
                }

                // 通用计费请求成功,返回0
                return 0;
            }
        } catch (Exception e) {
            //.e(TAG, e, "request error");

            if (relinkTimes < ConfigConst.RETRY_TIMES) {
                reconnect(url);
            } else {
                relinkTimes = 0;

                if (e instanceof HttpHostConnectException) {
                    connectFailed(ErrorCode.CODE_111403, ErrorCode.errorMsg.get(ErrorCode.CODE_111403));
                } else if (e instanceof ConnectTimeoutException) {
                    connectTimeOut();
                } else if (e instanceof IOException) {
                    connectTimeOut();
                } else {
                    connectTimeOut();
                }
            }
        } catch (Throwable e) {
            //.e(TAG, e, "---Throwable-----请求挂了----");
            connectFailed(ErrorCode.CODE_119999, ErrorCode.errorMsg.get(ErrorCode.CODE_119999));
        } finally {
            closeConnect();
        }
        return 1;
    }

    /**
     * 重新连接，并请求
     *
     * @param url
     * @return
     */
    private int reconnect(String url) {
        closeConnect();
        return uploadData(url);
    }

    /**
     * 关闭连接
     */
    private void closeConnect() {
        try {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
                httpClient = null;
            }

            if (httpPost != null) {
                if (!httpPost.isAborted()) {
                    httpPost.abort();
                }
                httpPost = null;
            }
        } catch (Exception e) {
            //.e(TAG, e, "closeConnect error:");
        }
    }

    /**
     * 失败
     *
     * @param errorTip
     * @param exceptionStr
     */
    private void connectFailed(String errorTip, String exceptionStr) {
        if (onNetListener != null) {
            onNetListener.onFailed(errorTip, exceptionStr);
        }
    }

    /**
     * 超时
     */
    private void connectTimeOut() {
        if (onNetListener != null) {
            onNetListener.onTimeout();
        }
    }
}