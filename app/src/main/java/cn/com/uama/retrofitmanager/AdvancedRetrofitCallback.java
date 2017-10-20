package cn.com.uama.retrofitmanager;

import retrofit2.Call;

/**
 * Created by liwei on 2016/10/27 15:11
 * Email: liwei@uama.com.cn
 * Description: 自定义retrofit访问接口回调
 */

public interface AdvancedRetrofitCallback<T> {
    void onSuccess(Call<T> call, T resp);
    void onError(Call<T> call, String errorCode, String msg);
    void onEnd(Call<T> call);
}
