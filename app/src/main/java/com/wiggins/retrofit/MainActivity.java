package com.wiggins.retrofit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wiggins.retrofit.adapter.TodayHistoryQueryAdapter;
import com.wiggins.retrofit.base.BaseActivity;
import com.wiggins.retrofit.bean.TodayHistoryQuery;
import com.wiggins.retrofit.entity.CommonList;
import com.wiggins.retrofit.entity.FromJsonUtils;
import com.wiggins.retrofit.http.HttpApi;
import com.wiggins.retrofit.http.HttpUtil;
import com.wiggins.retrofit.http.RxUtils;
import com.wiggins.retrofit.http.progress.DownloadProgressHandler;
import com.wiggins.retrofit.http.progress.ProgressHelper;
import com.wiggins.retrofit.http.progress.UploadProgressHandler;
import com.wiggins.retrofit.utils.Constant;
import com.wiggins.retrofit.utils.DialogUtil;
import com.wiggins.retrofit.utils.LogUtil;
import com.wiggins.retrofit.utils.StringUtil;
import com.wiggins.retrofit.utils.ToastUtil;
import com.wiggins.retrofit.utils.UIUtils;
import com.wiggins.retrofit.view.TodayHistoryDetailActivity;
import com.wiggins.retrofit.widget.TitleView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @Description Retrofit2.0使用详解
 * @Author 一花一世界
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private MainActivity mActivity = null;
    private TitleView titleView;
    private EditText mEdtData;
    private Button mBtnQuery;
    private TextView mTvEmpty;
    private ListView mLvData;

    private List<TodayHistoryQuery> todayHistoryQuery;
    private TodayHistoryQueryAdapter todayHistoryQueryAdapter;
    private Gson gson;
    private String data = "";
    private HttpApi mHttpApi;
    private CompositeSubscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        initView();
        initData();
        setLinstener();
    }

    private void initView() {
        titleView = (TitleView) findViewById(R.id.titleView);
        titleView.setAppTitle(UIUtils.getString(R.string.event_list));
        titleView.setLeftImageVisibility(View.GONE);
        mEdtData = (EditText) findViewById(R.id.edt_data);
        mBtnQuery = (Button) findViewById(R.id.btn_query);
        mTvEmpty = (TextView) findViewById(R.id.tv_empty);
        mLvData = (ListView) findViewById(R.id.lv_data);
        mLvData.setEmptyView(mTvEmpty);
    }

    private void initData() {
        if (gson == null) {
            gson = new Gson();
        }
        if (todayHistoryQuery == null) {
            todayHistoryQuery = new ArrayList<>();
        }
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        if (todayHistoryQueryAdapter == null) {
            todayHistoryQueryAdapter = new TodayHistoryQueryAdapter(todayHistoryQuery, mActivity);
            mLvData.setAdapter(todayHistoryQueryAdapter);
        } else {
            todayHistoryQueryAdapter.notifyDataSetChanged();
        }
        mHttpApi = HttpUtil.getService(HttpApi.class);
        mSubscription = RxUtils.getCompositeSubscription(mSubscription);
    }

    private void setLinstener() {
        mBtnQuery.setOnClickListener(this);
        mLvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(mActivity, TodayHistoryDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("e_id", String.valueOf(todayHistoryQuery.get(position).getE_id()));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    /**
     * 历史上的今天 事件列表
     */
    private void getTodayHistoryQuery() {
        DialogUtil.showDialogLoading(mActivity, "");
        Map<String, String> mMap = new HashMap<>();
        mMap.put("key", Constant.APP_KEY);
        mMap.put("date", data);

        Call<ResponseBody> call = mHttpApi.enqueueGet(Constant.queryEvent, mMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                DialogUtil.hideDialogLoading();
                todayHistoryQuery.clear();
                try {
                    String result = response.body().string();
                    LogUtil.e("返回数据" + result);
                    CommonList<TodayHistoryQuery> data = new FromJsonUtils(TodayHistoryQuery.class, result).fromJsonList();
                    LogUtil.e("解析数据" + data);

                    if (data.getError_code() == 0) {
                        todayHistoryQuery = data.getResult();
                        todayHistoryQueryAdapter.setData(todayHistoryQuery);
                        LogUtil.e(Constant.LOG_TAG, "历史上的今天 - 事件列表:" + todayHistoryQuery.toString());
                    } else {
                        todayHistoryQueryAdapter.setData(todayHistoryQuery);
                        ToastUtil.showText(data.getReason());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DialogUtil.hideDialogLoading();
                ToastUtil.showText(t.getMessage());
            }
        });
    }

    /**
     * Retrofit + RxJava
     */
    private void requestByRxJavaQueryEvent() {
        DialogUtil.showDialogLoading(mActivity, "");
        Map<String, String> mMap = new HashMap<>();
        mMap.put("key", Constant.APP_KEY);
        mMap.put("date", data);

        mSubscription.add(
                mHttpApi.requestByRxJavaGet(Constant.queryEvent, mMap)
                        //设置事件触发在非主线程
                        .subscribeOn(Schedulers.io())
                        //设置事件接受在UI线程以达到UI显示的目的
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                DialogUtil.hideDialogLoading();
                                ToastUtil.showText(e.getMessage());
                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                DialogUtil.hideDialogLoading();
                                todayHistoryQuery.clear();
                                try {
                                    String result = responseBody.string();
                                    LogUtil.e("返回数据" + result);
                                    CommonList<TodayHistoryQuery> data = new FromJsonUtils(TodayHistoryQuery.class, result).fromJsonList();
                                    LogUtil.e("解析数据" + data);

                                    if (data.getError_code() == 0) {
                                        todayHistoryQuery = data.getResult();
                                        todayHistoryQueryAdapter.setData(todayHistoryQuery);
                                        LogUtil.e(Constant.LOG_TAG, "历史上的今天 - 事件列表:" + todayHistoryQuery.toString());
                                    } else {
                                        todayHistoryQueryAdapter.setData(todayHistoryQuery);
                                        ToastUtil.showText(data.getReason());
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }));
    }

    /**
     * 下载文件
     */
    private void requestByDownload() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressNumberFormat("%1d KB/%2d KB");
        dialog.setTitle("下载");
        dialog.setMessage("正在下载，请稍后...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();

        ProgressHelper.setProgressHandler(new DownloadProgressHandler() {
            @Override
            protected void onProgress(long bytesRead, long contentLength, boolean done) {
                LogUtil.e("是否在主线程中运行: " + String.valueOf(Looper.getMainLooper() == Looper.myLooper()));
                LogUtil.e("onProgress: " + String.format("%d%% \n", (100 * bytesRead) / contentLength));
                LogUtil.e("done: " + String.valueOf(done));
                dialog.setMax((int) (contentLength / 1024));
                dialog.setProgress((int) (bytesRead / 1024));

                if (done) {
                    dialog.dismiss();
                }
            }
        });

        HttpApi mDownLoadHttpApi = HttpUtil.getDownLoadService(HttpApi.class);
        Call<ResponseBody> call = mDownLoadHttpApi.requestByDownload(Constant.mobileSafe);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    InputStream is = null;
                    FileOutputStream fos = null;
                    BufferedInputStream bis = null;
                    try {
                        //获取存储文件夹
                        String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RetrofitDownload/";
                        File file = new File(dirName);
                        //如果目录不存在则创建
                        if (!file.exists()) {
                            file.mkdir();
                        }

                        File fileName = new File(dirName + "mobileSafe.apk");
                        if (fileName.exists()) {
                            fileName.delete();
                            fileName.createNewFile();
                        } else {
                            fileName.createNewFile();
                        }

                        is = response.body().byteStream();
                        fos = new FileOutputStream(fileName);
                        bis = new BufferedInputStream(is);

                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = bis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                            fos.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ToastUtil.showText(t.getMessage());
            }
        });
    }

    /**
     * 上传文件
     */
    private void requestByUpload() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressNumberFormat("%1d KB/%2d KB");
        dialog.setTitle("上传");
        dialog.setMessage("正在上传，请稍后...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();

        ProgressHelper.setProgressHandler(new UploadProgressHandler() {
            @Override
            protected void onProgress(long bytesRead, long contentLength, boolean done) {
                LogUtil.e("是否在主线程中运行: " + String.valueOf(Looper.getMainLooper() == Looper.myLooper()));
                LogUtil.e("onProgress: " + String.format("%d%% \n", (100 * bytesRead) / contentLength));
                LogUtil.e("done: " + String.valueOf(done));
                dialog.setMax((int) (contentLength / 1024));
                dialog.setProgress((int) (bytesRead / 1024));

                if (done) {
                    dialog.dismiss();
                }
            }
        });

        HttpApi mUpLoadHttpApi = HttpUtil.getUpLoadService(HttpApi.class);
        Map<String, RequestBody> params = new HashMap<>();
        Call<ResponseBody> call = mUpLoadHttpApi.requestByUpload("", params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ToastUtil.showText(t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        RxUtils.unSubscriptionIfNotNull(mSubscription);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_query:
                data = mEdtData.getText().toString().trim();
                if (StringUtil.isEmpty(data)) {
                    ToastUtil.showText(UIUtils.getString(R.string.query_date_not_empty));
                    return;
                }
                getTodayHistoryQuery();
                //requestByRxJavaQueryEvent();
                //requestByDownload();
                //requestByUpload();
                break;
        }
    }
}
