package com.rainchen.filetools.bean;

/**
 * Created by Administrator on 2016/12/13.
 */

public class UploadFileInfo extends BaseBean {

    public static final Creator<UploadFileInfo> CREATOR = new Creator<>(UploadFileInfo.class);
    private String fileKey;

    private InfoType infoType = InfoType.PIC;//默认值
    //文件的本地路径
    private String path;
    //上传成功返回的url(相对路径)
    private String uploadUrl;
    //视频或声音的时长
    private int duration;

    public enum InfoType {
        //文件类型
        PIC, VOICE, VIDEO
    }

    public UploadFileInfo() {
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public InfoType getInfoType() {
        return infoType;
    }

    public void setInfoType(InfoType infoType) {
        this.infoType = infoType;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
