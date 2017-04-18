package cn.com.uama.retrofitmanager.bean;

/**
 * Created by liwei on 2017/4/18 11:58
 * Email: liwei@uama.com.cn
 * Description: 通用的分页列表实体类
 */

public class SimplePagedResp<T> extends BaseResp {
    private PagedBean<T> data;

    public PagedBean<T> getData() {
        return data;
    }

    public void setData(PagedBean<T> data) {
        this.data = data;
    }
}
