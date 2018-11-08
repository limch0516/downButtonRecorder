package com.ch_l.downbuttonrecorder;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Log.d("인트로 액티비티", "시작입니다");
        intent = new Intent(getApplicationContext(), MainActivity.class);

        if (Build.VERSION.SDK_INT >= 23) {

            new TedPermission().with(this)
                    .setRationaleMessage("녹음파일 생성과 저장을 위해서는 접근 권한이 필요합니다")


                    .setDeniedMessage("녹음파일생성과 저장을위해 동의해주세요")
                    .setPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
                    .setPermissionListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            startActivity(intent);

                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            Toast.makeText(getApplicationContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .check();
        } else {

            startActivity(intent);

        }


    }
}
