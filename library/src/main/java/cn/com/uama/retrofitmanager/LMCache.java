package cn.com.uama.retrofitmanager;

import java.io.File;
import java.io.IOException;

import cn.com.uama.retrofitmanager.cache.LMInternalCache;
import okhttp3.Cache;
import okhttp3.Request;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by liwei on 2018/3/17 16:56
 * Email: liwei@uama.com.cn
 * Description: 缓存相关操作
 */
public class LMCache {

    private final File cacheDir;

    public LMCache(File cacheDir) {
        this.cacheDir = cacheDir;
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    final LMInternalCache internalCache = new LMInternalCache() {
        @Override
        public void put(Request request, String value) {
            LMCache.this.put(request, value);
        }

        @Override
        public String get(Request request) {
            return LMCache.this.get(request);
        }

        @Override
        public boolean remove(Request request) {
            return LMCache.this.remove(request);
        }

        @Override
        public boolean isValid(Request request, long cacheTime) {
            return LMCache.this.isValid(request, cacheTime);
        }

        @Override
        public boolean clear() {
            return LMCache.this.clear();
        }
    };

    /**
     * 判断缓存目录是否有效
     * 目录存在或者目录不存在但能创建成功表示有效，否则表示无效
     */
    private boolean isCacheDirInvalid() {
        return cacheDir == null || !cacheDir.exists() && !cacheDir.mkdirs();
    }

    /**
     * 新增或更新对应请求缓存
     */
    void put(Request request, String value) {
        String requestMethod = request.method();
        // 下面的 if 代码块是从 okhttp3.Cache 中拿过来的
        // 意思是如果这次请求是 POST/PUT 等修改数据的操作
        // 那对应的缓存一定是失效了，需要删除
        if (HttpMethod.invalidatesCache(requestMethod)) {
            remove(request);
        }

        if (!"GET".equals(requestMethod)) {
            // 不缓存非 GET 请求的返回数据
            return;
        }

        File cacheFile = getCacheFileFor(request);
        if (cacheFile == null) return;

        BufferedSink bufferedSink = null;
        try {
            bufferedSink = Okio.buffer(Okio.sink(cacheFile));
            bufferedSink.writeUtf8(value);
        } catch (IOException ignored) {
        } finally {
            if (bufferedSink != null) {
                try {
                    bufferedSink.close();
                } catch (IOException ignored) {
                }
            }
       }
    }

    /**
     *  移除对应请求的缓存
     */
    boolean remove(Request request) {
        File cacheFile = getCacheFileFor(request);
        return cacheFile != null && cacheFile.exists() && cacheFile.delete();
    }

    /**
     * 获取对应请求的缓存
     */
    String get(Request request) {
        File cacheFile = getCacheFileFor(request);
        if (cacheFile != null && cacheFile.exists()) {
            BufferedSource fileBuffer = null;
            try {
                fileBuffer = Okio.buffer(Okio.source(cacheFile));
                return fileBuffer.readUtf8();
            } catch (IOException ignored) {
            } finally {
                if (fileBuffer != null) {
                    try {
                        fileBuffer.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据缓存时间判断对应请求的缓存是否有效
     */
    boolean isValid(Request request, long cacheTime) {
        File cacheFile = getCacheFileFor(request);
        return cacheFile != null
                && cacheFile.exists()
                && cacheFile.lastModified() + cacheTime * 1000 > System.currentTimeMillis();
    }

    /**
     * 获取对应请求的缓存文件对象
     */
    private File getCacheFileFor(Request request) {
        if (request == null) return null;
        if (isCacheDirInvalid()) return null;

        String key = Cache.key(request.url());
        return new File(cacheDir, key + ".lmc");
    }

    /**
     * 清除缓存
     */
    boolean clear() {
        return !isCacheDirInvalid() && deleteDir(cacheDir);
    }

    private boolean deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDir(file);
                }
            }
        }
        return dir.delete();
    }
}
