package cn.com.uama.retrofitmanager;

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
     * 提供配置项，设置给 okhttp client
     */
    OkHttpConfiguration provideOkHttpConfig();

    /**
     * 提供接口状态拦截器
     */
    ApiStatusInterceptor provideApiStatusInterceptor();

    /**
     * 提供缓存配置
     */
    LMCache provideCache();
}
