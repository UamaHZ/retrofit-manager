# Change Log

## Version 1.0

*2017-11-21*

* **完成对 RxJava2 的支持。**
* `AdvancedRetrofitCallback` 新增 `onIntercepted(Call<T> call, T resp)` 方法，用于在接口返回数据被“劫持”时进行回调。
* 将 `AdvancedRetrofitCallback#onEnd(Call<T> call)` 回调放到所有其他回调方法后执行。
* `PageResult` 增加 `maxSize` 字段。

## Version 0.4

*2017-10-20*

* 新增 `ApiStatusInterceptor` ，使用户有机会对某个接口返回状态进行统一处理。
* 新增 sample module，进行简单的示例。
* 新增 HTTPS 配置帮助类 `HttpsHelper` 。
* OkHttp 版本升级到 3.9.0 ，RxJava2 版本升级到 2.1.2 。
* 将 AdvancedRetrofitCallback 接口的 onError 回调方法的返回值类型改为 void 。

## Version 0.3

*2017-07-14*

OkhttpConfiguration 接口中增加 `X509TrustManager trustManager();` 方法，用于配置 HTTPS 证书。

## Version 0.2

*2017-06-23*

* 增加 com.github.UamaHZ:retrofit-gson-converter:0.1 依赖，用于解决项目中接口报错时 data 字段数据类型和约定不一致造成的 gson 解析报错的问题。
* 升级 Retrofit 版本到 2.3.0 ，升级 Okhttp 版本到 3.8.1 。

## Version 0.1
Initial release.
