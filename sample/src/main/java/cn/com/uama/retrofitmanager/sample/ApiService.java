package cn.com.uama.retrofitmanager.sample;

import cn.com.uama.retrofitmanager.sample.bean.UpdateBean;

import cn.com.uama.retrofitmanager.bean.SimpleResp;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by liwei on 2017/10/20.
 */

public interface ApiService {
    @GET("/v37/main/getAppVersion")
    Call<SimpleResp<UpdateBean>> checkNewVersion(@Query("mtype") String type);
}
