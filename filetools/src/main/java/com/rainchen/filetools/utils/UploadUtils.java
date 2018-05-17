package com.rainchen.filetools.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.rainchen.filetools.bean.UploadFileInfo;
import com.rainchen.filetools.upload.helper.ProgressHelper;
import com.rainchen.filetools.utils.compress.ImgCompress;
import com.rainchen.filetools.utils.compress.OnCompressListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by Administrator on 2016/12/15.
 * 上传文件的工具类
 */

public class UploadUtils {

    private final int ALIYUN_TYPE = 0x00;
    private final int OKHTTP_TYPE = 0x01;
    private final int COMPRESS_SUCCESS = 0x02;
    private final int COMPRESS_FAILED = 0x03;
    private final int UPLOAD_SUCCESS = 0x004;
    private final int UPLOAD_FAILED = 0x005;
    private final int UPLOAD_PROCESS = 0x006;
    private final int UPLOAD_START = 0x007;
    private int uploadSize;
    private int failSize;
    private long totalSize;
    private MyHandler handler = new MyHandler(this);
    private Context mContext;
    private List<UploadFileInfo> mTotalList;//上传文件的信息
    private String mUploadUrl;//文件服务器地址
    private boolean mIsCompress;//是否压缩（图片）

    private static class MyHandler extends Handler {
        private final WeakReference<UploadUtils> mUploadUtils;

