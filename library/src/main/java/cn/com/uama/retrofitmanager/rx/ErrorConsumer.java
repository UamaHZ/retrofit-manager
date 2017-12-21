package cn.com.uama.retrofitmanager.rx;

import java.net.ConnectException;

import cn.com.uama.retrofitmanager.ErrorStatus;
import cn.com.uama.retrofitmanager.bean.BaseResp;
import cn.com.uama.retrofitmanager.exception.ApiException;
import cn.com.uama.retrofitmanager.exception.ResultInterceptedException;
import cn.com.uama.retrofitmanager.Util;
import io.reactivex.functions.Consumer;
import retrofit2.HttpException;

/**
 * Created by liwei on 2017/11/13 20:25
 * Email: liwei@uama.com.cn
 * Description: 用于统一处理异常，将错误码和错误信息暴露给使用者
 */

public abstract class ErrorConsumer implements Consumer<Throwable> {

    @Override
    public final void accept(Throwable throwable) throws Exception {
        if (throwable instanceof ResultInterceptedException) {
            ResultInterceptedException interceptedException = (ResultInterceptedException) throwable;
            onIntercepted(interceptedException.getResult());
        } else {
            BaseResp resp;
            if (throwable instanceof ApiException) {
                // 接口正常返回，服务端定义错误
                ApiException apiException = (ApiException) throwable;
                resp = apiException.getResp();
            } else if (throwable instanceof retrofit2.HttpException) {
                // 接口访问失败，HTTP 错误
                HttpException httpException = (HttpException) throwable;
                String status = String.valueOf(httpException.code());
                resp = Util.createErrorResp(status);
            } else {
                // 其他异常
                String status = ErrorStatus.FAILURE;
                if (throwable instanceof ConnectException) {
                    // ConnectException 被认为是没有网络连接
                    status = ErrorStatus.NETWORK_UNAVAILABLE;
                }
                resp = Util.createErrorResp(status);
            }

            if (Util.isIntercepted(resp)) {
                onIntercepted(resp);
            } else {
                onError(resp);
            }
        }
    }

    /**
     * 错误回调方法
     *
     * @param resp 包含错误信息的 {@link BaseResp} 对象
     */
    public abstract void onError(BaseResp resp);

    /**
     * 被“劫持”回调方法
     *
     * @param result 数据实体
     */
    public void onIntercepted(BaseResp result) {}
}
