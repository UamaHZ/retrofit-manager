package cn.com.uama.retrofitmanager;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import cn.com.uama.retrofitmanager.exception.ApiException;
import cn.com.uama.retrofitmanager.exception.ResultInterceptedException;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
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

    private static WeakHashMap<Object, List<Call>> callMap = new WeakHashMap<>();
    private static WeakHashMap<Object, CompositeDisposable> disposableMap = new WeakHashMap<>();
    /**
     * 增加一个 {@link retrofit2.Call} 对象 call 到 {@link #callMap} 中
     * 对应 key 的所有 call 可以在适当的时候统一全部取消
     */
    public static void addCall(Object key, Call call) {
        List<Call> calls = callMap.get(key);
        if (calls == null) {
            calls = new ArrayList<>();
            callMap.put(key, calls);
        }
        if (!calls.contains(call)) {
            calls.add(call);
        }
    }

    /**
     * 增加一个 {@link io.reactivex.disposables.Disposable} 对象 disposable 到 {@link #disposableMap} 中
     * 对应 key 的所有 disposable 可以在适当的时候统一全部释放
     */
    public static void addDisposable(Object key, Disposable disposable){
        CompositeDisposable compositeDisposable = disposableMap.get(key);
        if(compositeDisposable == null){
            compositeDisposable = new CompositeDisposable();
            disposableMap.put(key, compositeDisposable);
        }
        compositeDisposable.add(disposable);
    }

    /**
     * 取消对应 key 的所有 call 访问
     */
    public static void cancelCalls(Object key) {
        List<Call> calls = callMap.get(key);
        if (calls != null) {
            for (Call call : calls) {
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }

            callMap.remove(key);
        }
    }

    /**
     * 释放对应 key 的所有 disposable
     */
    public static void disposeDisposables(Object key) {
        CompositeDisposable compositeDisposable = disposableMap.get(key);
        if (compositeDisposable != null) {
            if (!compositeDisposable.isDisposed()) {
                compositeDisposable.dispose();
            }

            disposableMap.remove(key);
        }
    }

    /**
     * 释放对应 key 的所有资源
     */
    public static void releaseResourcesFor(Object key) {
        cancelCalls(key);
        disposeDisposables(key);
    }

    /**
     * 从 {@link #callMap} 和 {@link #disposableMap} 中移除 key，主要是为了防止内存泄露
     */
    public static void remove(Object key) {
        callMap.remove(key);
        disposableMap.remove(key);
    }


    public static <T extends BaseResp> void enqueue(Object key,
                                                    Call<T> call,
                                                    AdvancedRetrofitCallback<T> callback) {
        enqueue(key, call, callback, true);
    }

    public static <T extends BaseResp> void enqueue(Object key,
                                                    Call<T> call,
                                                    AdvancedRetrofitCallback<T> callback,
                                                    boolean shouldAddCall) {
        if (shouldAddCall) {
            addCall(key, call);
        }
        enqueueCall(key, call, callback, shouldAddCall);
    }

    private static <T extends BaseResp> void enqueueCall(final Object key,
                                                         Call<T> call,
                                                         final AdvancedRetrofitCallback<T> callback,
                                                         final boolean shouldAddCall) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                // 判断是否需要从接口获取最新数据（缓存失效了）
                boolean needRefresh = Boolean.parseBoolean(response.headers().get("Need-Refresh"));
                if (needRefresh) {
                    scheduleARefreshCall(key, call, callback, shouldAddCall);
                }
                if (!call.isCanceled()) {
                    if (response.isSuccessful()) {
                        T body = response.body();
                        if (body != null) {
                            String status = body.getStatus();
                            String msg = body.getMsg();
                            if (Util.isIntercepted(body)) {
                                if (callback != null) {
                                    callback.onIntercepted(call, body);
                                }
                            } else {
                                if (SUCCESS.equals(status)) {
                                    if (callback != null) {
                                        callback.onSuccess(call, body);
                                    }
                                } else {
                                    if (callback != null) {
                                        callback.onError(call, status, msg);
                                        callback.onError(call, body);
                                    }
                                }
                            }
                        } else {
                            // body 为 null 表示 http status code 是 204 或 205
                            // 这种情况下没有服务端定义的状态码值，我们认为获取数据失败
                            String status = String.valueOf(response.code());
                            onCallError(call, status, callback);
                        }
                    } else {
                        String status = String.valueOf(response.code());
                        onCallError(call, status, callback);
                    }

                    if (callback != null) {
                        callback.onEnd(call);
                    }
                } else {
                    if (callback != null) {
                        callback.onCanceled();
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (!call.isCanceled()) {
                    String status = ErrorStatus.FAILURE;
                    if (t instanceof ConnectException) {
                        // 如果异常为 ConnectException ，认为是没有网络
                        status = ErrorStatus.NETWORK_UNAVAILABLE;
                    }

                    onCallError(call, status, callback);

                    if (callback != null) {
                        callback.onEnd(call);
                    }
                } else {
                    if (callback != null) {
                        callback.onCanceled();
                    }
                }
            }
        });
    }

    private static <T extends BaseResp> void onCallError(Call<T> call, String status, AdvancedRetrofitCallback<T> callback) {
        BaseResp errorResp = Util.createErrorResp(status);
        if (Util.isIntercepted(errorResp)) {
            if (callback != null) {
                callback.onIntercepted(call, errorResp);
            }
        } else {
            if (callback != null) {
                callback.onError(call, status, null);
                callback.onError(call, errorResp);
            }
        }
    }

    /**
     * 发送一个直接访问网络接口的请求
     */
    private static <T extends BaseResp> void scheduleARefreshCall(
            Object key, Call<T> call,
            AdvancedRetrofitCallback<T> callback,
            final boolean shouldAddCall) {
        Call<T> networkCall = call.clone();
        Request request = networkCall.request();
        // 通过反射的方式将 request 对象的 tag 属性设为 “Refresh”
        // 然后在缓存拦截器中通过判断 request 的 tag 是否为 “Refresh” 来判断是否直接请求接口
        if (request != null) {
            try {
                Field tagField = request.getClass().getDeclaredField("tag");
                tagField.setAccessible(true);
                tagField.set(request, "Refresh");
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException ignored) {
            }
        }
        enqueue(key, networkCall, callback, shouldAddCall);
    }

    public static <T extends BaseResp> ObservableTransformer<T, T> rxObservableTransformer(Object key) {
        return rxObservableTransformer(key, true);
    }

    /**
     * 包含统一逻辑处理的 ObservableTransformer ,使用 Observable 进行接口访问时使用。
     * @param key 上下文
     * @param shouldAddDisposable 是否要加到 CompositeDisposable 中以方便统一取消
     */
    public static <T extends BaseResp> ObservableTransformer<T, T> rxObservableTransformer(final Object key,
                                                                                   final boolean shouldAddDisposable) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                if (shouldAddDisposable) {
                                    addDisposable(key, disposable);
                                }
                            }
                        })
                        .map(new Function<T, T>() {
                            @Override
                            public T apply(T t) throws Exception {
                                if (Util.isIntercepted(t)) {
                                    throw new ResultInterceptedException(t);
                                }
                                return t;
                            }
                        })
                        .map(new Function<T, T>() {
                            @Override
                            public T apply(@NonNull T t) throws Exception {
                                String status = t.getStatus();
                                if(!SUCCESS.equals(status)){
                                    throw new ApiException(t);
                                }
                                return t;
                            }
                        });
            }
        };
    }
}
