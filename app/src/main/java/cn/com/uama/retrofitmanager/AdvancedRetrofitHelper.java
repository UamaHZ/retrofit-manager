package cn.com.uama.retrofitmanager;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by liwei on 2017/4/12 16:52
 * Email: liwei@uama.com.cn
 * Description: retrofit 访问接口帮助类
 */

public class AdvancedRetrofitHelper {

    private static final String SUCCESS = "100";
    private static final String TOKEN_EXPIRED = "102";

    /**
     * 将 context 中的 call 放到 callMap 中，
     * 方便统一取消某个 context 的所有 call
     */
    private static Map<Context, List<Call>> callMap = new HashMap<>();

    /**
     * 增加一个 call 到 callMap 中
     */
    public static void addCall(Context context, Call call) {
        List<Call> calls = callMap.get(context);
        if (calls == null) {
            calls = new ArrayList<>();
            callMap.put(context, calls);
        }
        if (!calls.contains(call)) {
            calls.add(call);
        }
    }

    /**
     * 取消 context 下的所有 call
     * 一定要在 Activity 或者 Fragment 的 onDestroy() 里调用该方法
     */
    public static void cancelCalls(Context context) {
        List<Call> calls = callMap.get(context);
        if (calls != null) {
            for (Call call : calls) {
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
            remove(context);
        }
    }

    /**
     * 从 callMap 中移除 context，主要是为了防止内存溢出
     */
    public static void remove(Context context) {
        callMap.remove(context);
    }

    public static <T extends BaseResp> void enqueue(Context context, Call<T> call, AdvancedRetrofitCallback<T> callback) {
        enqueue(context, call, callback, true);
    }

    public static <T extends BaseResp> void enqueue(final Context context,
                                                    Call<T> call,
                                                    final AdvancedRetrofitCallback<T> callback,
                                                    boolean shouldAddCall) {
        if (shouldAddCall) {
            addCall(context, call);
        }
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (callback != null) {
                    callback.onEnd();
                }
                if (call.isCanceled()) return;
                if (response.isSuccessful()) {
                    T body = response.body();
                    String status = body.getStatus();
                    String msg = body.getMsg();
                    if (status.equals(SUCCESS)) {
                        if (callback != null) {
                            callback.onSuccess(call, body);
                        }
                    } else if (status.equals(TOKEN_EXPIRED)) { // token失效
                        if (TextUtils.isEmpty(msg)) {
                            msg = "账号已失效,请重新登录";
                        }
                        if (callback != null) {
                            callback.onTokenExpired(context, msg);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError(call, msg);
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onError(call, "服务器数据异常，请稍后再试");
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (callback != null) {
                    callback.onEnd();
                }
                if (call.isCanceled()) return;
                if (callback != null) {
                    t.printStackTrace();
                    callback.onError(call, "服务器数据异常，请稍后再试");
                }
            }
        });
    }
}
