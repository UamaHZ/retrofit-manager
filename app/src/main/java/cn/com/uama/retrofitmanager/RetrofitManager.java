package cn.com.uama.retrofitmanager;

import android.util.Log;

import com.uama.retrofit.converter.gson.LMGsonConverterFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

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

    private static Retrofit retrofit;

    public static void init(RetrofitProvider provider) {
        if (retrofit != null) {
            Log.w(TAG, "RetrofitManager already initialized!");
            return;
        }
        retrofit = new Retrofit.Builder()
                .baseUrl(provider.provideBaseUrl())
                .addConverterFactory(LMGsonConverterFactory.create(BaseResp.class))
                .client(buildClient(provider.provideOkhttpConfig()))
                .build();
    }

    /**
     * 创建配置好的 OkhttpClient
     */
    private static OkHttpClient buildClient(OkhttpConfiguration config) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        // 日志拦截器拦截所有返回数据
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // 如果是 debug 就 log 所有数据，否则不 log
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        clientBuilder.addInterceptor(loggingInterceptor);
        if (config != null) {
            List<Interceptor> interceptors = config.interceptors();
            // 添加提供的拦截器
            for (int i = 0; interceptors != null && i < interceptors.size(); i++) {
                interceptors.get(i);
                clientBuilder.addInterceptor(interceptors.get(i));
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
