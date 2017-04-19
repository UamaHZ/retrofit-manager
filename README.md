# Retrofit Manager

对 [Retrofit][1] 的使用进行封装，对一些情况进行统一处理，方便复用。

* 统一处理 status 的判断，并在自定义接口中回调
* 提供统一的取消（对应某个 `context` 或 `fragment` 的）所有网络请求的便捷方法

## 主要类介绍

### RetrofitManager

在自定义 application 中调用 `RetrofitManager.init(this)` 进行初始化，自定义 application 继承 `RetrofitProvider` 接口。`RetrofitProvider` 接口定义了两个方法：

```java
public interface RetrofitProvider {
    /**
     * 提供 Retrofit 所需要的 base url
     */
    String provideBaseUrl();

    /**
     * 提供一些需要的 interceptor，设置给 okhttp client
     */
    List<Interceptor> provideInterceptors();
}
```

初始化方法中创建了一个 `Retrofit` 对象。

调用 `RetrofitManager.createService()` 方法创建定义 API 端点 (endpoints) 的 service 接口的实现 (implementation) 。

### AdvancedRetrofitCallback

自定义的回调接口，定义了四个方法：
* `onSuccess` 方法在成功的时候（即 status 为 100 时）回调
* `onTokenExpired` 方法在 token 失效的时候（即 status 为 102 时）回调
* `onError` 方法在不成功的时候进行回调
* `onEnd` 方法不管成功或者失败或者 token 失效的时候都会回调，该方法的初衷是方便取消 loading 对话框的显示

`SimpleRetrofitCallback` 是 `AdvancedRetrofitCallback` 接口的实现类，四个方法均为空实现，方便根据需要重写 (override) 方法。

建议引用该 library 的项目继承 `SimpleRetrofitCallback` 类自定义自己的 callback 类，重写 (override) `onTokenExpired` 方法，对 token 失效的情况进行统一处理，这也是 `onTokenExpired` 回调方法的初衷。比如，我在绿城智慧管理项目中定义了如下 callback 类：

```java
public class TERetrofitCallback<T> extends SimpleRetrofitCallback<T> {
    @Override
    public void onTokenExpired(Context context, String message) {
        if (!TextUtils.isEmpty(message)) {
            CustomToast.makeLongToast(context, message);
        } else {
            CustomToast.makeLongToast(context, "账号已失效,请重新登录");
        }
        Utils.returnSign(context);
    }
}
```

### AdvancedRetrofitHelper

提供 `enqueue` 方法对 `retrofit2.Call<T>.enqueue(retrofit2.Callback<T>)` 的调用进行封装，在 `retrofit2.Callback<T>` 的回调方法中统一处理 status ，并将对应的结果回调到我们的 `AdvancedRetrofitCallback` 接口的回调方法中。

维护了两个 `WeakHashMap` 对象，用于缓存针对某个 `context` 或 `fragment` 的 `call` 对象列表，凡是放到缓存 `map` 中的 `call` 都可以通过 `cancelCalls()` 方法取消某个 `context` 或 `fragment` 下的所有 `call`。初衷是方便在 `activity` 的 `onDestroy` 方法或者 `fragment` 的 `onDestroyView` 方法中取消所有尚未执行完毕的 `call` 。可以通过 `enqueue` 的重载 (overload) 方法选择是否将某个 `call` 放到缓存 `map` 中。

## 用法示例

定义一个 [Retrofit][1] 的 API service 接口：

```java
public interface MainService {
    @FormUrlEncoded
    @POST(UrlConstant.LOGIN)
    Call<SimpleResp<SignInfoBean>> login(@Field("username") String username, @Field("password") String password);
}
```

进行接口调用：

```java
AdvancedRetrofitHelper.enqueue(mContext,
				RetrofitManager.createService(MainService.class).login(username, password),
				new TERetrofitCallback<SimpleResp<SignInfoBean>>() {
					@Override
					public void onSuccess(Call<SimpleResp<SignInfoBean>> call, SimpleResp<SignInfoBean> resp) {
						...
					}

					@Override
					public void onError(Call<SimpleResp<SignInfoBean>> call, String error) {
						...
					}

					@Override
					public void onEnd() {
						DialogManager.dismiss();
					}
				});
```

取消接口访问：

```java
@Override
public void onDestroy() {
    super.onDestroy();
    AdvancedRetrofitHelper.cancelCalls(this);
}
```

## 注意

**bean 包下定义了一些通用的实体类。在自己的项目中定义接口访问的实体类时，一定要继承 `BaseResp` ，推荐使用 `SimpleResp` ，`SimpleListResp` 和 `SimplePagedListResp` 。详情参考类的实现。**

## TODO

- [x] 在 Fragment 的 onDestroyView 方法中调用 AdvancedRetrofitHelper.cancelCalls(Context) 方法，有可能将同一个 Activity 下的正在显示界面的 Fragment 的 call 取消

[1]: https://github.com/square/retrofit
