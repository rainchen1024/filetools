package com.rainchen.filetools.bean;

/**
 * 文件信息
 * */
public class FileInfo extends BaseBean {
    public static final Creator<UploadFileInfo> CREATOR = new Creator<>(UploadFileInfo.class);


    //文件的本地路径
     String path;
    //文件MD5
     String md5;
     //文件类型
     InfoType infoType;
     //文件大小
    long size;
    //文件名
    String name;
    //文件在网上的地址
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public InfoType getInfoType() {
        return infoType;
    }

    public void setInfoType(InfoType infoType) {
        this.infoType = infoType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum InfoType {
        //文件类型
        JPG,
        JPEG,
        MP4,
        PNG,
        AAR,
    }
}
