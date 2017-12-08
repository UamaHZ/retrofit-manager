package cn.com.uama.retrofitmanager.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by liwei on 2017/4/18 11:59
 * Email: liwei@uama.com.cn
 * Description: 分页的数据类型定义
 */

public class PagedBean<T> {
    @SerializedName(value = "pageResult", alternate = "pageInfo")
    private PageResult pageResult;
    private List<T> resultList;

    public PageResult getPageResult() {
        return pageResult;
    }

    public void setPageResult(PageResult pageResult) {
        this.pageResult = pageResult;
    }

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }
}
