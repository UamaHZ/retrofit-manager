package cn.com.uama.retrofitmanager;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

import cn.com.uama.retrofitmanager.bean.BaseResp;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.cache.InternalCache;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;

/**
 * Created by liwei on 2018/3/16 10:06
 * Email: liwei@uama.com.cn
 * Description: 缓存处理逻辑拦截器
 */
public class LMCacheInterceptor implements Interceptor {

    final InternalCache cache;
    final Gson gson;
    final TypeAdapter<BaseResp> typeAdapter;

    public LMCacheInterceptor(InternalCache cache) {
        this.cache = cache;
        this.gson = new Gson();
        typeAdapter = gson.getAdapter(BaseResp.class);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 判断是否是直接从接口获取的请求
        Request request = chain.request();
        boolean refresh = Boolean.parseBoolean(request.header("Refresh"));
        if (refresh) {
            return proceed(chain);
        }

        // 从缓存获取数据
        Response cacheCandidate = cache != null
                ? cache.get(request)
                : null;

        // 判断缓存是否有缓存
        if (cacheCandidate != null) {
            // 有缓存，先将其返回
            // 判断缓存是否有效
            // 有效不需要做任何操作

            // TODO: 2018/3/16 20:11 如何先对 body 缓存？ （李炜）

            // 失效的话要从网络进行获取
            BaseResp baseResp = parseBaseResp(cacheCandidate.body());
            if (baseResp != null) {

            }

            return cacheCandidate;
        }

        return proceed(chain);
    }

    private Response proceed(Chain chain) throws IOException {
        Response networkResponse = chain.proceed(chain.request());
        // 从新获取到的数据中获取 cacheTime ，判断是否需要缓存
        // 如果不需要的话要将旧的缓存也删除
        ResponseBody body = networkResponse.body();
        Buffer buffer = body.source().buffer();
        Buffer newBuffer = new Buffer();
        buffer.copyTo(newBuffer, 0, buffer.size());
        BaseResp baseResp = parseBaseResp(body);
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
                // 新增或更新缓存
                cache.remove(chain.request());
                // TODO: 2018/3/16 21:02 新增缓存 （李炜）
            } else {
                // 表示出现异常，不缓存新数据，但同时不删除旧数据
            }
        }
        // 如果需要带的话还需要校验数据的有效性
        // 比如 status 是否是 100

        // 因为 ResponseBody 已经被读取过了，所以需要重新构造
        String contentType = networkResponse.header("Content-Type");
        return networkResponse.newBuilder()
                .body(new RealResponseBody(contentType, newBuffer.size(), newBuffer))
                .build();
    }

    /**
     * 将 ResponseBody 转换为 BaseResp
     */
    private BaseResp parseBaseResp(ResponseBody body) throws IOException {
        if (body == null) return null;
        JsonReader jsonReader = gson.newJsonReader(body.charStream());
        return typeAdapter.read(jsonReader);
    }
}
