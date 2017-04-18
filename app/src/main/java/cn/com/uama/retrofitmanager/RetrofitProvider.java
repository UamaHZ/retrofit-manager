package cn.com.uama.retrofitmanager;

import java.util.List;

import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/4/18 10:23
 * Email: liwei@uama.com.cn
 * Description: Retrofit 的一些相关配置项
 */

public interface RetrofitProvider {
    /**
     * 提供 Retrofit 所需要的 base url
     */
    String provideBaseUrl();

    /**
     * 提供一些需要的 interceptor，设置给 okhttp client
     */
    List<Interceptor> provideInterceptors();
}
