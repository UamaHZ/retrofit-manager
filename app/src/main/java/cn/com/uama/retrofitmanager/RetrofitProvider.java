package cn.com.uama.retrofitmanager;

import java.util.List;

import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/4/18 10:23
 * Email: liwei@uama.com.cn
 * Description: Retrofit 的一些相关配置项
 */

public interface RetrofitProvider {
    String provideBaseUrl();
    List<Interceptor> provideInterceptors();
}
