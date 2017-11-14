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
import cn.com.uama.retrofitmanager.RetrofitManager;
import cn.com.uama.retrofitmanager.SimpleRetrofitCallback;
import cn.com.uama.retrofitmanager.bean.SimpleResp;
import cn.com.uama.retrofitmanager.sample.ApiService;
import cn.com.uama.retrofitmanager.sample.R;
import cn.com.uama.retrofitmanager.sample.bean.UpdateBean;
import retrofit2.Call;

/**
 * Created by liwei on 2017/11/14 10:23
 * Email: liwei@uama.com.cn
 * Description: 使用普通 Call 访问接口的示例
 */
public class CallFragment extends Fragment {

    private ApiService apiService;
    private TextView infoView;
    private TextView titleView;

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
                checkNewVersion();
            }
        });
        titleView = view.findViewById(R.id.title_view);
        titleView.setText("普通方式访问接口：");

        apiService = RetrofitManager.createService(ApiService.class);
        checkNewVersion();
    }

    /**
     * 普通方式访问接口
     */
    private void checkNewVersion() {
        progressBar.setVisibility(View.VISIBLE);
        AdvancedRetrofitHelper.enqueue(this, apiService.checkNewVersion("android-wuguan"),
                new SimpleRetrofitCallback<SimpleResp<UpdateBean>>() {
                    @Override
                    public void onSuccess(Call<SimpleResp<UpdateBean>> call, SimpleResp<UpdateBean> resp) {
                        UpdateBean updateBean = resp.getData();
                        if (updateBean != null) {
                            infoView.setText(updateBean.getContent());
                        } else {
                            infoView.setText("update bean 为 null");
                        }
                    }

                    @Override
                    public void onError(Call<SimpleResp<UpdateBean>> call, String errorCode, String msg) {
                        if (TextUtils.isEmpty(msg)) {
                            infoView.setText(errorCode + ":获取数据失败");
                        } else {
                            infoView.setText(errorCode + ":" + msg);
                        }
                    }

                    @Override
                    public void onIntercepted(Call<SimpleResp<UpdateBean>> call, SimpleResp<UpdateBean> resp) {
                        infoView.setText("数据被劫持了！");
                    }

                    @Override
                    public void onEnd(Call<SimpleResp<UpdateBean>> call) {
                        progressBar.setVisibility(View.GONE);
                        if (!infoView.isShown()) {
                            infoView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 仅取消普通方式接口访问
        AdvancedRetrofitHelper.cancelCalls(this);

        // 把普通方式和 RxJava 形式的接口访问都取消
        // AdvancedRetrofitHelper.releaseResourcesFor(this);
    }
}
