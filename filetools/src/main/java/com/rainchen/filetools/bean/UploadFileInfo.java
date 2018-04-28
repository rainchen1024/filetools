package com.rainchen.filetools.bean;

/**
 * Created by Administrator on 2016/12/13.
 */

public class UploadFileInfo extends FileInfo {

    public static final Creator<UploadFileInfo> CREATOR = new Creator<>(UploadFileInfo.class);
    private String fileKey;


    //视频或声音的时长
    private int duration;
    //是否压缩过(针对于图片)
    private boolean isCompress;



    public UploadFileInfo() {

    }

    public boolean isCompress() {
        return isCompress;
    }

    public void setCompress(boolean compress) {
        isCompress = compress;
    }

    public UploadFileInfo(String path) {
        this.path = path;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