        private MyHandler(UploadUtils uploadUtils) {
            mUploadUtils = new WeakReference<UploadUtils>(uploadUtils);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mUploadUtils.get() == null) {
                return;
            }
            mUploadUtils.get().toDoMsg(msg);
        }
    }

    private void toDoMsg(Message msg) {
        switch (msg.what) {
            case UPLOAD_SUCCESS:
//                List<UploadFileInfo> totalList = (List<UploadFileInfo>) msg.obj;
                mOnUploadListener.onUploadSuccess(mTotalList);
                delTempFile();
                release();
                break;

            case UPLOAD_FAILED:
                Exception e = (Exception) msg.obj;
                mOnUploadListener.onUploadFail(e);
                delTempFile();
                release();
                break;

            case UPLOAD_PROCESS:
                long curSize = (long) msg.obj;
                Log.d("UploadUtils", "handler..." + curSize + "/" + totalSize);
                mOnUploadListener.onUploadProcess(curSize, totalSize);
                break;
            case UPLOAD_START:
                if (mIsCompress) {
                    compress();
                } else {
                    uploadAsync();
                }
                break;
            case COMPRESS_SUCCESS:
                uploadAsync();
                break;
            case COMPRESS_FAILED:
//                release();
                break;
            default:
                break;
        }
    }



    public UploadUtils(Context context, OnUploadListener mOnUploadListener) {
        this.mContext = context;
        this.mOnUploadListener = mOnUploadListener;
    }

    /**
     * @param fileInfo  单文件信息
     * @param uploadUrl 文件服务器地址
     */
    public void uploadFilesByOkHttp(UploadFileInfo fileInfo, String uploadUrl) {
        List<UploadFileInfo> fileInfos = new ArrayList<>();
        fileInfos.add(fileInfo);
        uploadFilesByOkHttp(fileInfos, uploadUrl);
    }

    /**
     * @param fileInfo   单文件信息
     * @param uploadUrl  文件服务器地址
     * @param isCompress 是否压缩
     */
    public void uploadFilesByOkHttp(UploadFileInfo fileInfo, String uploadUrl, boolean isCompress) {
        List<UploadFileInfo> fileInfos = new ArrayList<>();
        fileInfos.add(fileInfo);
        uploadFilesByOkHttp(fileInfos, uploadUrl, isCompress);
    }


    /**
     * @param totalList 本地文件信息封装
     * @param uploadUrl 文件服务器地址
     *                  非阿里云oss  基于okhttp3实现
     *                  不压缩
     */
    private void uploadFilesByOkHttp(final List<UploadFileInfo> totalList, String uploadUrl) {
        uploadFilesByOkHttp(totalList, uploadUrl, false);
    }


    /**
     * @param uploadUrl  uploadUrl上传服务器的地址
     * @param totalList  本地文件信息封装
     * @param isCompress 是否启用压缩默认不启用（false）
     * <p>
     * <p>
     * <p>
     * 非阿里云oss  基于okhttp3实现
     * 自定义压缩单个文件大小
     */


    private void uploadFilesByOkHttp(List<UploadFileInfo> totalList, String uploadUrl, boolean isCompress) {
        //统计下载的个数
        uploadSize = 0;
        failSize = 0;
        if (totalList == null || totalList.size() == 0) {
            throw new NullPointerException("totalList is null or totalList.size() == 0");
        } else {
            this.mTotalList = totalList;
            this.mUploadUrl = uploadUrl;
            this.mIsCompress = isCompress;
            handler.obtainMessage(UPLOAD_START).sendToTarget();
        }

    }

    /*
     * 异步上传
     * */
    private void uploadAsync() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(this::uploadByOkhttp);
    }

    /**
     * 表单的封装
     */
    private synchronized void uploadByOkhttp() {
        final int size = mTotalList.size();
        OkHttpClient client = new OkHttpClient.Builder()
                //设置超时，不设置可能会报异常
                .connectTimeout(5000, TimeUnit.MILLISECONDS).readTimeout(10000, TimeUnit
                        .MILLISECONDS).writeTimeout(60000, TimeUnit.MILLISECONDS).build();
        //构造上传请求，类似web表单
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody
                .FORM).addFormDataPart("os", "android");
        /*
         * 表单封装
         * 以文件名字作为key（file.getName()）
         *
         * */
        for (int i = 0; i < size; i++) {
            File file;
            UploadFileInfo uploadFileInfo = mTotalList.get(i);
            String path = uploadFileInfo.getPath();
            String url = uploadFileInfo.getUrl();
            //兼容修改页面的上传（只要UploadFileInfo里的path不为空的话就代表是修改后上传的）
            if (!TextUtils.isEmpty(path)) {
                file = new File(path);
            } else if (!TextUtils.isEmpty(url)) {
                ++uploadSize;
                continue;
            } else {
                ++failSize;
                continue;
            }
            builder.addFormDataPart("name" + i, file.getName(), RequestBody.create(null, file));

        }
        if (uploadSize == size) {
            handler.obtainMessage(UPLOAD_SUCCESS).sendToTarget();
            return;
        } else if (failSize > 0) {
            handler.obtainMessage(UPLOAD_FAILED, new Exception("upload failed")).sendToTarget();
            return;
        }

        MultipartBody requestBody = builder.build();
        //进行包装，使其支持进度回调
        Request request = new Request.Builder().url(mUploadUrl).post(ProgressHelper
                .addProgressRequestListener(requestBody, (currentBytes, contentLength, done) -> {
                    //这个是ui线程回调，可直接操作UI
                    UploadUtils.this.totalSize = contentLength;
                    Log.d("UploadUtils", "uploading..." + currentBytes + "/" + totalSize);
                    handler.obtainMessage(UPLOAD_PROCESS, currentBytes).sendToTarget();
                })).build();
        //开始请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@Nullable Call call, @NonNull IOException e) {
                Log.d("UploadUtils", e.getMessage());
                handler.obtainMessage(UPLOAD_FAILED, e).sendToTarget();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String json = response.body().string();
                    Log.d("UploadUtils", json);
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray data = jsonObject.optJSONArray("data");
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            updateFileInfo(data, i, mTotalList);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(UPLOAD_FAILED, e).sendToTarget();
                }
                if (uploadSize == size) {
                    handler.obtainMessage(UPLOAD_SUCCESS).sendToTarget();
                } else {
                    handler.obtainMessage(UPLOAD_FAILED, new Exception("upload failed")).sendToTarget();
                }
            }

        });
    }

    /**
     * 封装返回的文件信息
     */
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

    /**
     * 删除压缩临时产生的文件
     */
    private void delTempFile() {
        File file = new File(getPath());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                f.delete();
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }

    public static String getProcessString(String formatString, long currentSize, long totalSize) {
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(formatString)) {
            sb.append(formatString);
        }
        if (currentSize < 0) {
            currentSize = 0;
        }
        if (currentSize > totalSize) {
            currentSize = totalSize;
        }
        String format = " (%s)";
        sb.append(String.format(format, parsePercent((double) currentSize / (double) totalSize)));
        return sb.toString();
    }

    /**
     * Object 转化成百分比 传如的参数必须是数字类型， 如"0.02" return 2.00%
     * <p>
     * 百分比位数 参数可自行调整
     *
     * @param obj
     * @return 返回百分比
     */
    private static String parsePercent(double obj) {
        Double d = obj;
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(0); //最大小数位数
        percentFormat.setMaximumIntegerDigits(3);//最大整数位数
        percentFormat.setMinimumFractionDigits(0); //最小小数位数
        percentFormat.setMinimumIntegerDigits(1);//最小整数位数
        return percentFormat.format(d);//自动转换成百分比显示
    }

    /**
     * 图片压缩
     */
    private void compress() {
        List<UploadFileInfo> temp = new ArrayList<>();
        for (UploadFileInfo fileInfo : mTotalList) {
            if (!TextUtils.isEmpty(fileInfo.getPath())) {
                temp.add(fileInfo);
            }
        }
        ImgCompress.with(mContext)
                .load(temp)
                .ignoreBy(200)
                .setTargetDir(getPath())//context.getCacheDir().getPath()+"/temp/img/"
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(File file) {
                        for (int i = 0; i < mTotalList.size(); i++) {
                            //压缩后的文件
                            String path1 = file.getPath();
                            String name1 = path1.substring(path1.lastIndexOf(File.separator) + 1);
                            //源文件
                            UploadFileInfo uploadFileInfo = mTotalList.get(i);
                            String path2 = uploadFileInfo.getPath();
                            String name2 = path2.substring(path2.lastIndexOf(File.separator) + 1);
                            if (name1.equals(name2)) {
                                uploadFileInfo.setPath(path1);
                                uploadFileInfo.setCompress(true);
                            }
                            Log.d("compress", path1 + "======" + path2);
                        }
                        handler.obtainMessage(COMPRESS_SUCCESS).sendToTarget();
                    }


                    @Override
                    public void onError(Throwable e) {
                        handler.obtainMessage(UPLOAD_FAILED, e).sendToTarget();
                    }
                }).launchOnUiThread();
    }

    //临时文件夹
    private String getPath() {
        String path = mContext.getExternalCacheDir() + "/temp/image/";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }


    //设置接口回调
    private OnUploadListener mOnUploadListener;

    public interface OnUploadListener {
        void onUploadSuccess(List<UploadFileInfo> totalList);//上传成功的回调

        void onUploadFail(Exception e);//上传失败的回调

        void onUploadProcess(long currentSize, long totalSize);//上传进度的回调
    }

    //释放资源
    private void release() {
        //移除所有的callback和messages
        if (handler!=null) {
            handler.removeCallbacksAndMessages(null);
        }
        handler = null;
        mTotalList = null;
        mIsCompress = false;
    }
}
