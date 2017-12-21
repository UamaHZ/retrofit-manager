package cn.com.uama.retrofitmanager;

import cn.com.uama.retrofitmanager.bean.BaseResp;

/**
 * Created by liwei on 2017/12/21 15:00
 * Email: liwei@uama.com.cn
 * Description: 工具类
 */
public class Util {
    /**
     * 创建包含错误码的 {@link BaseResp} 对象
     *
     * @param status 错误码
     * @return 包含错误码的 {@link BaseResp} 对象
     */
    public static BaseResp createErrorResp(String status) {
        BaseResp baseResp = new BaseResp();
        baseResp.setStatus(status);
        return baseResp;
    }

    /**
     * 判断返回数据是否被拦截
     *
     * @param resp 返回数据
     * @return 如果被拦截返回 true 否则返回 false
     */
    public static boolean isIntercepted(BaseResp resp) {
        return RetrofitManager.apiStatusInterceptor != null &&
                RetrofitManager.apiStatusInterceptor.intercept(resp);
    }
}
