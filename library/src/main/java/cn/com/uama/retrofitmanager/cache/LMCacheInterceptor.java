package cn.com.uama.retrofitmanager.cache;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.bean.BaseResp;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Created by liwei on 2018/3/16 10:06
 * Email: liwei@uama.com.cn
 * Description: 缓存处理逻辑拦截器
 */
public class LMCacheInterceptor implements Interceptor {

    private static final MediaType jsonType = MediaType.parse("application/json;charset=UTF-8");

    public static final String REFRESH_FROM_SERVER = "refresh_from_server";
    public static final String HEADER_NEED_REFRESH = "Need-Refresh";
    public static final String HEADER_FROM_CACHE = "From-Cache";
    private static final String QUERY_CUR_PAGE = "curPage";

    private final LMInternalCache cache;
    private final JsonAdapter<BaseResp> baseRespJsonAdapter;

    public LMCacheInterceptor(LMInternalCache cache) {
        this.cache = cache;
        this.baseRespJsonAdapter = new Moshi.Builder().build().adapter(BaseResp.class);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 判断是否是需要直接从接口获取数据的请求
        Request request = chain.request();
        if (REFRESH_FROM_SERVER.equals(request.tag())) {
            return proceed(chain);
        }

        // 从缓存获取数据
        BufferedSource cacheBufferedSource = cache != null
                ? cache.getBufferedSource(request)
                : null;

        // 判断是否有缓存
        if (cacheBufferedSource != null) {
            // 有缓存
            // 构造缓存数据的 Response 对象
            ResponseBody body = ResponseBody.create(jsonType, -1, cacheBufferedSource);
            Response.Builder responseBuilder = new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body);

            // 根据 cacheTime 判断缓存是否已经失效
            BaseResp baseResp = baseRespJsonAdapter
                    .fromJson(cache.getBufferedSource(request));
            if (baseResp != null) {
                long cacheTime = -1L;
                try {
                    cacheTime = Long.parseLong(baseResp.getCacheTime());
                } catch (NumberFormatException ignored) {
                }

                if (!cache.isValid(request, cacheTime)) {
                    // 缓存失效的话要从网络进行获取最新的数据
                    responseBuilder.addHeader(HEADER_NEED_REFRESH, "true");
                }
            }

            // 将缓存数据返回
            return responseBuilder
                    .header(HEADER_FROM_CACHE, "true")
                    .build();
        }

        // 没有缓存，从接口请求数据
        return proceed(chain);
    }

    /**
     * 从接口请求数据，并根据 cacheTime 和 status 字段判断是否要缓存返回的数据
     */
    private Response proceed(Chain chain) throws IOException {
        Request request = chain.request();
        // 从接口获取最新数据
        Response networkResponse = chain.proceed(request);

        ResponseBody body = networkResponse.body();
        if (body == null) return networkResponse;

        String bodyStr = body.string();
        BaseResp baseResp = parseBaseResp(bodyStr);
        // 先判断是否是成功状态的数据，不成功的数据不缓存
        if (baseResp != null && AdvancedRetrofitHelper.SUCCESS.equals(baseResp.getStatus())) {
            long cacheTime = -1L;
            try {
                cacheTime = Long.parseLong(baseResp.getCacheTime());
            } catch (NumberFormatException ignored) {
            }

            if (cacheTime == 0) {
                // 表示不缓存，同时删除旧数据
                if (cache != null) {
                    cache.remove(request);
                }
            } else if (cacheTime > 0) {
                // 缓存时间大于 0

                boolean shouldCache = true;
                // 如果是分页接口，并且 curPage 不等于 1 则不缓存（只缓存第一页数据）
                HttpUrl url = request.url();
                Set<String> queryNames = url.queryParameterNames();
                if (queryNames.contains(QUERY_CUR_PAGE)) {
                    List<String> curPageValues = url.queryParameterValues(QUERY_CUR_PAGE);
                    if (curPageValues.size() > 0 && !"1".equals(curPageValues.get(0))) {
                        // 如果不是第一页则不缓存
                        shouldCache = false;
                    }
                }
                if (shouldCache && cache != null) {
                    cache.put(request, bodyStr);
                }
            }
        }

        // 因为 ResponseBody 已经被读取过了，所以需要重新构造
        return networkResponse.newBuilder()
                .body(ResponseBody.create(jsonType, bodyStr))
                .build();
    }

    /**
     * 将返回数据转换为 BaseResp
     *
     * @param body 字符串格式的返回数据
     */
    private BaseResp parseBaseResp(String body) throws IOException {
        if (body == null) return null;
        return baseRespJsonAdapter.fromJson(body);
    }
}
