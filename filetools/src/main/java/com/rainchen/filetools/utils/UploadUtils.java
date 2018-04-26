package com.rainchen.filetools.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.rainchen.filetools.bean.UploadFileInfo;
import com.rainchen.filetools.upload.helper.ProgressHelper;
import com.rainchen.filetools.upload.listener.ProgressListener;

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

    private static final int UPLOAD_SUCCESS = 0;
    private static final int UPLOAD_FAILED = 1;
    private static final int UPLOAD_PROCESS = 2;
    public static final int ALIYUN_TYPE = 0x00;
    public static final int OKHTTP_TYPE = 0x01;
    private int uploadSize;
    private int failSize;
    private long totalSize;
    private MyHandler handler = new MyHandler(this);

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
                List<UploadFileInfo> totalList = (List<UploadFileInfo>) msg.obj;
                mOnUploadListener.onUploadSuccess(totalList);
                break;

            case UPLOAD_FAILED:
                Exception e = (Exception) msg.obj;
                mOnUploadListener.onUploadFail(e);
                break;

            case UPLOAD_PROCESS:
                long curSize = (long) msg.obj;
                Log.d("UploadUtils", "handler..." + curSize + "/" + totalSize);
                mOnUploadListener.onUploadProcess(curSize, totalSize);
                break;
            default:
                break;
        }
    }


    public UploadUtils(OnUploadListener mOnUploadListener) {
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

    //图片压缩
    private String fileCompress(String picPath, String newPath, int maxSize) {
        return ImageUtils.compressFile(picPath, newPath, maxSize);
    }

    /**
     * @param totalList 本地文件信息封装
     * @param uploadUrl 文件服务器地址
     *                  非阿里云oss  基于okhttp3实现
     *                  不压缩
     */
    public void uploadFilesByOkHttp(final List<UploadFileInfo> totalList, String uploadUrl) {
        uploadFilesByOkHttp(totalList, uploadUrl, false);
    }

    /**
     * @param uploadUrl  uploadUrl上传服务器的地址
     * @param totalList  本地文件信息封装
     * @param isCompress 是否启用压缩默认不启用（false）
     *                   <p>
     *                   <p>
     *                   非阿里云oss  基于okhttp3实现
     *                   默认压缩到200k（单个文件)
     */
    public void uploadFilesByOkHttp(final List<UploadFileInfo> totalList, String uploadUrl, boolean isCompress) {
        uploadFilesByOkHttp(totalList, uploadUrl, isCompress, 200);
    }

    /**
     * @param uploadUrl  uploadUrl上传服务器的地址
     * @param totalList  本地文件信息封装
     * @param isCompress 是否启用压缩默认不启用（false）
     * @param maxSize    单个文件的最大占用存储空间
     *                   <p>
     *                   <p>
     *                   <p>
     *                   非阿里云oss  基于okhttp3实现
     *                   自定义压缩单个文件大小
     */
    public void uploadFilesByOkHttp(final List<UploadFileInfo> totalList, String uploadUrl, boolean isCompress, int maxSize) {
        //统计下载的个数
        uploadSize = 0;
        failSize = 0;
        if (totalList == null || totalList.size() == 0) {
            throw new NullPointerException("totalList not null and totalList.size()!=0");
        } else {
            MyAsyncTask myAsyncTask = new MyAsyncTask(OKHTTP_TYPE, totalList, uploadUrl, isCompress, maxSize);
            myAsyncTask.execute();

        }

    }

    /**
     * @param uploadUrl  uploadUrl上传服务器的地址
     * @param totalList  本地文件信息封装
     * @param isCompress 是否启用压缩默认不启用（false）
     * @param maxSize    单个文件的最大占用存储空间
     */
    private synchronized void uploadAndCompressByOkhttp(int type, final List<UploadFileInfo> totalList, String uploadUrl, final boolean isCompress, int maxSize) {
        final int size = totalList.size();
        OkHttpClient client = new OkHttpClient.Builder()
                //设置超时，不设置可能会报异常
                .connectTimeout(5000, TimeUnit.MILLISECONDS).readTimeout(10000, TimeUnit
                        .MILLISECONDS).writeTimeout(60000, TimeUnit.MILLISECONDS).build();
        //构造上传请求，类似web表单
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody
                .FORM).addFormDataPart("os", "android");
        final String[] tempPathArr = new String[size];
        /*
         * 表单封装
         * 以文件名字作为key（file.getName()）
         *
         * */
        for (int i = 0; i < size; i++) {
            File file;
            UploadFileInfo uploadFileInfo = totalList.get(i);
            String path = uploadFileInfo.getPath();
            String url = uploadFileInfo.getUploadUrl();
            String tempPath;
            String uploadPicPath;
            //兼容修改页面的上传（只要UploadFileInfo里的path不为空的话就代表是修改后上传的）
            if (!TextUtils.isEmpty(path)) {
                if (isCompress) {//是否启用压缩
                    tempPath = path.substring(0, path.lastIndexOf("/") + 1) + "temp" + SystemClock.currentThreadTimeMillis() + ".jpg";
                    uploadPicPath = fileCompress(path, tempPath, maxSize);
                    tempPathArr[i] = tempPath;
                } else {
                    uploadPicPath = path;
                    tempPathArr[i] = path;
                }
                file = new File(uploadPicPath);
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
            handler.obtainMessage(UPLOAD_SUCCESS, totalList).sendToTarget();
            return;
        } else if (failSize > 0) {
            handler.obtainMessage(UPLOAD_FAILED, new Exception("upload failed")).sendToTarget();
            return;
        }

        MultipartBody requestBody = builder.build();
        //进行包装，使其支持进度回调
        Request request = new Request.Builder().url(uploadUrl).post(ProgressHelper
                .addProgressRequestListener(requestBody, new ProgressListener() {
                    @Override
                    public void onProgress(long currentBytes, long contentLength, boolean done) {
                        //这个是ui线程回调，可直接操作UI
                        UploadUtils.this.totalSize = contentLength;
                        Log.d("UploadUtils", "uploading..." + currentBytes + "/" + totalSize);
                        handler.obtainMessage(UPLOAD_PROCESS, currentBytes).sendToTarget();
                    }
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
                            String substring = updateFileInfo(data, i, totalList);
                            delTempFile(i, substring, tempPathArr, isCompress);
                            if (TextUtils.isEmpty(substring)) {
                                failSize++;
                            } else {
                                ++uploadSize;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(UPLOAD_FAILED, e).sendToTarget();
                }
                if (uploadSize == size) {
                    handler.obtainMessage(UPLOAD_SUCCESS, totalList).sendToTarget();
                } else {
                    handler.obtainMessage(UPLOAD_FAILED, new Exception("upload failed")).sendToTarget();
                }
            }

        });
    }

    /**
     * 封装返回的文件信息
     */
    private String updateFileInfo(JSONArray data, int i, List<UploadFileInfo> totalList) throws JSONException {
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
            uploadFileInfo.setUploadUrl(url);
        }
        return substring;
    }

    /**
     * 删除压缩临时产生的文件
     */
    private void delTempFile(int i, String substring, String[] tempPathArr, boolean isCompress) {
        if (!TextUtils.isEmpty(tempPathArr[i]) && !TextUtils.isEmpty(substring)) {
            String tempFileName = FileUtils.getFileName(tempPathArr[i]);
            String tempName = tempFileName.substring(0, tempFileName.indexOf("."));
            if (substring.equals(tempName) && isCompress) {//删除上传产生的临时文件
                new File(tempPathArr[i]).delete();
            }
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

    private class MyAsyncTask extends AsyncTask {
        private List<UploadFileInfo> totalList;
        private boolean isCompress;
        private int maxSize;
        private int uploadType;
        private String uploadUrl;

        private MyAsyncTask(int uploadType, List<UploadFileInfo> totalList, boolean isCompress, int maxSize) {
            this.uploadType = uploadType;
            this.totalList = totalList;
            this.isCompress = isCompress;
            this.maxSize = maxSize;
        }

        private MyAsyncTask(int uploadType, final List<UploadFileInfo> totalList, String uploadUrl, boolean isCompress, int maxSize) {
            this.uploadType = uploadType;
            this.totalList = totalList;
            this.isCompress = isCompress;
            this.maxSize = maxSize;
            this.uploadUrl = uploadUrl;
        }

        @Override
        protected void onPreExecute() {
            //onPreExecute方法用于在执行后台任务前做一些UI操作
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Object[] params) {
            //doInBackground方法内部执行后台任务,不可在此方法内修改UI
            if (uploadType == OKHTTP_TYPE) {
                //okhttp上传
                uploadAndCompressByOkhttp(OKHTTP_TYPE, totalList, uploadUrl, isCompress, maxSize);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            //onProgressUpdate方法用于更新进度信息
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
            super.onPostExecute(o);
        }


        @Override
        protected void onCancelled() {
            //onCancelled方法用于在取消执行中的任务时更改UI
            super.onCancelled();
        }
    }

    //设置接口回调
    private OnUploadListener mOnUploadListener;

    public interface OnUploadListener {
        void onUploadSuccess(List<UploadFileInfo> totalList);//上传成功的回调

        void onUploadFail(Exception e);//上传失败的回调

        void onUploadProcess(long currentSize, long totalSize);//上传进度的回调
    }

    //释放资源
    public void release() {
        //移除所有的callback和messages
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }
}
