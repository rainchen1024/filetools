package com.rainchen.filetoolstest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.rainchen.filetoolstest.download.DownloadMainActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UploadActivity.class));
        });
        findViewById(R.id.button1).setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadMainActivity.class));
        });
    }
}
