package cn.com.uama.retrofitmanager;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import retrofit2.Call;

/**
 * Created by liwei on 2016/10/27 15:11
 * Email: liwei@uama.com.cn
 * Description: 自定义retrofit访问接口回调
 */
public interface AdvancedRetrofitCallback<T> {
    /**
     * 接口访问成功时的回调方法，
     * 这里的成功指的接口返回的 json 数据中包含的 status 字段的值为 100 。
     *
     * @param call 接口请求对象
     * @param resp 数据实体
     */
    void onSuccess(Call<T> call, T resp);

    /**
     * 接口访问失败时的回调方法，
     * 这里的失败包括除 status 为 100 外的所有情况。
     *
     * @param call 接口请求对象
     * @param errorCode 错误码，可能为 status 的值或 http 请求返回的 code 值或 -1
     * @param msg 错误信息，可能是接口返回 json 数据中包含的 msg 的值或 null
     */
    void onError(Call<T> call, String errorCode, String msg);

    /**
     * 接口访问失败时的回调方法
     *
     * @param call 接口请求对象
     * @param baseResp 包含错误信息的 {@link BaseResp} 对象
     * @since 1.2
     */
    void onError(Call<T> call, BaseResp baseResp);

    /**
     * 接口返回数据被“劫持”时的回调，
     * 被“劫持”表示 {@link ApiStatusInterceptor#intercept(cn.com.uama.retrofitmanager.bean.BaseResp)} 方法返回 true 。
     *
     * 增加这个回调方法的目的是在这种情况下给使用者一个机会处理特定逻辑。
     *
     * @param call 接口请求对象
     * @param resp 数据实体，如果接口访问成功，可以将 resp 强转为具体的实体类，否则 resp 只包含一个 status
     */
    void onIntercepted(Call<T> call, BaseResp resp);

    /**
     * 接口请求结束时的回调，在没有被取消的情况下都会走该方法。
     *
     * @param call 接口请求对象
     */
    void onEnd(Call<T> call);

    /**
     * 接口请求被取消时的回调
     */
    void onCanceled();
}
