package cn.com.uama.retrofitmanager.base;

import android.content.Context;
import android.support.v4.app.Fragment;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.bean.BaseResp;
import cn.com.uama.retrofitmanager.exception.ApiException;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Tina on 2017/7/19.
 * Description: 通用转换
 */

public class BaseTransformer {
    public static <T extends BaseResp> ObservableTransformer<T, T>  switchSchedulers(final Context context){
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                //  做泄漏处理
                                AdvancedRetrofitHelper.addDisposable(context, disposable);
                            }
                        })
                        .map(new Function<T, T>() {
                            @Override
                            public T apply(@NonNull T t) throws Exception {
                                if(!AdvancedRetrofitHelper.SUCCESS.equals(t.getStatus())){
                                    throw new ApiException(t.getStatus(), t.getMsg());
                                }
                                return t;
                            }
                        });
            }
        };
    }
    public static <T extends BaseResp> ObservableTransformer<T, T>  switchSchedulers(final Fragment fragment){
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                //  做泄漏处理
                                AdvancedRetrofitHelper.addDisposable(fragment, disposable);
                            }
                        })
                        .map(new Function<T, T>() {
                            @Override
                            public T apply(@NonNull T t) throws Exception {
                                if(!AdvancedRetrofitHelper.SUCCESS.equals(t.getStatus())){
                                    throw new ApiException(t.getStatus(), t.getMsg());
                                }
                                return t;
                            }
                        });
            }
        };
    }
}
