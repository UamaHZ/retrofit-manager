package cn.com.uama.retrofitmanager.bean;

/**
 * Created by liwei on 2017/4/18 11:51
 * Email: liwei@uama.com.cn
 * Description: 通用的实体类，通过泛型映射实际的数据类型
 */

public class SimpleResp<T> extends BaseResp {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
