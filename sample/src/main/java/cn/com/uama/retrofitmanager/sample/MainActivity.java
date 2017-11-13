package cn.com.uama.retrofitmanager.sample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import cn.com.uama.retrofitmanager.AdvancedRetrofitHelper;
import cn.com.uama.retrofitmanager.RetrofitManager;
import cn.com.uama.retrofitmanager.SimpleRetrofitCallback;
import cn.com.uama.retrofitmanager.bean.SimpleResp;
import cn.com.uama.retrofitmanager.rx.ErrorConsumer;
import cn.com.uama.retrofitmanager.sample.bean.UpdateBean;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private TextView infoView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitManager.createService(ApiService.class);
        infoView = findViewById(R.id.info_view);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在获取数据...");
    }

    /**
     * 普通方式访问接口
     */
    private void checkNewVersion() {
        progressDialog.show();
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
                        progressDialog.dismiss();
                    }
                });
    }

    /**
     * 通过 RxJava 访问接口
     */
    private void checkNewVersionRx() {
        progressDialog.show();
        apiService.checkNewVersionRx("android-wuguan")
                .compose(AdvancedRetrofitHelper.<SimpleResp<UpdateBean>>rxObservableTransformer(this))
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        progressDialog.dismiss();
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
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        checkNewVersion();
        checkNewVersionRx();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 仅取消普通方式接口访问
        //  AdvancedRetrofitHelper.cancelCalls(this);

        // 仅取消 RxJava 接口访问
        // AdvancedRetrofitHelper.disposeDisposables(this);

        // 把普通方式和 RxJava 形式的接口访问都取消
        AdvancedRetrofitHelper.releaseResourcesFor(this);
    }
}
