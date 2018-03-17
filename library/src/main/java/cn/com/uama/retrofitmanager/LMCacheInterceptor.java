package cn.com.uama.retrofitmanager;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by liwei on 2018/3/16 10:06
 * Email: liwei@uama.com.cn
 * Description: 缓存处理逻辑拦截器
 */
public class LMCacheInterceptor implements Interceptor {

    private static final MediaType jsonType = MediaType.parse("application/json;charset=UTF-8");

    private final LMCache cache;
    private final Gson gson;

    public LMCacheInterceptor(LMCache cache) {
        this.cache = cache;
        this.gson = new Gson();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 判断是否是直接从接口获取的请求
        Request request = chain.request();
        boolean refresh = "Refresh".equals(request.tag());
        if (refresh) {
            return proceed(chain);
        }

        // 从缓存获取数据
        String cacheCandidate = cache != null
                ? cache.get(request)
                : null;

        // 判断缓存是否有缓存
        if (!TextUtils.isEmpty(cacheCandidate)) {
            // 有缓存，先将其返回
            // 判断缓存是否有效
            // 有效不需要做任何操作

            Response.Builder responseBuilder = new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(jsonType, cacheCandidate));

            // 失效的话要从网络进行获取
            BaseResp baseResp = parseBaseResp(cacheCandidate);
            if (baseResp != null) {
                long cacheTime = -1L;
                try {
                    cacheTime = Long.parseLong(baseResp.getCacheTime());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                if (!cache.isValid(request, cacheTime)) {
                    responseBuilder.addHeader("Need-Refresh", "true");
                }
            }

            return responseBuilder
                    .build();
        }

        return proceed(chain);
    }

    private Response proceed(Chain chain) throws IOException {
        Response networkResponse = chain.proceed(chain.request());
        // 从新获取到的数据中获取 cacheTime ，判断是否需要缓存
        // 如果不需要的话要将旧的缓存也删除
        ResponseBody body = networkResponse.body();
        String bodyStr = body.string();
        BaseResp baseResp = parseBaseResp(bodyStr);
        if (baseResp != null) {
            long cacheTime = -1L;
            try {
                cacheTime = Long.parseLong(baseResp.getCacheTime());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (cacheTime == 0) {
                // 表示不缓存，同时删除旧数据
                if (cache != null) {
                    cache.remove(chain.request());
                }
            } else if (cacheTime > 0) {
                // 缓存时间大于 0
                // 还要根据 status 判断是否进行缓存
                if (AdvancedRetrofitHelper.SUCCESS.equals(baseResp.getStatus()) && cache != null) {
                    cache.put(chain.request(), bodyStr);
                }
            } else {
                // 表示出现异常，不缓存新数据，但同时不删除旧数据
            }
        }

        // 因为 ResponseBody 已经被读取过了，所以需要重新构造
        return networkResponse.newBuilder()
                .body(ResponseBody.create(jsonType, bodyStr))
                .build();
    }

    /**
     * 将 ResponseBody 转换为 BaseResp
     *
     * @param body
     */
    private BaseResp parseBaseResp(String body) throws IOException {
        if (body == null) return null;
        return gson.fromJson(body, BaseResp.class);
    }
}
