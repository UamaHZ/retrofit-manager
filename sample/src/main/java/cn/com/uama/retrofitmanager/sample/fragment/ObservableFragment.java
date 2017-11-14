package cn.com.uama.retrofitmanager.sample.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
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
public class ObservableFragment extends Fragment {

    private static final String TAG = "ObservableFragment";

    private ApiService apiService;
    private TextView infoView;

    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        infoView = view.findViewById(R.id.info_view);
        progressBar = view.findViewById(R.id.progressBar);
        view.findViewById(R.id.button_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNewVersionRx();
            }
        });
        TextView titleView = view.findViewById(R.id.title_view);
        titleView.setText("RxJava 方式访问接口：");

        apiService = RetrofitManager.createService(ApiService.class);
        checkNewVersionRx();
    }

    /**
     * 通过 RxJava 访问接口
     */
    private void checkNewVersionRx() {
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
                        UpdateBean updateBean = resp.getData();
                        if (updateBean != null) {
                            infoView.setText(updateBean.getContent());
                        } else {
                            infoView.setText("update bean 为 null");
                        }
                    }
                }, new ErrorConsumer() {
                    @Override
                    public void onError(@NonNull String code, @Nullable String msg) {
                        if (TextUtils.isEmpty(msg)) {
                            msg = "获取数据失败";
                        }
                        infoView.setText(String.format("%s:%s", code, msg));
                    }

                    @Override
                    public void onIntercepted(BaseResp result) {
                        infoView.setText("数据被劫持了!");
                        try {
                            SimpleResp<UpdateBean> resp = (SimpleResp<UpdateBean>) result;
                            UpdateBean data = resp.getData();
                            if (data != null) {
                                Log.d(TAG, "被劫持的数据为：" + data.getContent());
                            }
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 仅取消 RxJava 接口访问
        AdvancedRetrofitHelper.disposeDisposables(this);

        // 把普通方式和 RxJava 形式的接口访问都取消
        // AdvancedRetrofitHelper.releaseResourcesFor(this);
    }
}
