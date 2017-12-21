package cn.com.uama.retrofitmanager.exception;

import cn.com.uama.retrofitmanager.bean.BaseResp;

/**
 * Created by Tina on 2017/7/10.
 * Description: 用于代表自定义接口访问异常的异常类
 */

public class ApiException extends Exception {
    private final BaseResp resp;

    public ApiException(BaseResp resp){
        this.resp = resp;
    }

    public BaseResp getResp() {
        return resp;
    }
}
