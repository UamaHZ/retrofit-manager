package cn.com.uama.retrofitmanager.exception;

import android.text.TextUtils;

/**
 * Created by Tina on 2017/7/10.
 * Description: 用于代表自定义接口访问异常的异常类
 */

public class ApiException extends Exception {
    private String status;
    private String message;

    public ApiException(String status, String message){
        super();
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        if(TextUtils.isEmpty(message)){
            return getMessage();
        }
        return message;
    }

    public void setMsg(String message) {
        this.message = message;
    }
}
