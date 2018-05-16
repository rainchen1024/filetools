package com.rainchen.filetoolstest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.rainchen.filetoolstest.download.DownloadMainActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<MyShortcutInfo> infos = new ArrayList<>();
            infos.add(new MyShortcutInfo(new Intent(Intent.ACTION_VIEW,null,this,UploadActivity.class),"上传测试",R.drawable.ic_arrow_upward_black_24dp));
            infos.add(new MyShortcutInfo(new Intent(Intent.ACTION_VIEW,null,this,DownloadMainActivity.class),"下载测试",R.drawable.ic_arrow_downward_black_24dp));
            setupShortcuts(infos);
        }
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

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupShortcuts(List<MyShortcutInfo> myShortcutInfos) {
        ShortcutManager mShortcutManager = getSystemService(ShortcutManager.class);
            List<ShortcutInfo> infos = new ArrayList<>();
        for (MyShortcutInfo myShortcutInfo : myShortcutInfos) {
            Intent intent = myShortcutInfo.intent;

            ShortcutInfo info = new ShortcutInfo.Builder(this, "id" + myShortcutInfo.id)
                    .setShortLabel(myShortcutInfo.label)
                    .setLongLabel(myShortcutInfo.label)
                    .setIcon(Icon.createWithResource(this, myShortcutInfo.icon))
                    .setIntent(intent)
                    .build();
            infos.add(info);
//            manager.addDynamicShortcuts(Arrays.asList(info));
        }
        mShortcutManager.setDynamicShortcuts(infos);
    }

    public static class MyShortcutInfo{
        Intent intent;
        String label;
        int icon;
        long id;

        public MyShortcutInfo(Intent intent, String label, int icon) {
            this.id = System.currentTimeMillis();
            this.intent = intent;
            this.label = label;
            this.icon = icon;
        }
    }
}
