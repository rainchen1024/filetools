package com.rainchen.filetoolstest;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.rainchen.filetools.bean.UploadFileInfo;
import com.rainchen.filetools.utils.UploadUtils;
import com.rainchen.filetoolstest.databinding.ActivityMainBinding;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UploadUtils.OnUploadListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView(activityMainBinding);
    }

    private void initView(ActivityMainBinding activityMainBinding) {
        activityMainBinding.appCompatButton.setOnClickListener(view -> {
            UploadUtils uploadUtils = new UploadUtils(this);
            String fileSer = "http://ggejw.huaao24.com.cn/file/upload/files.do";
            //测试前记得在外部存储中放一张1.jpg的图片（实在太赖了所以直接写死了）
            UploadFileInfo uploadFileInfo = new UploadFileInfo(Environment.getExternalStorageDirectory().getPath()+ File.separator+"1.jpg");
            Log.d("uploadUtils","========开始上传============");
            uploadUtils.uploadFilesByOkHttp(uploadFileInfo,fileSer);

        });
    }

    @Override
    public void onUploadSuccess(List<UploadFileInfo> totalList) {
        Log.d("onUploadSuccess",totalList.toString());
    }

    @Override
    public void onUploadFail(Exception e) {
        Log.d("onUploadFail",e.getMessage());
    }

    @Override
    public void onUploadProcess(long currentSize, long totalSize) {
        Log.d("onUploadProcess","==="+currentSize+"======="+totalSize+"===");
    }
}
