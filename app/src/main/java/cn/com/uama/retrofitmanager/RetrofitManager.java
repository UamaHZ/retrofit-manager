package cn.com.uama.retrofitmanager;

import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by liwei on 2017/4/12 10:56
 * Email: liwei@uama.com.cn
 * Description: retrofit 配置管理类
 */

public class RetrofitManager {

    private static final String TAG = "RetrofitManager";

    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 30;

    private static Retrofit retrofit;

    public static void init(RetrofitProvider provider) {
        if (retrofit != null) {
            Log.w(TAG, "RetrofitManager already initialized!");
            return;
        }
        retrofit = new Retrofit.Builder()
                .baseUrl(provider.provideBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(buildClient(provider.provideInterceptors()))
                .build();
    }

    /**
     * 创建配置好的 OkhttpClient
     */
    private static OkHttpClient buildClient(List<Interceptor> interceptors) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        // 添加提供的拦截器
        for (int i = 0; interceptors != null && i < interceptors.size(); i++) {
            interceptors.get(i);
            clientBuilder.addInterceptor(interceptors.get(i));
        }
        // 日志拦截器拦截所有返回数据
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // 如果是 debug 就 log 所有数据，否则不 log
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        clientBuilder.addInterceptor(loggingInterceptor);
        // 连接超时
        clientBuilder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        // 读取超时
        clientBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        // 写入超时
        clientBuilder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        return clientBuilder.build();
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
