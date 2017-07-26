package com.wiggins.retrofit.http;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;

/**
 * @Description API接口
 * @Author 一花一世界
 */
public interface HttpApi {

    @GET("{url}")
    Call<ResponseBody> enqueueGet(@Path("url") String url);

    @GET("{url}")
    Call<ResponseBody> enqueueGet(@Path("url") String url, @QueryMap Map<String, String> maps);

    @FormUrlEncoded
    @POST("{url}")
    Call<ResponseBody> enqueuePost(@Path("url") String url);

    @FormUrlEncoded
    @POST("{url}")
    Call<ResponseBody> enqueuePost(@Path("url") String url, @FieldMap Map<String, String> maps);

    @GET
    Call<ResponseBody> requestByDownload(@Url String url);

    @Multipart
    @POST("{url}")
    Call<ResponseBody> requestByUpload(@Path("url") String url, @PartMap Map<String, RequestBody> params);

    @GET("{url}")
    Observable<ResponseBody> requestByRxJavaGet(@Path("url") String url, @QueryMap Map<String, String> maps);

    @FormUrlEncoded
    @POST("{url}")
    Observable<ResponseBody> requestByRxJavaPost(@Path("url") String url, @FieldMap Map<String, String> maps);
}
