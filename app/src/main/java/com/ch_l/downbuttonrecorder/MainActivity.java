package com.ch_l.downbuttonrecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainAct";
//    ActionBar actionBar;

    MediaPlayer mediaPlayer;
    MediaRecorder recorder;

    String getTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("녹음기");

        permissioncheck();

        DateCheck();
        Toast.makeText(this, DateCheck(), Toast.LENGTH_SHORT).show();



        //저장경로 지정

        File file = Environment.getExternalStorageDirectory();
        final String saPath = file.getAbsolutePath() + "/Download/downButtonRecorder/"+DateCheck()+".3gp";


        Button btnPause = (Button) findViewById(R.id.buttonPause);
        Button btnRecord = (Button) findViewById(R.id.buttonRecord);
        Button btnStop = (Button) findViewById(R.id.buttonStop);
        Button btnStart = (Button) findViewById(R.id.buttonStart);


        //녹음시작버튼
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "record start");

                recorder = new MediaRecorder();
                //오디오 입력 형식 설정
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //파일 저장 방식 설정(확장자)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                //코덱 설정
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                //파일 저장 경로 설정
                recorder.setOutputFile(saPath);
                try {
                    Toast.makeText(getApplicationContext(), "녹음 시작", Toast.LENGTH_SHORT).show();


                    recorder.prepare();
                    recorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        //녹음종료버튼
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recorder.stop();
//                recorder.release();

                Log.d(TAG, "record stop");
                Toast.makeText(getApplicationContext(), "녹음 중지", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //권한설정 무조건 다시(2018-10)
    public void permissioncheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission_group.STORAGE)) ;
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) ;

                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                requestPermissions(new String[]{Manifest.permission_group.STORAGE}, 1);
            }

        }

    }

    public String DateCheck(){

        //녹음시작 시간구하기(녹음파일명)
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String getTime = df.format(new Date());






        return getTime;
    }


}
