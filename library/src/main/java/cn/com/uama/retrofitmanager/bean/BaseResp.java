package cn.com.uama.retrofitmanager.bean;

/**
 * Created by liwei on 2017/4/18 11:49
 * Email: liwei@uama.com.cn
 * Description: 实体类基类
 */

public class BaseResp {
    private String status;
    private String msg;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
