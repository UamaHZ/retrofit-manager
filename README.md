# Retrofit Manager
[![](https://jitpack.io/v/UamaHZ/retrofit-manager.svg)](https://jitpack.io/#UamaHZ/retrofit-manager)

对 [Retrofit][1] 的使用进行封装，对一些情况进行统一处理，方便复用。

* 统一处理 status 的判断，并在自定义接口中回调
* 提供统一的取消（对应某个 key）所有网络请求的便捷方法

## 主要类介绍

### RetrofitManager

在自定义 application 中调用 `RetrofitManager.init(this)` 进行初始化，自定义 application 继承 `RetrofitProvider` 接口。`RetrofitProvider` 接口定义了三个方法：

```java
public interface RetrofitProvider {
    /**
     * 提供 Retrofit 所需要的 base url
     */
    String provideBaseUrl();

    /**
     * 提供配置项，设置给 okhttp client
     */
    OkHttpConfiguration provideOkHttpConfig();

    /**
     * 提供接口状态拦截器
     */
    ApiStatusInterceptor provideApiStatusInterceptor();
}
```

初始化方法中创建了一个 `Retrofit` 对象。

调用 `RetrofitManager.createService()` 方法创建定义 API 端点 (endpoints) 的 service 接口的实现 (implementation) 。

#### OkHttpConfiguration

这是一个接口，用于配置 OkHttpClient，定义如下：

```java
public interface OkHttpConfiguration {
    List<Interceptor> interceptors();
    int readTimeoutSeconds();
    int writeTimeoutSeconds();
    int connectTimeoutSeconds();
    X509TrustManager trustManager();
}
```

其中，`interceptors()` 方法用于提供自定义拦截器，典型应用就是添加公共参数 header 的拦截器；中间三个方法分别用于配置读取、写入和连接超时时间（单位：秒），默认均为30秒；`trustManager()` 方法用于提供和 HTTPS 证书相关的 `X509TrustManager` 对象。

#### ApiStatusInterceptor

用于拦截接口返回状态的拦截器，方便对某种状态进行统一处理。**注意：这是个自定义接口，和 OkHttp 的 `Interceptor` 没有任何关系。 **接口定义如下：

```java
public interface ApiStatusInterceptor {
    /**
     * 拦截方法
     * @param status 接口状态码
     * @param message 描述信息
     * @return 如果不想数据继续往下走到后面的回调方法，返回 true，否则 false
     */
    boolean intercept(String status, String message);
}
```

intercep 方法的返回值类型为 boolean ，如果返回 true 则表示这次接口访问被拦截，不会继续往下走到 `AdvancedRetrofitCallback` 的 onSuccess 或 onError 方法中。

### AdvancedRetrofitHelper

接口访问的帮助类。

提供 `enqueue` 方法对 `retrofit2.Call<T>.enqueue(retrofit2.Callback<T>)` 的调用进行封装，在 `retrofit2.Callback<T>` 的回调方法中统一处理 status ，并将对应的结果回调到我们的 `AdvancedRetrofitCallback` 接口的回调方法中。

维护了一个 `WeakHashMap` 对象 *（暂时忽略代码实现中的另一个，那是为后面提供 RxJava2 支持做准备）*，用于缓存针对某个 key  (可以是 `context` 或 `fragment`) 的 `call` 对象列表，凡是放到缓存 `map` 中的 `call` 都可以通过 `cancelCalls(Object key)` 方法取消某个 key 下的所有 `call`。初衷是方便在 `activity` 的 `onDestroy` 方法或者 `fragment` 的 `onDestroyView` 方法中取消所有尚未执行完毕的 `call` 。可以通过 `enqueue` 的重载 (overload) 方法选择是否将某个 `call` 放到缓存 `map` 中。

#### AdvancedRetrofitCallback

自定义的回调接口，定义了三个方法：
* `onSuccess` 方法在成功的时候（即 status 为 100 时）回调
* `onError` 方法在不成功的时候进行回调
* `onEnd` 方法不管成功或者失败的时候都会回调，该方法的初衷是方便取消 loading 对话框的显示

`SimpleRetrofitCallback` 是 `AdvancedRetrofitCallback` 接口的实现类，三个方法均为空实现，方便根据需要重写 (override) 方法。

## 用法示例

一个简单的参考示例写在了 sample 模块中。

## 注意

**bean 包下定义了一些通用的实体类。在自己的项目中定义接口访问的实体类时，一定要继承 `BaseResp` ，推荐使用 `SimpleResp` ，`SimpleListResp` 和 `SimplePagedListResp` 。详情参考类的实现。**

[1]: https://github.com/square/retrofit
