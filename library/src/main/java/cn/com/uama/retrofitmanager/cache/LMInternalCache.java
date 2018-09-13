package cn.com.uama.retrofitmanager.cache;

import java.io.IOException;

import okhttp3.Request;
import okio.BufferedSource;

/**
 * Created by liwei on 2018/3/19 00:00
 * Email: liwei@uama.com.cn
 * Description: 内部使用的缓存接口
 */
public interface LMInternalCache {
    /**
     * 新增或更新对应请求的缓存
     *
     * @param request okhttp 请求对象
     * @param value   要缓存的数据
     */
    void put(Request request, String value);

    /**
     * 获取对应请求的缓存
     *
     * @param request okhttp 请求对象
     * @return 缓存数据
     */
    String get(Request request);

    /**
     * 获取对应请求缓存的 BufferedSource 对象
     *
     * @param request okhttp 请求对象
     * @return 缓存数据的 BufferedSource 对象
     * @throws IOException 异常
     */
    BufferedSource getBufferedSource(Request request) throws IOException;

    /**
     * 移除对应请求的缓存
     *
     * @param request okhttp 请求对象
     * @return 对应缓存存在并且删除成功为 true，否则为 false
     */
    boolean remove(Request request);

    /**
     * 判断对应请求的缓存是否有效
     *
     * @param request   okhttp 请求对象
     * @param cacheTime 缓存时间
     * @return 有效为 true，否则为 false
     */
    boolean isValid(Request request, long cacheTime);

    /**
     * 查询请求对象对应的缓存数据是否需要从接口刷新
     *
     * @param request okhttp 请求对象
     * @return 需要返回 true，否则返回 false
     */
    boolean needRefresh(Request request);

    /**
     * 将请求对象对应的缓存数据设置为需要从接口刷新
     *
     * @param request okhttp 请求对象
     */
    void setNeedRefresh(Request request);

    /**
     * 移除请求对象对应缓存数据的需要从接口刷新标志
     *
     * @param request okhttp 请求对象
     */
    void removeNeedRefresh(Request request);

    /**
     * 清除所有缓存
     *
     * @return 删除成功为 true，否则为 false
     */
    boolean clear();
}
