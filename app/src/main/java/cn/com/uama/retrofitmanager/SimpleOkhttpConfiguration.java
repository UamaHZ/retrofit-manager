package cn.com.uama.retrofitmanager;

import java.util.List;

import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/4/19 15:11
 * Email: liwei@uama.com.cn
 * Description: OkhttpConfiguration 的简单实现
 */

public class SimpleOkhttpConfiguration implements OkhttpConfiguration {
    @Override
    public List<Interceptor> interceptors() {
        return null;
    }

    @Override
    public int readTimeoutSeconds() {
        return RetrofitManager.DEFAULT_READ_TIMEOUT;
    }

    @Override
    public int writeTimeoutSeconds() {
        return RetrofitManager.DEFAULT_WRITE_TIMEOUT;
    }

    @Override
    public int connectTimeoutSeconds() {
        return RetrofitManager.DEFAULT_CONNECT_TIMEOUT;
    }

    @Override
    public X509TrustManager trustManager() {
        return null;
    }
}
