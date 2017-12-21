package cn.com.uama.retrofitmanager;

import cn.com.uama.retrofitmanager.bean.BaseResp;

/**
 * Created by liwei on 2017/10/19.
 * Email: liwei@uama.com.cn
 * Description: 接口返回状态码拦截器
 */

public interface ApiStatusInterceptor {
    /**
     * 拦截方法
     *
     * @param baseResp 接口返回数据的基类，因为在这里我们只关心基础的几个字段
     * @return 如果不想数据继续往下走到后面的回调方法，返回 true，否则 false
     */
    boolean intercept(BaseResp baseResp);
}
