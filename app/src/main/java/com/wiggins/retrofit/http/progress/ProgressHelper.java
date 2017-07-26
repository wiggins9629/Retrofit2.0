package com.wiggins.retrofit.http.progress;

import com.wiggins.retrofit.utils.LogUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Description 上传或下载进度帮助类
 * @Author 一花一世界
 */
public class ProgressHelper {

    private static ProgressBean progressBean = new ProgressBean();
    private static ProgressHandler mProgressHandler;

    public static void setProgressHandler(ProgressHandler progressHandler) {
        mProgressHandler = progressHandler;
    }

    /**
     * 包装OkHttpClient，用于下载文件的回调
     *
     * @return 包装后的OkHttpClient
     */
    public static OkHttpClient addProgressDownLoadBuilder(OkHttpClient.Builder builder) {
        if (builder == null) {
            builder = new OkHttpClient.Builder();
        }

        //进度回调接口
        final ProgressListener progressListener = new ProgressListener() {
            // 该方法在子线程中运行
            @Override
            public void onProgress(long progress, long total, boolean done) {
                LogUtil.e("progress: " + String.format("%d%% \n", (100 * progress) / total));
                if (mProgressHandler == null) {
                    return;
                }

                progressBean.setBytesRead(progress);
                progressBean.setContentLength(total);
                progressBean.setDone(done);
                mProgressHandler.sendMessage(progressBean);
            }
        };

        //增加拦截器
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //拦截
                Response originalResponse = chain.proceed(chain.request());
                //包装响应体并返回
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        });
        return builder.build();
    }

    /**
     * 包装OkHttpClient，用于上传文件的回调
     *
     * @return 包装后的OkHttpClient
     */
    public static OkHttpClient addProgressUpLoadBuilder(OkHttpClient.Builder builder) {
        if (builder == null) {
            builder = new OkHttpClient.Builder();
        }

        //进度回调接口
        final ProgressListener progressListener = new ProgressListener() {
            // 该方法在子线程中运行
            @Override
            public void onProgress(long progress, long total, boolean done) {
                LogUtil.e("progress: " + String.format("%d%% \n", (100 * progress) / total));
                if (mProgressHandler == null) {
                    return;
                }

                progressBean.setBytesRead(progress);
                progressBean.setContentLength(total);
                progressBean.setDone(done);
                mProgressHandler.sendMessage(progressBean);
            }
        };

        //增加拦截器
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .method(original.method(), new ProgressRequestBody(original.body(), progressListener))
                        .build();
                return chain.proceed(request);
            }
        });
        return builder.build();
    }
}
