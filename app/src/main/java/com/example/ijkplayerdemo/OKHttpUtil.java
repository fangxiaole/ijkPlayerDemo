package com.example.ijkplayerdemo;

import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * 说明：该类包含post请求和get请求
 * 请求方式又分为：同步和异步
 * 如果不使用该类中的同步或异步请求方法，可以使用返回Call对象的相关方法，自行构造并实现Callback
 */
public class OKHttpUtil {
    public static final String TAG = OKHttpUtil.class.getSimpleName();

    public static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final MediaType STREAM = MediaType.parse("application/octet-stream");
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final int REQUEST_POST = 1;
    public static final int REQUEST_GET = 2;

    private static OKHttpUtil sOkHttpUtil;
    private static OkHttpClient sOkHttpClient;
    private static Object sObj = new Object();

    private OKHttpUtil() { }

    public static void init(String url) {
        OkHttpClient.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new OkHttpClient().newBuilder();
            builder.connectTimeout(15, TimeUnit.SECONDS)//设置超时时间
                    .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                    .writeTimeout(15, TimeUnit.SECONDS);//设置写入超时时间
//            builder.sslSocketFactory(HttpSSLManager.getSSLHolder().delegate);
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            builder = getHttpClient(url);
            builder.connectTimeout(15, TimeUnit.SECONDS)//设置超时时间
                    .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                    .writeTimeout(15, TimeUnit.SECONDS);//设置写入超时时间
        }
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        sOkHttpClient = builder.build();
    }

    protected static OkHttpClient.Builder getHttpClient(String url) {
        // String hostname = Constants.HOST_NAME_DEBUG;
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(url, "sha1/mBN/TTGneHe2Hq0yFG+SRt5nMZQ=")
                .add(url, "sha1/6CgvsAgBlX3PYiYRGedC0NZw7ys=")
                .build();

        //specifying the specs; this is impotent otherwise android <5 won't work
        //And do note to include the android < 5 supported specs.
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256)
                .build();

        //final OkHttpClient okHttpClient = new OkHttpClient();
        //okHttpClient.setCertificatePinner(certificatePinner);
        //okHttpClient.setConnectionSpecs(Collections.singletonList(spec));
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.certificatePinner(certificatePinner);
        builder.connectionSpecs(Collections.singletonList(spec));
//        try {
//            //enabling the tlsv1.1 and tlsv.2
//            //okHttpClient.setSslSocketFactory(new MyTLSSocketFactory());
//            builder.sslSocketFactory(new MyTLSSocketFactory(), new TrustAllManager());
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
        return builder;
    }

    public static OKHttpUtil getInstance() {
        if (sOkHttpUtil == null) {
            synchronized (sObj) {
                if (sOkHttpUtil == null) {
                    sOkHttpUtil = new OKHttpUtil();
                }
            }
        }
        return sOkHttpUtil;
    }

    /**
     * 将Map转化为Json字符串
     */
    public static <T> String mapToJson(Map<String, T> map) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map);
        return jsonStr;
    }

    public static RequestBody createBody(Map<String, Object> map) {
        Log.i("","json:" + mapToJson(map));
        return RequestBody.create(JSON, mapToJson(map));
    }

    public static RequestBody createBody(String body) {
        return RequestBody.create(JSON, body);
    }

    /**
     * 同步post请求
     */
    public Response PostSync(String url, Map<String, Object> params) {
        try {
            return RequestPost(url, params).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步post请求带参数和body
     */
    public void PostNonSync(String url, Map<String, String> params, Map<String, Object> body, Callback mCallback) {
        try {
            RequestPost(url, params, body).enqueue(mCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步post请求无参带body
     */
    public void PostNonSync(String url, Map<String, Object> body, Callback mCallback) {
        try {
            RequestPost(url, body).enqueue(mCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步post请求无参带body
     */
    public void PostNonSync(String url, String body, Callback mCallback) {
        try {
            RequestPost(url, body).enqueue(mCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * post请求
     * @return
     */
    public okhttp3.Call RequestPost(String url, Map<String, String> params, Map<String, Object> body) {
        if (url == null)
            return null;
        Request request = new Request.Builder().url(getParamUrl(url, params)).post(createBody(body)).build();
        return sOkHttpClient.newCall(request);
    }

    /**
     * post请求无参带body
     * @return
     */
    public okhttp3.Call RequestPost(String url, Map<String, Object> body) {
        if (url == null)
            return null;
        Request request = new Request.Builder().url(url).post(createBody(body)).build();
        return sOkHttpClient.newCall(request);
    }


    public okhttp3.Call RequestPost(String url, String body) {
        if (url == null)
            return null;
        Request request = new Request.Builder().url(url).post(createBody(body)).build();
        Log.e(TAG, "request=" + request.toString());
        return sOkHttpClient.newCall(request);
    }


    /**
     * get请求
     * @return 返回Call对象
     */
    public Call RequestGet(String url) {
        if (url == null) return null;
        Request request = new Request.Builder().url(url).get().build();
        return sOkHttpClient.newCall(request);
    }

    /**
     * 同步get请求
     */
    public Response RequestGetSync(String url) {
        try {
            return RequestGet(url).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步get请求带参数
     */
    public void RequestGetNonSync(String url, Map<String, String> params, Callback mCallback) {
        RequestGet(url, params).enqueue(mCallback);
    }

    /**
     * 异步get请求
     */
    public void RequestGetNonSync(String url, Callback mCallback) {
        RequestGet(url).enqueue(mCallback);
    }

    /**
     * get请求(带参数)
     * @return
     */
    public okhttp3.Call RequestGet(String url, Map<String, String> params) {
        if (url == null) return null;
        Request.Builder builder = new Request.Builder();

        String requestUrl = getParamUrl(url, params);
        Log.i(TAG,"requestUrl:" + requestUrl);
        Log.e("leleTest", "requestUrl=" + requestUrl);
        Request request = builder.url(requestUrl).get().build();
        return sOkHttpClient.newCall(request);
    }

    /**
     * 构造带参数url
     */
    public String getParamUrl(String url, Map<String, String> params) {
        StringBuilder tempParams = new StringBuilder();
        int pos = 0;

        if (params != null)
            for (String key : params.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                try {
                    tempParams.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key), "utf-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                pos++;
            }
        return String.format("%s?%s", url, tempParams.toString());
    }

    public void upload(String url, String imagePath, Callback mCallback) {
        Log.e("TAG", "url=" + url);
        File file = new File(imagePath);
        RequestBody image = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        Request request = new Request.Builder().url(url).put(image).build();
        OkHttpClient okHttpClient = getNoVerificationOkHttpClient();
        if(okHttpClient!=null){
            okHttpClient.newCall(request).enqueue(mCallback);
        }
    }

    private static OkHttpClient getNoVerificationOkHttpClient() {
//        try {
//            TrustAllManager[]  trustAllCerts = new TrustAllManager[]{new TrustAllManager()};
//            final SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            builder.sslSocketFactory(sslSocketFactory);
//            return builder.build();
//        } catch (Exception e) {
//           e.printStackTrace();
//        }
        return null;
    }

}

