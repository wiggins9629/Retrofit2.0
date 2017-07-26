package com.wiggins.retrofit.http.progress;

import android.os.Looper;
import android.os.Message;

/**
 * @Description 用来发送和处理上传消息
 * @Author 一花一世界
 */
public abstract class UploadProgressHandler extends ProgressHandler {

    private static final int UPLOAD_PROGRESS = 0;
    protected ResponseHandler mHandler = new ResponseHandler(this, Looper.getMainLooper());

    @Override
    protected void sendMessage(ProgressBean progressBean) {
        mHandler.obtainMessage(UPLOAD_PROGRESS, progressBean).sendToTarget();
    }

    @Override
    protected void handleMessage(Message message) {
        switch (message.what) {
            case UPLOAD_PROGRESS:
                ProgressBean progressBean = (ProgressBean) message.obj;
                onProgress(progressBean.getBytesRead(), progressBean.getContentLength(), progressBean.isDone());
        }
    }
}
