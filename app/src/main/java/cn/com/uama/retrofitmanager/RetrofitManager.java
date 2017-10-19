package cn.com.uama.retrofitmanager;

import android.util.Log;

import com.uama.retrofit.converter.gson.LMGsonConverterFactory;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by liwei on 2017/4/12 10:56
 * Email: liwei@uama.com.cn
 * Description: retrofit 配置管理类
 */

public class RetrofitManager {

    private static final String TAG = "RetrofitManager";

    static final int DEFAULT_CONNECT_TIMEOUT = 30;
    static final int DEFAULT_READ_TIMEOUT = 30;
    static final int DEFAULT_WRITE_TIMEOUT = 30;

    static ApiStatusInterceptor apiStatusInterceptor;

    private static Retrofit retrofit;
    public static void init(RetrofitProvider provider) {
        if (retrofit != null) {
            Log.w(TAG, "RetrofitManager already initialized!");
            return;
        }
        if (provider == null) {
            throw new IllegalArgumentException("RetrofitProvider must NOT be null!");
        }
        apiStatusInterceptor = provider.provideApiStatusInterceptor();
        retrofit = new Retrofit.Builder()
                .baseUrl(provider.provideBaseUrl())
                .addConverterFactory(LMGsonConverterFactory.create(BaseResp.class))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(buildClient(provider.provideOkHttpConfig()))
                .build();
    }

    /**
     * 创建配置好的 OkhttpClient
     */
    private static OkHttpClient buildClient(OkHttpConfiguration config) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (config != null) {
            List<Interceptor> interceptors = config.interceptors();
            if (interceptors != null) {
                clientBuilder.interceptors().addAll(interceptors);
            }
            // 连接超时
            int connectTimeoutSeconds = config.connectTimeoutSeconds();
            if (!isTimeoutValueValid(connectTimeoutSeconds)) {
                throw new RuntimeException("connect timeout must be between 0 and Integer.MAX_VALUE in milliseconds.");
            }
            clientBuilder.connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS);
            // 读取超时
            int readTimeoutSeconds = config.readTimeoutSeconds();
            if (!isTimeoutValueValid(readTimeoutSeconds)) {
                throw new RuntimeException("read timeout must be between 0 and Integer.MAX_VALUE in milliseconds.");
            }
            clientBuilder.readTimeout(readTimeoutSeconds, TimeUnit.SECONDS);
            // 写入超时
            int writeTimeoutSeconds = config.writeTimeoutSeconds();
            if (!isTimeoutValueValid(writeTimeoutSeconds)) {
                throw new RuntimeException("write timeout must be between 0 and Integer.MAX_VALUE in milliseconds.");
            }
            clientBuilder.writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS);

            X509TrustManager trustManager = config.trustManager();
            if (trustManager != null) {
                // 配置 https 证书
                SSLSocketFactory sslSocketFactory;
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, new TrustManager[] { trustManager }, null);
                    sslSocketFactory = sslContext.getSocketFactory();
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
            }
        }
        return clientBuilder.build();
    }

    /**
     * 超时值是否在合理范围内
     */
    private static boolean isTimeoutValueValid(int timeout) {
        return timeout * 1000 >=0 && timeout * 1000 <= Integer.MAX_VALUE;
    }

    /**
     * 根据指定的 Service.Class ，得到一个被代理后的 Service
     */
    public static <T> T createService(Class<T> service) {
        if (retrofit == null) {
            throw new IllegalStateException("RetrofitManager not initialized! Call RetrofitManager.init() in your custom application class!");
        }
        return retrofit.create(service);
    }
}
