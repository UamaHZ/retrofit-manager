package cn.com.uama.retrofitmanager.rx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.bean.BaseResp;
import cn.com.uama.retrofitmanager.exception.ApiException;
import cn.com.uama.retrofitmanager.exception.ResultInterceptedException;
import io.reactivex.functions.Consumer;

/**
 * Created by liwei on 2017/11/13 20:25
 * Email: liwei@uama.com.cn
 * Description: 用于统一处理异常，将错误码和错误信息暴露给使用者
 */

public abstract class ErrorConsumer implements Consumer<Throwable> {
    private static final String ERROR_MSG_SOCKET_TIMEOUT = "网络连接超时，请检查您的网络状态，稍后重试！";
    private static final String ERROR_MSG_SOCKET = "网络链接异常，请检查您的网络状态";
    private static final String ERROR_MSG_UNKNOWN_HOST = "网络异常，请检查您的网络状态";

    @Override
    public final void accept(Throwable throwable) throws Exception {
        if (throwable instanceof ResultInterceptedException) {
            ResultInterceptedException interceptedException = (ResultInterceptedException) throwable;
            onIntercepted(interceptedException.getResult());
        } else {
            String errorCode;
            String errorMsg = null;
            if (throwable instanceof ApiException) {
                ApiException apiException = (ApiException) throwable;
                errorCode = apiException.getStatus();
                errorMsg = apiException.getMsg();
            } else {
                errorCode = AdvancedRetrofitHelper.FAILURE;
                if (throwable instanceof SocketTimeoutException) {
                    errorMsg = ERROR_MSG_SOCKET_TIMEOUT;
                } else if (throwable instanceof SocketException) {
                    errorMsg = ERROR_MSG_SOCKET;
                } else if (throwable instanceof UnknownHostException) {
                    errorMsg = ERROR_MSG_UNKNOWN_HOST;
                }
            }

            onError(errorCode, errorMsg);
        }
    }

    /**
     * 错误回调方法
     * @param code 错误码
     * @param msg 错误信息
     */
    public abstract void onError(@NonNull String code, @Nullable String msg);

    /**
     * 被“劫持”回调方法
     * @param result 数据实体
     */
    public void onIntercepted(BaseResp result) {}
}
