# **FileTools**
    这是一个文件操作工具类

## 一、android集成步骤
1. 在project下的build.gradle中添加<br/>
      allprojects {<br/>
        repositories {<br/>
         ...<br/>
         maven { url 'https://jitpack.io' }<br/>
        }<br/>
      }<br/>
2. 在app下的build.gradle中添加<br/>
    dependencies {<br/>
          ...<br/>
	        compile 'com.github.rainchen1024:filetools:v1.0.0'<br/>
	}<br/>
## 二、开发
1. 实例化UploadUtils ` UploadUtils uploadUtils = new UploadUtils(this);`
2. 传入文件信息和文件服务器地址开始异步上传   <br/> 
      单文件 `new UploadFileInfo(String path)`path即本地路径<br/>
      多文件 `List<UploadFileInfo>`用集合封装UploadFileInfo<br/>  传入即可
    调用`uploadUtils.uploadFilesByOkHttp()`传入参数开始上传
    
    参数说明
` 
     * @param uploadUrl  文件服务器的地址
     * @param totalList  本地文件信息封装（单文件为UploadFileInfo）
     * @param isCompress 是否启用压缩默认不启用（false）
     * @param maxSize    单个文件的最大占用存储空间
     `
3. 回调说明<br/>
  
//上传成功的回调，totalList会按你传入顺序返回给你，不会错乱，支持更新上传（如果path为空我会认为是未修改或已经上传成功的，代码会跳过这一条数据）<br/>
void onUploadSuccess(List<UploadFileInfo> totalList);<br/>
//上传失败的回调<br/>
void onUploadFail(Exception e);<br/>
//上传进度的回调<br/>
void onUploadProcess(long currentSize, long totalSize);<br/>
