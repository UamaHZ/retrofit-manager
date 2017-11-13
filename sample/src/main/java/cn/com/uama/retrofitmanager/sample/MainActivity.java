package cn.com.uama.retrofitmanager.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.RetrofitManager;
import cn.com.uama.retrofitmanager.SimpleRetrofitCallback;
import cn.com.uama.retrofitmanager.bean.SimpleResp;
import cn.com.uama.retrofitmanager.sample.bean.UpdateBean;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private TextView infoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitManager.createService(ApiService.class);
        infoView = findViewById(R.id.info_view);
    }

    private void checkNewVersion() {
        infoView.setText("正在获取数据...");
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
                    public void onEnd(Call<SimpleResp<UpdateBean>> call) {
                        infoView.setText("end");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNewVersion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AdvancedRetrofitHelper.cancelCalls(this);
    }
}
