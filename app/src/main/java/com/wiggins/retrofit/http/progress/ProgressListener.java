package com.wiggins.retrofit.http.progress;

/**
 * @Description 请求/响应体进度回调接口，用于文件上传/下载进度回调
 * @Author 一花一世界
 */
public interface ProgressListener {
    /**
     * @param progress 已经下载或上传字节数
     * @param total    总字节数
     * @param done     是否完成
     */
    void onProgress(long progress, long total, boolean done);
}
