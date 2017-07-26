package com.wiggins.retrofit.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wiggins.retrofit.R;
import com.wiggins.retrofit.adapter.QueryDetailPicAdapter;
import com.wiggins.retrofit.base.BaseActivity;
import com.wiggins.retrofit.bean.QueryDetailPicUrl;
import com.wiggins.retrofit.bean.TodayHistoryQueryDetail;
import com.wiggins.retrofit.entity.CommonList;
import com.wiggins.retrofit.entity.FromJsonUtils;
import com.wiggins.retrofit.http.HttpApi;
import com.wiggins.retrofit.http.HttpUtil;
import com.wiggins.retrofit.utils.Constant;
import com.wiggins.retrofit.utils.DialogUtil;
import com.wiggins.retrofit.utils.LogUtil;
import com.wiggins.retrofit.utils.ToastUtil;
import com.wiggins.retrofit.utils.UIUtils;
import com.wiggins.retrofit.widget.TitleView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Description 事件列表详情
 * @Author 一花一世界
 */
public class TodayHistoryDetailActivity extends BaseActivity {

    private TodayHistoryDetailActivity mActivity = null;
    private TitleView titleView;
    private TextView mTvDetailTitle;
    private TextView mTvDetailContent;
    private TextView mTvEmpty;
    private ListView mLvData;

    private List<TodayHistoryQueryDetail> todayHistoryQueryDetail;
    private QueryDetailPicAdapter queryDetailPicAdapter;
    private Gson gson = null;
    private String e_id = "";
    private HttpApi mHttpApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_history_detail);
        mActivity = this;

        initView();
        initData();
    }

    private void initView() {
        titleView = (TitleView) findViewById(R.id.titleView);
        titleView.setAppTitle(UIUtils.getString(R.string.event_list_details));
        titleView.setLeftImgOnClickListener();
        mTvDetailTitle = (TextView) findViewById(R.id.tv_detail_title);
        mTvDetailContent = (TextView) findViewById(R.id.tv_detail_content);
        mTvDetailContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTvEmpty = (TextView) findViewById(R.id.tv_empty);
        mLvData = (ListView) findViewById(R.id.lv_data);
        mLvData.setEmptyView(mTvEmpty);
    }

    private void initData() {
        if (gson == null) {
            gson = new Gson();
        }
        if (todayHistoryQueryDetail == null) {
            todayHistoryQueryDetail = new ArrayList<>();
        }
        if (queryDetailPicAdapter == null) {
            queryDetailPicAdapter = new QueryDetailPicAdapter(todayHistoryQueryDetail.size() > 0 ? todayHistoryQueryDetail.get(0).getPicUrl() : new ArrayList<QueryDetailPicUrl>(), mActivity);
            mLvData.setAdapter(queryDetailPicAdapter);
        } else {
            queryDetailPicAdapter.notifyDataSetChanged();
        }

        mHttpApi = HttpUtil.getService(HttpApi.class);

        Intent intent = getIntent();
        if (intent != null) {
            e_id = intent.getStringExtra("e_id");
        }

        getTodayHistoryQueryDetail();
    }

    /**
     * @Description 历史上的今天 事件列表详情
     */
    private void getTodayHistoryQueryDetail() {
        DialogUtil.showDialogLoading(mActivity, "");
        Map<String, String> mMap = new HashMap<>();
        mMap.put("key", Constant.APP_KEY);
        mMap.put("e_id", e_id);

        Call<ResponseBody> call = mHttpApi.enqueuePost(Constant.queryDetail, mMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                DialogUtil.hideDialogLoading();
                try {
                    String result = response.body().string();
                    LogUtil.e("返回数据" + result);
                    CommonList<TodayHistoryQueryDetail> data = new FromJsonUtils(TodayHistoryQueryDetail.class, result).fromJsonList();
                    LogUtil.e("解析数据" + data);

                    if (data.getError_code() == 0) {
                        todayHistoryQueryDetail = data.getResult();
                        mTvDetailTitle.setText(todayHistoryQueryDetail.get(0).getTitle().trim());
                        mTvDetailContent.setText(todayHistoryQueryDetail.get(0).getContent().trim());
                        queryDetailPicAdapter.setData(todayHistoryQueryDetail.get(0).getPicUrl());

                        Log.e(Constant.LOG_TAG, "历史上的今天 - 事件详情:" + todayHistoryQueryDetail.toString());
                        Log.e(Constant.LOG_TAG, "历史上的今天 - 事件详情 - 图片详情:" + todayHistoryQueryDetail.get(0).getPicUrl().toString());
                    } else {
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
}
