package com.ch_l.downbuttonrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static com.ch_l.downbuttonrecorder.MainActivity.recorder;


public class RecordService extends Service {
    String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/downButtonRecorder";
    String RECORDED_FILE;
//    MediaRecorder recorder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //파일이름 설정
        String dateCheck = new MainActivity.DateCheck().DateCheck();
        File file = new File(sdcard, dateCheck + ".mp4");
        RECORDED_FILE = file.getAbsolutePath();
        recorder = recorder;

        //오디오 입력 형식 설정
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //파일 저장 방식 설정(확장자)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        //코덱 설정
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //파일 저장 경로 설정
        recorder.setOutputFile(RECORDED_FILE);
        try {

            Toast.makeText(getApplicationContext(), "녹음 시작", Toast.LENGTH_SHORT).show();

            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            super.onDestroy();
        }
    }
}
