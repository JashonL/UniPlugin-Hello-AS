package com.zkf.jly_plugin;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RequestPermissionHub.requestBLEPermission(getSupportFragmentManager(), new RequestPermissionHub.Callback() {
            @Override
            public void onResult(boolean isGranted) {
                if (isGranted){//同意权限
                    //执行你的逻辑
                }
            }
        });

    }
}
