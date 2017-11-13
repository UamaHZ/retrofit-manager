package cn.com.uama.retrofitmanager.sample;

import cn.com.uama.retrofitmanager.bean.SimpleResp;
import cn.com.uama.retrofitmanager.sample.bean.UpdateBean;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by liwei on 2017/10/20.
 */

public interface ApiService {
    /**
     * 普通接口定义
     */
    @GET("/v37/main/getAppVersion")
    Call<SimpleResp<UpdateBean>> checkNewVersion(@Query("mtype") String type);

    /**
     * RxJava 方式接口定义
     */
    @GET("/v37/main/getAppVersion")
    Observable<SimpleResp<UpdateBean>> checkNewVersionRx(@Query("mtype") String type);
}
