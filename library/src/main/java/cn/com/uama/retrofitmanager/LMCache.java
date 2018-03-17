package cn.com.uama.retrofitmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Request;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

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

    /**
     * 判断缓存目录是否有效
     * 目录存在或者目录不存在但能创建成功表示有效，否则表示无效
     */
    private boolean isCacheDirInvalid() {
        if (cacheDir == null) return true;
        return !cacheDir.exists() && !cacheDir.mkdirs();
    }

    /**
     * 新增或更新对应请求缓存
     */
    public void put(Request request, String value) {
        if (request == null) return;
        if (isCacheDirInvalid()) return;
        String key = Cache.key(request.url());
        File cacheFile = new File(cacheDir, key + ".lmc");
        BufferedSink bufferedSink = null;
        try {
            Sink sink = Okio.sink(cacheFile);
            bufferedSink = Okio.buffer(sink);
            bufferedSink.writeUtf8(value);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedSink != null) {
                try {
                    bufferedSink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
       }
    }

    /**
     *  移除对应请求的缓存
     */
    public boolean remove(Request request) {
        if (request == null) return false;
        if (isCacheDirInvalid()) return false;
        String key = Cache.key(request.url());
        File cacheFile = new File(cacheDir, key + ".tmp");
        if (cacheDir.exists()) {
            return cacheFile.delete();
        }
        return false;
    }

    /**
     * 获取对应请求的缓存
     */
    public String get(Request request) {
        if (request == null) return null;
        if (isCacheDirInvalid()) return null;
        String key = Cache.key(request.url());
        File cacheFile = new File(cacheDir, key + ".tmp");
        if (cacheFile.exists()) {
            try {
                BufferedSource fileBuffer = Okio.buffer(Okio.source(cacheFile));
                return fileBuffer.readUtf8();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 根据缓存时间判断对应请求的缓存是否有效
     */
    public boolean isValid(Request request, long cacheTime) {
        if (request == null) return false;
        String key = Cache.key(request.url());
        File cacheFile = new File(cacheDir, key + ".tmp");
        return cacheFile.exists() &&
                cacheFile.lastModified() + cacheTime * 1000 > System.currentTimeMillis();
    }

    /**
     * 清除缓存
     */
    public boolean clear() {
        if (isCacheDirInvalid()) return false;
        return deleteDir(cacheDir);
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
