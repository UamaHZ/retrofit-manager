package cn.com.uama.retrofitmanager.exception;

import android.text.TextUtils;

/**
 * Created by Tina on 2017/7/10.
 * Description:
 */

public class ApiException extends Exception {
    public static final String errorMsg_SocketTimeoutException = "网络连接超时，请检查您的网络状态，稍后重试！";
    public static final String errorMsg_ConnectException = "网络链接异常，请检查您的网络状态";
    public static final String errorMsg_UnknownHostException = "网络异常，请检查您的网络状态";
    private String status;
    private String message;
    public ApiException(){
        super();
    }
    public ApiException(String message){
        super(message);
    }
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
