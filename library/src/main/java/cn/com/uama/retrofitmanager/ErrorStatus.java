package cn.com.uama.retrofitmanager;

/**
 * Created by liwei on 2017/12/21 13:56
 * Email: liwei@uama.com.cn
 * Description: 错误状态定义
 */
public class ErrorStatus {
    private ErrorStatus() {}

    /**
     * 失败
     */
    public static final String FAILURE = "-1";
    /**
     * 没有网络
     */
    public static final String NETWORK_UNAVAILABLE = "-2";
}
