package cn.com.uama.retrofitmanager.sample;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liwei on 2017/10/20.
 */

public class CommonHeadersInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder newRequestBuilder = originalRequest.newBuilder();

        // 在这里加上公共参数
//        newRequestBuilder.addHeader("", "");

        return chain.proceed(newRequestBuilder.build());
    }
}
