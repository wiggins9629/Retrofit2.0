package com.wiggins.retrofit.http;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wiggins.retrofit.app.MyApplication;
import com.wiggins.retrofit.http.progress.ProgressHelper;
import com.wiggins.retrofit.utils.Constant;
import com.wiggins.retrofit.utils.LogUtil;
import com.wiggins.retrofit.utils.NetworkUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * @Description HTTP封装
 * @Author 一花一世界
 */
public class HttpUtil {

    private static OkHttpClient.Builder OkHttpClientBuilder() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                //打印日志
                .addInterceptor(httpLoggingInterceptor)
                //设置Cache目录
                .cache(cache())
                //设置缓存
                .addInterceptor(cacheInterceptor)
                .addNetworkInterceptor(cacheInterceptor)
                //设置超时
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                //错误重连
                .retryOnConnectionFailure(true);

        return builder;
    }

    private static Gson gson() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .serializeNulls()
                .create();

        return gson;
    }

    private static Retrofit.Builder RetrofitBuilder() {
        Retrofit.Builder builder = new Retrofit.Builder()
                //设置baseUrl
                .baseUrl(Constant.baseUrl)
                //设置OkHttpClient,如果不设置会提供一个默认的
                .client(OkHttpClientBuilder().build())
                //RxJava
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                //Gson转换器
                .addConverterFactory(GsonConverterFactory.create(gson()));

        return builder;
    }

    /**
     * 创建普通回调的Service
     */
    public static <T> T getService(Class<T> service) {
        return RetrofitBuilder().build().create(service);
    }

    /**
     * 创建带响应进度(下载进度)回调的Service
     */
    public static <T> T getDownLoadService(Class<T> service) {
        return RetrofitBuilder()
                .client(ProgressHelper.addProgressDownLoadBuilder(OkHttpClientBuilder()))
                .build()
                .create(service);
    }

    /**
     * 创建带请求进度(上传进度)回调的Service
     */
    public static <T> T getUpLoadService(Class<T> service) {
        return RetrofitBuilder()
                .client(ProgressHelper.addProgressUpLoadBuilder(OkHttpClientBuilder()))
                .build()
                .create(service);
    }

    /**
     * 设置缓存目录
     */
    private static Cache cache() {
        //设置缓存路径
        File cacheDir = new File(MyApplication.getContext().getExternalCacheDir(), "HttpResponseCache");
        //设置缓存大小为10M
        return new Cache(cacheDir, 10 * 1024 * 1024);
    }

    /**
     * 缓存机制
     */
    private static Interceptor cacheInterceptor = new Interceptor() {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            //在每个请求发出前，判断一下网络状况，如果没问题继续访问，如果有问题，则设置从本地缓存中读取
            if (!NetworkUtils.isNetworkAvailable()) {
                LogUtil.i("no network");
                request = request.newBuilder()
                        //强制使用缓存
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }

            Response response = chain.proceed(request);
            //先判断网络，网络好的时候，移除header后添加cache失效时间为0小时，网络未连接的情况下设置缓存时间为4周
            if (NetworkUtils.isNetworkAvailable()) {
                LogUtil.i("has network");
                // 有网络时 设置缓存超时时间0个小时
                int maxAge = 0;// 在线缓存0个小时
                response.newBuilder()
                        .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                LogUtil.i("network error");
                // 无网络时，设置超时为4周
                int maxStale = 60 * 60 * 24 * 4 * 7;// 离线缓存4周
                response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
            return response;
        }
    };


    /**
     * 判断sd卡可用
     */
    private static boolean hasSDCardMounted() {
        String state = Environment.getExternalStorageState();
        if (state != null && state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 存储的用量情况
     */
    private static long getUsableSpace(File path) {
        if (path == null) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        } else {
            if (!path.exists()) {
                return 0;
            } else {
                final StatFs stats = new StatFs(path.getPath());
                return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
            }
        }
    }
}
