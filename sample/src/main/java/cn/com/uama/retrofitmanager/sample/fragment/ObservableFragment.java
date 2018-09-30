package cn.com.uama.retrofitmanager.sample.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.ErrorStatus;
import cn.com.uama.retrofitmanager.RetrofitManager;
import cn.com.uama.retrofitmanager.bean.BaseResp;
import cn.com.uama.retrofitmanager.bean.SimpleResp;
import cn.com.uama.retrofitmanager.rx.ErrorConsumer;
import cn.com.uama.retrofitmanager.sample.ApiService;
import cn.com.uama.retrofitmanager.sample.R;
import cn.com.uama.retrofitmanager.sample.bean.UpdateBean;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by liwei on 2017/11/14 10:49
 * Email: liwei@uama.com.cn
 * Description: 使用 RxJava 访问接口的示例
 */
public class ObservableFragment extends BaseFragment {

    private static final String TAG = "ObservableFragment";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleView.setText("RxJava 方式访问接口：");
    }

    /**
     * 通过 RxJava 访问接口
     */
    void checkNewVersion() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.checkNewVersionRx("android-wuguan")
                .compose(AdvancedRetrofitHelper.<SimpleResp<UpdateBean>>rxObservableTransformer(this))
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        progressBar.setVisibility(View.GONE);
                        if (!infoView.isShown()) {
                            infoView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .subscribe(new Consumer<SimpleResp<UpdateBean>>() {
                    @Override
                    public void accept(SimpleResp<UpdateBean> resp) throws Exception {
                        boolean fromCache = resp.isFromCache();
                        UpdateBean updateBean = resp.getData();
                        if (updateBean != null) {
                            infoView.setText((fromCache ? "来自缓存：" : "来自接口：") + updateBean.getContent());
                        } else {
                            infoView.setText("update bean 为 null");
                        }
                    }
                }, new ErrorConsumer() {
                    @Override
                    public void onError(BaseResp resp) {
                        String status = resp.getStatus();
                        if (ErrorStatus.NETWORK_UNAVAILABLE.equals(status)) {
                            infoView.setText("没有网络");
                        } else {
                            String msg = resp.getMsg();
                            if (TextUtils.isEmpty(msg)) {
                                msg = "获取数据失败";
                            }
                            infoView.setText(String.format("%s:%s", status, msg));
                        }
                    }

                    @Override
                    public void onIntercepted(BaseResp result) {
                        infoView.setText("数据被劫持了!");
                        // 需要注意唯一能保证的是 status 不为空
                        String status = result.getStatus();
                        if (status.equals("100")) {
                            try {
                                SimpleResp<UpdateBean> realResp = (SimpleResp<UpdateBean>) result;
                                UpdateBean data = realResp.getData();
                                if (data != null) {
                                    infoView.setText("数据被劫持了：" + data.getContent());
                                }
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
}
