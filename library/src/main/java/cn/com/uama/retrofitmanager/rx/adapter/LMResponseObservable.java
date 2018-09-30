package cn.com.uama.retrofitmanager.rx.adapter;

import java.io.IOException;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import cn.com.uama.retrofitmanager.cache.LMCacheInterceptor;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

final class LMResponseObservable<T extends BaseResp> extends Observable<T> {
    private final Call<T> originalCall;
    private boolean terminated;
    private CallDisposable disposable;

    LMResponseObservable(Call<T> originalCall) {
        this.originalCall = originalCall;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        Call<T> call = originalCall.clone();
        disposable = new CallDisposable(call);
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) return;

        terminated = false;
        try {
            Response<T> response = call.execute();
            if (disposable.isDisposed()) return;

            if (response.isSuccessful()) {
                // 判断返回数据是否是缓存数据
                boolean fromCache = Boolean.parseBoolean(
                        response.headers().get(LMCacheInterceptor.HEADER_FROM_CACHE));
                T body = response.body();
                if (body != null) {
                    body.setFromCache(fromCache);
                    observer.onNext(body);
                }

                if (disposable.isDisposed()) return;

                // 判断是否需要从接口获取最新数据（缓存失效了）
                boolean needRefresh = Boolean.parseBoolean(
                        response.headers().get(LMCacheInterceptor.HEADER_NEED_REFRESH));
                if (needRefresh) {
                    // 需要获取最新数据，再发送一次请求
                    sendRefreshRequest(observer);
                }

                if (disposable.isDisposed() || terminated) return;

                terminated = true;
                observer.onComplete();
            } else {
                onHttpException(response, observer);
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            if (terminated) {
                RxJavaPlugins.onError(t);
            } else if (!disposable.isDisposed()) {
                try {
                    observer.onError(t);
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                    RxJavaPlugins.onError(new CompositeException(t, inner));
                }
            }
        }
    }

    private void sendRefreshRequest(Observer<? super T> observer) throws IOException {
        Call<T> call = originalCall.clone();
        Response<T> response = call.execute();
        if (disposable.isDisposed()) return;

        if (response.isSuccessful()) {
            T body = response.body();
            if (body != null) {
                body.setFromCache(false);
                observer.onNext(body);
            }
        } else {
            onHttpException(response, observer);
        }
    }

    private void onHttpException(Response<T> response, Observer<? super T> observer) {
        terminated = true;
        Throwable t = new HttpException(response);
        try {
            observer.onError(t);
        } catch (Throwable inner) {
            Exceptions.throwIfFatal(inner);
            RxJavaPlugins.onError(new CompositeException(t, inner));
        }
    }

    private static final class CallDisposable implements Disposable {
        private final Call<?> call;
        private volatile boolean disposed;

        CallDisposable(Call<?> call) {
            this.call = call;
        }

        @Override
        public void dispose() {
            disposed = true;
            call.cancel();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
