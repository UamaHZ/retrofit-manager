package cn.com.uama.retrofitmanager;

import java.util.List;

import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/4/19 15:00
 * Email: liwei@uama.com.cn
 * Description: Okhttp 配置接口
 */

public interface OkHttpConfiguration {
    List<Interceptor> interceptors();
    int readTimeoutSeconds();
    int writeTimeoutSeconds();
    int connectTimeoutSeconds();
    X509TrustManager trustManager();
}
