package cn.com.uama.retrofitmanager.exception;

/**
 * Created by Tina on 2017/7/10.
 * Description:
 */

public class ApiException extends Exception {
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
}
