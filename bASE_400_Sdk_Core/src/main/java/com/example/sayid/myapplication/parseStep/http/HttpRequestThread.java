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
import com.example.sayid.myapplication.parseStep.step.action.MyRequest;
import com.example.sayid.myapplication.parseStep.step.request.Param;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpRequestThread {

    /**
     * 保存第一次获取验证码时的Cookie信息
     */
    static volatile CookieStore test_cookieStore = null;

    int relinkTimes = 0; // 重连次数
    int requestTimes = 1; // 请求次数,防止第一次请求被移动网关劫持

    DefaultHttpClient httpClient;
    HttpRequestBase httpRequest;

    public void startThread(ParseStepBean psb, MyRequest request, OnNetListener listener) {
        updataLoad(psb, request, listener);
    }

    /**
     * 请求连接
     */
    private void updataLoad(ParseStepBean psb, MyRequest request, OnNetListener listener) {
        try {
            psb.append("\n<br>---------------req start---------------").append("\r\n");
            relinkTimes++;

            String url = StringUtil.replaceMapValue(psb.map, request.url);
            url = url.replaceAll("&amp;", "&");

            psb.append("\n<br>URL:").append(url).append("\r\n");
            httpClient = getHttpClient();

            HttpResponse httpResponse = null;
            httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.NETSCAPE);

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

            // 设置cookie
            if (request.cookies != null && request.cookies.size() > 0
                    && test_cookieStore != null) {
                httpClient.setCookieStore(test_cookieStore);
            }
            if (test_cookieStore != null) {
                httpClient.setCookieStore(test_cookieStore);
            }

            psb.append("\n<br>method:").append(request.method).append("\r\n");
            psb.append("\n<br>cookie:").append(httpClient.getCookieStore().toString()).append("\r\n");

            if ("GET".equals(request.method.toUpperCase())) { // GET
                String request_url = url;
                // 添加参数 支持URL编码
                if (request.params != null) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(url);
                    sb.append("?");
                    for (Param params : request.params) {
                        sb.append(params.name);
                        sb.append("=");
                        String paramValue = StringUtil.replaceMapValue(psb.map, params.value);
                        try {
                            sb.append(URLEncoder.encode(paramValue, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            sb.append(URLEncoder.encode(paramValue));
                        }
                        sb.append("&");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    request_url = sb.toString();
                }
                httpRequest = new HttpGet(request_url);

                HttpUtil.judgeNet(httpRequest);

                // 设置请求Headers
                setRequestHeaders(psb, request, httpRequest);

                psb.append("\n<br>request_get_url=" + request_url);
                // 发送请求
                httpResponse = httpClient.execute(httpRequest);

            } else { // POST
                psb.append("\n<br>request_post_url=" + url);

                httpRequest = new HttpPost(url);

                HttpUtil.judgeNet(httpRequest);

                // 设置请求Headers
                setRequestHeaders(psb, request, httpRequest);

                if (request.bodytype == 2) {
                    if (request.content != null && psb.map_object != null) {
                        StringBuffer arraySize = new StringBuffer();

                        String[] keys = request.content.split("\\|");
                        List<byte[]> body = new ArrayList<byte[]>();
                        for (String key : keys) {
                            byte[] reqbody = (byte[]) psb.map_object.get(key);
                            if (reqbody != null) {
                                arraySize.append(reqbody.length).append(",");

                                body.add(reqbody);
                            }
                        }
                        arraySize.deleteCharAt(arraySize.length() - 1);

                        httpRequest.setHeader("Content-ArraySize", arraySize.toString());
                        ((HttpPost) httpRequest).setEntity(new ByteArrayEntity(StringUtil.concatAll(body)));
                    }
                } else {
                    // 替换变量生成内容
                    String content = StringUtil.replaceMapValue(psb.map, request.content);

                    // 根据参数拼接内容
                    if (request.params != null) {
                        if ("json".equals(request.contentFormat)) {
                            JSONObject jo = new JSONObject();
                            for (Param params : request.params) {
                                jo.put(params.name, StringUtil.replaceMapValue(psb.map, params.value));
                            }
                            content = jo.toString();
                        } else {
                            StringBuffer sb = new StringBuffer();
                            for (Param params : request.params) {
                                sb.append(params.name);
                                sb.append("=");
                                sb.append(StringUtil.replaceMapValue(psb.map, params.value));
                                sb.append("&");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            content = sb.toString();
                        }
                    }

                    if (content != null) {
                        psb.append("\n<br>request_post_content=" + content);

                        ((HttpPost) httpRequest).setEntity(new StringEntity(content, HTTP.UTF_8));
                    }
                }

                // 发送请求
                httpResponse = httpClient.execute(httpRequest);
            }

            // -----------------------------响应--------------------------------->
            int rc = httpResponse.getStatusLine().getStatusCode();
            if (rc != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP response code: " + rc);
            }

            //获取header
            if (request.response.headers != null) {
                for (MyHeader header : request.response.headers) {
                    header.value = httpResponse.getFirstHeader(header.name).toString();
                    psb.map.put(header.variable, header.value);
                }
            }

            // 保存cookie
            test_cookieStore = httpClient.getCookieStore();

            // 得到应答数据
            HttpEntity entity = httpResponse.getEntity();
            if (relinkTimes < requestTimes) {
                reconnect(psb, request, listener);
            } else {
                if (listener != null) {
                    if (entity != null) {
                        listener.onSuccess(entity, false);
                    } else {
                        listener.onFailed(ErrorCode.CODE_111012, "");
                    }
                }
            }
        } catch (Exception e) {
            psb.append("\n<br>HttpRequestThread 001 Error:" + e.toString());

            if (relinkTimes < ConfigConst.WAP_RETRY_TIMES) {
                reconnect(psb, request, listener);
            } else {
                relinkTimes = 0;

                if (e instanceof ConnectTimeoutException) {
                    connectTimeOut(listener);
                } else if (e instanceof HttpHostConnectException) {
                    connectFailed(listener, ErrorCode.CODE_111403, e.toString());
                } else if (e instanceof IOException) {
                    connectFailed(listener, ErrorCode.CODE_111404, e.toString());
                } else {
                    connectFailed(listener, ErrorCode.CODE_111007, e.toString());
                }
            }
        } finally {
            closeConnect(psb);
        }
    }

    /**
     * 设置请求Header
     *
     * @param httpReq
     */
    private void setRequestHeaders(ParseStepBean psb, MyRequest request, HttpRequestBase httpReq) {
        if (request.headers != null && request.headers.size() > 0) {
            boolean isUa = false;
            for (MyHeader header : request.headers) {
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

        psb.append("\n<br>all header:" + Arrays.toString(httpReq.getAllHeaders()));
    }


    /**
     * 自动重连
     */
    private void reconnect(ParseStepBean psb, MyRequest request, OnNetListener listener) {
        closeConnect(psb);
        updataLoad(psb, request, listener);
    }

    /**
     * 关闭连接
     */
    private void closeConnect(ParseStepBean psb) {
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
            psb.append("\n<br>HttpRequestThread 002 Error:" + e.toString());
        }
    }

    private void connectFailed(OnNetListener listener, String errorTip, String exceptionStr) {
        if (listener != null) {
            listener.onFailed(errorTip, exceptionStr);
        }
    }

    private void connectTimeOut(OnNetListener listener) {
        if (listener != null) {
            listener.onTimeout();
        }
    }


    public synchronized DefaultHttpClient getHttpClient() {
        if (null == httpClient) {
            // 初始化工作
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  //允许所有主机的验证

                HttpParams params = new BasicHttpParams();

                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
                HttpProtocolParams.setUseExpectContinue(params, true);

                // 设置连接管理器的超时
                ConnManagerParams.setTimeout(params, ConfigConst.WAP_CLIENT_TIMEOUT * 1000);
                // 设置连接超时
                HttpConnectionParams.setConnectionTimeout(params, ConfigConst.WAP_CLIENT_TIMEOUT * 1000);
                // 设置socket超时
                HttpConnectionParams.setSoTimeout(params, ConfigConst.WAP_CLIENT_SO_TIMEOUT * 1000);

                // 设置http https支持
                SchemeRegistry schReg = new SchemeRegistry();
                schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                schReg.register(new Scheme("https", sf, 443));

                ClientConnectionManager conManager = new ThreadSafeClientConnManager(params, schReg);

                httpClient = new DefaultHttpClient(conManager, params);
            } catch (Exception e) {
                return new DefaultHttpClient();
            }
        }
        return httpClient;
    }

}

class SSLSocketFactoryEx extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public SSLSocketFactoryEx(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }
        };

        sslContext.init(null, new TrustManager[]{tm}, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port,
                               boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}
