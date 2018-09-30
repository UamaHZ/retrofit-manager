package cn.com.uama.retrofitmanager.sample.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.RetrofitManager;
import cn.com.uama.retrofitmanager.sample.ApiService;
import cn.com.uama.retrofitmanager.sample.R;

public class BaseFragment extends Fragment {

    ApiService apiService;
    TextView infoView;
    TextView titleView;

    ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleView = view.findViewById(R.id.title_view);
        infoView = view.findViewById(R.id.info_view);
        progressBar = view.findViewById(R.id.progressBar);
        view.findViewById(R.id.button_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNewVersion();
            }
        });
        view.findViewById(R.id.button_clear_cache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 同步清除缓存
                // RetrofitManager.clearCache();
                // 异步清除缓存
                RetrofitManager.clearCacheAsync(new RetrofitManager.ClearCacheCallback() {
                    @Override
                    public void onComplete(boolean result) {
                        String resultStr = result ? "缓存清除成功" : "缓存清除失败";
                        Toast.makeText(getContext(), resultStr, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(getContext(), "发生错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        view.findViewById(R.id.button_clear_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitManager.setCacheId(null);
            }
        });
        view.findViewById(R.id.button_set_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitManager.setCacheId("44");
            }
        });
        titleView = view.findViewById(R.id.title_view);

        apiService = RetrofitManager.createService(ApiService.class);
        checkNewVersion();
    }

    void checkNewVersion() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 仅取消普通方式接口访问
        // AdvancedRetrofitHelper.cancelCalls(this);

        // 把普通方式和 RxJava 形式的接口访问都取消
        AdvancedRetrofitHelper.releaseResourcesFor(this);
    }
}
