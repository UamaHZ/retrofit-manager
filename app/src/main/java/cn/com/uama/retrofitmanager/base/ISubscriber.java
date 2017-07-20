package cn.com.uama.retrofitmanager.base;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import io.reactivex.disposables.Disposable;

/**
 * Created by Tina on 2017/7/19.
 * Description: 请求结果处理接口
 */

public interface ISubscriber<T extends BaseResp> {
    void doOnSubscribe(Disposable d);

    void doOnError(Throwable e);

    void doOnNext(T t);

    void doOnCompleted();
}
