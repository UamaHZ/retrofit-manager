package cn.com.uama.retrofitmanager.rx.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public final class LMRxJava2CallAdapterFactory extends CallAdapter.Factory {

    public static LMRxJava2CallAdapterFactory create() {
        return new LMRxJava2CallAdapterFactory();
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);

        if (rawType == Completable.class) {
            return new LMRxJava2CallAdapter(Void.class, false, false,
                    false, true);
        }

        boolean isFlowable = rawType == Flowable.class;
        boolean isSingle = rawType == Single.class;
        boolean isMaybe = rawType == Maybe.class;
        if (rawType != Observable.class && !isFlowable && !isSingle && !isMaybe) {
            return null;
        }

        if (!(returnType instanceof ParameterizedType)) {
            String name = isFlowable ? "Flowable"
                    : isSingle ? "Single"
                    : isMaybe ? "Maybe" : "Observable";
            throw new IllegalStateException(name + " return type must be parameterized"
                    + " as " + name + "<Foo> or " + name + "<? extends Foo>");
        }

        Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        if (!(BaseResp.class.isAssignableFrom(getRawType(responseType)))) {
            // 如果返回值类型不是 BaseResp 或其子类则这个 adapter 不处理
            return null;
        }

        return new LMRxJava2CallAdapter(responseType, isFlowable, isSingle, isMaybe,
                false);
    }
}
