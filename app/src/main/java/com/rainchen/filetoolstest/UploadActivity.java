package com.rainchen.filetoolstest;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainchen.filetools.bean.UploadFileInfo;
import com.rainchen.filetools.utils.UploadUtils;

import java.util.List;

/**
 * @author chenyu
 */
public class UploadActivity extends AppCompatActivity implements UploadUtils.OnUploadListener {
    private UploadUtils mUploadUtils;
    private final int IMAGE_REQUEST_CODE = 0x001;
    private final String fileSer = "http://ggejw.huaao24.com.cn/file/upload/files.do";
    private TextView pathTv;
    private ProgressBar bunchProgressBar;
    private TextView text;
    private TextView urlTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initView();
    }

    private void initView() {
        bunchProgressBar = findViewById(R.id.progressBar);
        text = findViewById(R.id.text);
        urlTv = findViewById(R.id.url);
        pathTv = findViewById(R.id.path);
        findViewById(R.id.button).setOnClickListener(view -> {
            mUploadUtils = new UploadUtils(this,this);

            //在这里跳转到手机系统相册里面
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_REQUEST_CODE);

        });
        urlTv.setOnClickListener(v->{
            Intent intent= new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(urlTv.getText().toString());
            intent.setData(content_url);
            startActivity(intent);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            try {
                Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String path = cursor.getString(columnIndex);  //获取照片路径
                pathTv.setText(path);
                cursor.close();
                UploadFileInfo uploadFileInfo = new UploadFileInfo(path);
                Log.d("uploadFilesByOkHttp", "======上传开始========");
                mUploadUtils.uploadFilesByOkHttp(uploadFileInfo, fileSer,true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onUploadSuccess(List<UploadFileInfo> totalList) {
        Log.d("onUploadSuccess", totalList.toString());
        urlTv.setText(totalList.get(0).getUrl());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
