package cn.com.uama.retrofitmanager.base;

import android.widget.Toast;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by Tina on 2017/7/19.
 * Description:
 */

public abstract class BaseObserver<T extends BaseResp> implements Observer<T>, ISubscriber<T> {
    private Toast mToast;
    @Override
    public void onSubscribe(@NonNull Disposable d) {
        doOnSubscribe(d);
    }

    @Override
    public void onNext(@NonNull T t) {
        doOnNext(t);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        doOnError(e);
//        if(e instanceof SocketTimeoutException){
//            setError(ApiException.errorMsg_SocketTimeoutException);
//        }else if(e instanceof ConnectException){
//            setError(ApiException.errorMsg_ConnectException);
//        }else if(e instanceof UnknownHostException){
//            setError(ApiException.errorMsg_UnknownHostException);
//        }else{
//            String error = e.getMessage();
//            doOnError(e);
//        }
    }

    @Override
    public void onComplete() {
        doOnCompleted();
    }

}
