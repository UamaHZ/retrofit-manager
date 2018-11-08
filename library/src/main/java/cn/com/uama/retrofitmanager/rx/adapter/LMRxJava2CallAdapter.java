package cn.com.uama.retrofitmanager.rx.adapter;

import java.lang.reflect.Type;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.CallAdapter;

final class LMRxJava2CallAdapter<R extends BaseResp> implements CallAdapter<R, Object> {
    private final Type responseType;
    private final boolean isFlowable;
    private final boolean isSingle;
    private final boolean isMaybe;
    private final boolean isCompletable;

    LMRxJava2CallAdapter(Type responseType, boolean isFlowable, boolean isSingle,
                         boolean isMaybe, boolean isCompletable) {
        this.responseType = responseType;
        this.isFlowable = isFlowable;
        this.isSingle = isSingle;
        this.isMaybe = isMaybe;
        this.isCompletable = isCompletable;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public Object adapt(Call<R> call) {
        Observable<R> responseObservable = new LMResponseObservable<>(call);

        responseObservable = responseObservable.subscribeOn(Schedulers.io());

        if (isFlowable) {
            return responseObservable.toFlowable(BackpressureStrategy.LATEST);
        }
        if (isSingle) {
            return responseObservable.singleOrError();
        }
        if (isMaybe) {
            return responseObservable.singleElement();
        }
        if (isCompletable) {
            return responseObservable.ignoreElements();
        }
        return RxJavaPlugins.onAssembly(responseObservable);
    }
}
