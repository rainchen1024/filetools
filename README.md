# **FileTools**
    这是一个文件操作工具类

## 一、android集成步骤

1. 在app下的build.gradle中添加<br/>
```java
dependencies {
      ...
        implementation 'com.rainchen.filetools:filetools:1.1.5'<br/>
}
```
## 二、开发
**a.文件上传**
<br/>
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

3 回调说明<br/>
  
//上传成功的回调，totalList会按你传入顺序返回给你，不会错乱，支持更新上传（如果path为空我会认为是未修改或已经上传成功的，代码会跳过这一条数据）<br/>
	void onUploadSuccess(List<UploadFileInfo> totalList);<br/>
//上传失败的回调<br/>
	void onUploadFail(Exception e);<br/>
//上传进度的回调<br/>
	void onUploadProcess(long currentSize, long totalSize);<br/>
<br/>
<br/>
**b.文件下载功能使用**<br/>
[文件下载功能详情请移步这里](https://github.com/rainchen1024/filetools/blob/dev/okdownload-zh.md) 
<br/><br/>
**c.图片压缩功能使用**<br/>

### 异步调用

`Luban`内部采用`IO`线程进行图片压缩，外部调用只需设置好结果监听即可：

```java
ImgCompress.with(this)
        .load(photos)                                   // 传人要压缩的图片列表
        .ignoreBy(100)                                  // 忽略不压缩图片的大小
        .setTargetDir(getPath())                        // 设置压缩后文件存储位置
        .setCompressListener(new OnCompressListener() { //设置回调
          @Override
          public void onStart() {
            // TODO 压缩开始前调用，可以在方法内启动 loading UI
          }

          @Override
          public void onSuccess(File file) {
            // TODO 压缩成功后调用，返回压缩后的图片文件
          }

          @Override
          public void onError(Throwable e) {
            // TODO 当压缩过程出现问题时调用
          }
        }).launchOnUiThread();    //启动压缩
```

### 同步调用

同步方法请尽量避免在主线程调用以免阻塞主线程，下面以rxJava调用为例

```java
Flowable.just(photos)
    .observeOn(Schedulers.io())
    .map(new Function<List<String>, List<File>>() {
      @Override public List<File> apply(@NonNull List<String> list) throws Exception {
        // 同步方法直接返回压缩后的文件
        return ImgCompress.with(MainActivity.this).load(list).get();
      }
    })
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe();
```
 备注：
---


1. 表单提交时记得把本地的文件名给服务端<br/>
<!--lang:java-->
	builder.addFormDataPart("name" + i, file.getName(), RequestBody.create(null, file));
<br/>
2. 服务端返回给你数据时一定要将你传的文件名作为key值 

```java
private void updateFileInfo(JSONArray data, int i, List<UploadFileInfo> totalList) throws JSONException {
        JSONObject object = data.getJSONObject(i);
        UploadFileInfo uploadFileInfo = totalList.get(i);
        String path = uploadFileInfo.getPath();
        String fileName = FileUtils.getFileName(path);
        String substring = fileName.substring(0, fileName.indexOf("."));
        if (object.has(substring)) {
            String s = object.optString(substring);
            String url = s.replace("\\/", File.separator);
            //2017.3.10修改为返回相对路径（cy）
//                                    uploadFileInfo.setUploadUrl(HttpRequestUtils.DOWNLOAD_HOST + url);
            uploadFileInfo.setUrl(url);
            uploadSize++;
        }
}
```
<br/>
3. 如遇到FileUriExposedException异常[请参考](https://blog.csdn.net/jdsjlzx/article/details/68487013)<br/><br/><br/>
灵感来源于 <br/>
[鲁班压缩](https://github.com/Curzibn/Luban)<br/>
[okdownload](https://github.com/lingochamp/okdownload)

[更过文章](https://rainchen1024.github.io)
