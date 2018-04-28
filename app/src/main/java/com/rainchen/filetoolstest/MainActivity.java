package com.rainchen.filetoolstest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainchen.filetools.bean.UploadFileInfo;
import com.rainchen.filetools.utils.UploadUtils;
import com.rainchen.filetoolstest.download.DownloadMainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UploadUtils.OnUploadListener {

    private ProgressBar bunchProgressBar;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        bunchProgressBar = findViewById(R.id.progressBar);
        text = findViewById(R.id.text);
        findViewById(R.id.button).setOnClickListener(view -> {
            UploadUtils uploadUtils = new UploadUtils(this);
            String fileSer = "http://ggejw.huaao24.com.cn/file/upload/files.do";
            //测试前记得在外部存储中放一张1.jpg的图片（实在太赖了所以直接写死了）
            List<UploadFileInfo> fileInfos = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                UploadFileInfo uploadFileInfo = new UploadFileInfo(Environment.getExternalStorageDirectory().getPath() + File.separator + "1.jpg");
                fileInfos.add(uploadFileInfo);
            }
            Log.d("uploadUtils", "========开始上传============");
            uploadUtils.uploadFilesByOkHttp(fileInfos, fileSer,true);

        });
        findViewById(R.id.button1).setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadMainActivity.class));
        });
    }

    @Override
    public void onUploadSuccess(List<UploadFileInfo> totalList) {
        Log.d("onUploadSuccess", totalList.toString());
        Toast.makeText(this, "上传成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadFail(Exception e) {
        Log.d("onUploadFail", e.getMessage());
        Toast.makeText(this, "上传失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadProcess(long currentSize, long totalSize) {
        Log.d("onUploadProcess", "===" + currentSize + "=======" + totalSize + "===");
        bunchProgressBar.setMax((int) totalSize);
        bunchProgressBar.setProgress((int) currentSize);
        text.setText(currentSize + "/" + totalSize);
    }

}
