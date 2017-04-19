package cn.com.uama.retrofitmanager.bean;

import java.util.List;

/**
 * Created by liwei on 2017/4/19 13:59
 * Email: liwei@uama.com.cn
 * Description: 通用的（不分页）列表实体类
 */

public class SimpleListResp<T> extends BaseResp {
    private List<T> data;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
