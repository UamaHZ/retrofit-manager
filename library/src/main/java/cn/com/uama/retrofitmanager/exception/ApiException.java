package cn.com.uama.retrofitmanager.exception;

/**
 * Created by Tina on 2017/7/10.
 * Description: 用于代表自定义接口访问异常的异常类
 */

public class ApiException extends Exception {
    private final String status;
    private final String msg;

    public ApiException(String status, String msg){
        this.status = status;
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
