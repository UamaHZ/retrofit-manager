package cn.com.uama.retrofitmanager;

/**
 * Created by liwei on 2017/10/19.
 * Email: liwei@uama.com.cn
 * Description: 接口返回状态码拦截器
 */

public interface ApiStatusInterceptor {
    /**
     * 拦截方法
     * @param status 接口状态码
     * @param message 描述信息
     * @return 如果不想数据继续往下走到后面的回调方法，返回 true，否则 false
     */
    boolean intercept(String status, String message);
}
