package cn.com.uama.retrofitmanager;

import android.content.Context;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by liwei on 2017/4/12 16:52
 * Email: liwei@uama.com.cn
 * Description: retrofit 访问接口帮助类
 */

public class AdvancedRetrofitHelper {

    public static final String SUCCESS = "100";
    public static final String FAILURE = "-1";

    /**
     * 将 context 中的 call 放到 contextCallMap 中，
     * 方便统一取消某个 context 的所有 call
     */
    private static WeakHashMap<Context, List<Call>> contextCallMap = new WeakHashMap<>();
    /**
     * 将 fragment 中的 call 放到 fragmentCallMap 中，
     * 方便统一取消某个 fragment 的所有 call
     */
    private static WeakHashMap<Fragment, List<Call>> fragmentCallMap = new WeakHashMap<>();

    private static WeakHashMap<Context, CompositeDisposable> contextDisposable = new WeakHashMap<>();
    private static WeakHashMap<Fragment, CompositeDisposable> fragmentDisposable = new WeakHashMap<>();
    /**
     * 增加一个 call 到 contextCallMap 中
     */
    public static void addCall(Context context, Call call) {
        List<Call> calls = contextCallMap.get(context);
        if (calls == null) {
            calls = new ArrayList<>();
            contextCallMap.put(context, calls);
        }
        if (!calls.contains(call)) {
            calls.add(call);
        }
    }

    /**
     * 增加一个 call 到 fragmentCallMap 中
     */
    public static void addCall(Fragment fragment, Call call) {
        List<Call> calls = fragmentCallMap.get(fragment);
        if (calls == null) {
            calls = new ArrayList<>();
            fragmentCallMap.put(fragment, calls);
        }
        if (!calls.contains(call)) {
            calls.add(call);
        }
    }

    public static void addDisposable(Context context, Disposable disposable){
        CompositeDisposable compositeDisposable = contextDisposable.get(context);
        if(null == compositeDisposable){
            compositeDisposable = new CompositeDisposable();
            contextDisposable.put(context, compositeDisposable);
        }
        compositeDisposable.add(disposable);
    }
    public static void addDisposable(Fragment fragment, Disposable disposable){
        CompositeDisposable compositeDisposable = fragmentDisposable.get(fragment);
        if(null == compositeDisposable){
            compositeDisposable = new CompositeDisposable();
            fragmentDisposable.put(fragment, compositeDisposable);
        }
        compositeDisposable.add(disposable);
    }

    /**
     * 取消 context 下的所有 call
     * 如果有需要，在 Activity 的 onDestroy() 里调用该方法
     */
    public static void cancelCalls(Context context) {
        List<Call> calls = contextCallMap.get(context);
        if (calls != null) {
            for (Call call : calls) {
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
            remove(context);
        }
        CompositeDisposable disposable = contextDisposable.get(context);
        if(null != disposable && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    /**
     * 取消 fragment 下的所有 call
     * 如果有需要，在 Fragment 的 onDestroyView() 里调用该方法
     * @param fragment
     */
    public static void cancelCalls(Fragment fragment) {
        List<Call> calls = fragmentCallMap.get(fragment);
        if (calls != null) {
            for (Call call : calls) {
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
            remove(fragment);
        }
        CompositeDisposable disposable = fragmentDisposable.get(fragment);
        if(null != disposable && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    /**
     * 从 {@link #contextCallMap} 中移除 context，主要是为了防止内存泄露
     */
    public static void remove(Context context) {
        contextCallMap.remove(context);
        contextDisposable.remove(context);
    }

    /**
     * 从 {@link #fragmentCallMap} 中移除 fragment，主要是为了防止内存泄露
     */
    public static void remove(Fragment fragment) {
        fragmentCallMap.remove(fragment);
        fragmentDisposable.remove(fragment);
    }

    public static <T extends BaseResp> void enqueue(Context context,
                                                    Call<T> call,
                                                    AdvancedRetrofitCallback<T> callback) {
        enqueue(context, call, callback, true);
    }

    public static <T extends BaseResp> void enqueue(final Context context,
                                                    Call<T> call,
                                                    final AdvancedRetrofitCallback<T> callback,
                                                    boolean shouldAddCall) {
        if (shouldAddCall) {
            addCall(context, call);
        }
        enqueueCall(context, call, callback);
    }

    public static <T extends BaseResp> void enqueue(Fragment fragment,
                                                    Call<T> call,
                                                    AdvancedRetrofitCallback<T> callback) {
        enqueue(fragment, call, callback, true);
    }

    public static <T extends BaseResp> void enqueue(Fragment fragment,
                                                    Call<T> call,
                                                    AdvancedRetrofitCallback<T> callback,
                                                    boolean shouldAddCall) {
        if (shouldAddCall) {
            addCall(fragment, call);
        }
        enqueueCall(fragment.getContext(), call, callback);
    }

    private static <T extends BaseResp> void enqueueCall(final Context context,
                                                         Call<T> call,
                                                         final AdvancedRetrofitCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (callback != null) {
                    callback.onEnd(call);
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
                    } else {
                        if (callback != null) {
                            callback.onError(call, status, msg);
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onError(call, String.valueOf(response.code()), null);
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (callback != null) {
                    callback.onEnd(call);
                }
                if (call.isCanceled()) return;
                if (callback != null) {
                    t.printStackTrace();
                    callback.onError(call, FAILURE, null);
                }
            }
        });
    }
}
