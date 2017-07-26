package com.wiggins.retrofit.http.progress;

/**
 * @Description 存放上传或下载进度信息
 * @Author 一花一世界
 */
public class ProgressBean {

    private long bytesRead;
    private long contentLength;
    private boolean done;

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String toString() {
        return "ProgressBean{" +
                "bytesRead=" + bytesRead +
                ", contentLength=" + contentLength +
                ", done=" + done +
                '}';
    }
}
