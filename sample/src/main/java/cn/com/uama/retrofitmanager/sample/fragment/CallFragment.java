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
import android.widget.Toast;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.ErrorStatus;
import cn.com.uama.retrofitmanager.RetrofitManager;
import cn.com.uama.retrofitmanager.SimpleRetrofitCallback;
import cn.com.uama.retrofitmanager.bean.BaseResp;
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
public class CallFragment extends BaseFragment {

    private static final String TAG = "CallFragment";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleView.setText("普通方式访问接口：");
    }

    /**
     * 普通方式访问接口
     */
    void checkNewVersion() {
        progressBar.setVisibility(View.VISIBLE);
        AdvancedRetrofitHelper.enqueue(this, apiService.checkNewVersion("android-wuguan"),
                new SimpleRetrofitCallback<SimpleResp<UpdateBean>>() {
                    @Override
                    public void onSuccess(Call<SimpleResp<UpdateBean>> call, SimpleResp<UpdateBean> resp) {
                        boolean fromCache = resp.isFromCache();
                        UpdateBean updateBean = resp.getData();
                        if (updateBean != null) {
                            infoView.setText((fromCache ? "来自缓存：" : "来自接口：") + updateBean.getContent());
                        } else {
                            infoView.setText("update bean 为 null");
                        }
                    }

                    @Override
                    public void onError(Call<SimpleResp<UpdateBean>> call, String errorCode, String msg) {
                        /*
                        if (TextUtils.isEmpty(msg)) {
                            infoView.setText(errorCode + ":获取数据失败");
                        } else {
                            infoView.setText(errorCode + ":" + msg);
                        }
                        */
                    }

                    @Override
                    public void onError(Call<SimpleResp<UpdateBean>> call, BaseResp baseResp) {
                        String status = baseResp.getStatus();
                        if (ErrorStatus.NETWORK_UNAVAILABLE.equals(status)) {
//                            infoView.setText("没有网络");
                        } else {
                            String msg = baseResp.getMsg();
                            if (TextUtils.isEmpty(msg)) {
                                msg = "获取数据失败";
                            }
                            infoView.setText(String.format("%s:%s", status, msg));
                        }
                    }

                    @Override
                    public void onIntercepted(Call<SimpleResp<UpdateBean>> call, BaseResp resp) {
                        // 需要注意唯一能保证的是 status 不为空
                        String status = resp.getStatus();
                        if (status.equals("100")) {
                            try {
                                SimpleResp<UpdateBean> realResp = (SimpleResp<UpdateBean>) resp;
                                UpdateBean data = realResp.getData();
                                if (data != null) {
                                    infoView.setText("数据被劫持了：" + data.getContent());
                                }
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }
                        }
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
}
