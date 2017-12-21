package cn.com.uama.retrofitmanager.sample;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import cn.com.uama.retrofitmanager.ApiStatusInterceptor;
import cn.com.uama.retrofitmanager.ErrorStatus;
import cn.com.uama.retrofitmanager.HttpsHelper;
import cn.com.uama.retrofitmanager.OkHttpConfiguration;
import cn.com.uama.retrofitmanager.RetrofitManager;
import cn.com.uama.retrofitmanager.RetrofitProvider;
import cn.com.uama.retrofitmanager.SimpleOkHttpConfiguration;
import cn.com.uama.retrofitmanager.bean.BaseResp;
import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/10/20.
 */

public class SampleApplication extends Application implements RetrofitProvider {

    @Override
    public void onCreate() {
        super.onCreate();

        RetrofitManager.init(this);
    }

    @Override
    public String provideBaseUrl() {
        // 接口的基础路径
        return "http://192.168.20.200:7031";
    }

    @Override
    public OkHttpConfiguration provideOkHttpConfig() {
        return new SimpleOkHttpConfiguration() {
            @Override
            public List<Interceptor> interceptors() {
                // 自定义拦截器列表
                return Collections.<Interceptor>singletonList(new CommonHeadersInterceptor());
            }

            @Override
            public int readTimeoutSeconds() {
                // 自定义读取超时时间，单位为秒。默认30秒。
                return super.readTimeoutSeconds();
            }

            @Override
            public int writeTimeoutSeconds() {
                // 默认写入超时时间，单位为秒。默认30秒。
                return super.writeTimeoutSeconds();
            }

            @Override
            public int connectTimeoutSeconds() {
                // 默认连接超时时间，单位为秒。默认30秒。
                return super.connectTimeoutSeconds();
            }

            @Override
            public X509TrustManager trustManager() {
                // 配置 HTTPS 的证书
                 return HttpsHelper.getTrustManager(getApplicationContext());
            }
        };
    }

    @Override
    public ApiStatusInterceptor provideApiStatusInterceptor() {
        return new ApiStatusInterceptor() {
            @Override
            public boolean intercept(BaseResp resp) {
                String status = resp.getStatus();
                // 在这里对某种 status 进行统一处理
                if ("102".equals(status)) {
                    // 处理逻辑
                    Log.d("ApiStatusInterceptor", "intercept: " + status);

                    // 如果不想数据继续流向本身的回调方法，返回 true
                    return true;
                } else if (ErrorStatus.NETWORK_UNAVAILABLE.equals(status)) {
                    Toast.makeText(SampleApplication.this, "没有网络连接", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        };
    }
}
