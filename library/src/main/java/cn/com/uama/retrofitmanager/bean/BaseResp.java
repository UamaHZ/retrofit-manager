package cn.com.uama.retrofitmanager.bean;

/**
 * Created by liwei on 2017/4/18 11:49
 * Email: liwei@uama.com.cn
 * Description: 实体类基类
 */

public class BaseResp {
    // 状态码
    private String status;
    // 提示信息
    private String msg;
    // 消息码
    private String msgCode;
    // 缓存时间，单位为秒
    private String cacheTime;
    // 是否来自缓存
    private boolean fromCache;

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

    public String getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    public String getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(String cacheTime) {
        this.cacheTime = cacheTime;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
}
