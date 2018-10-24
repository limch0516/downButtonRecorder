package com.ch_l.downbuttonrecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainAct";
    //    ActionBar actionBar;
    private static String RECORDED_FILE;
    MediaPlayer mediaPlayer;
    MediaRecorder recorder;
    SeekBar seekBar;
    //textView
    TextView tv_stname,tv_ding,tv_fduration;
    LinearLayout linearLayout;


    //recyclerView
    recordAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<recordItem> items = new ArrayList<>();

    SwipeRefreshLayout refreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("녹음기");

        permissioncheck();

        //recycleView 설정
        setRecyclerView();


        //RecyclerView 새로고침
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                refreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), "새로고침 왜안됨...", Toast.LENGTH_SHORT).show();
            }
        });
        //아이템 터치
        tv_stname=(TextView)findViewById(R.id.tv_stname);
        tv_ding=(TextView)findViewById(R.id.tv_ding);
        tv_fduration=(TextView)findViewById(R.id.tv_fduration);
        linearLayout=(LinearLayout)findViewById(R.id.Linear1);







        //저장경로 지정

//        File sdcard = Environment.getExternalStorageDirectory();

        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/downButtonRecorder";
        File path = new File(sdcard);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(sdcard, DateCheck() + ".mp4");

        RECORDED_FILE = file.getAbsolutePath();


        Button btnPause = (Button) findViewById(R.id.buttonPause);
        Button btnRecord = (Button) findViewById(R.id.buttonRecord);
        Button btnStop = (Button) findViewById(R.id.buttonStop);
        Button btnStart = (Button) findViewById(R.id.buttonStart);

        mediaPlayer = new MediaPlayer();

        //파일 재생버튼
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }

        });

        //녹음시작버튼
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                }
                Log.d(TAG, "record start");

                recorder = new MediaRecorder();

                //오디오 입력 형식 설정
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //파일 저장 방식 설정(확장자)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
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

            }
        });

        //녹음종료버튼
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder != null) {


                    recorder.stop();
                    recorder.release();
                    recorder = null;

                }
                Log.d(TAG, "record stop");
                Toast.makeText(getApplicationContext(), "녹음 중지", Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    protected void onPause() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onPause();
    }

    private void setRecyclerView() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerSetData();
    }

    //RecyclerView 설정
    private void recyclerSetData() {


        //파일 정보 리스트에 넣기
        //불러올 파일 경로설정
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/downButtonRecorder");
        //불러올파일 확장자

        //불러온 파일 배열에 넣기
        File[] files = file.listFiles();
        //파일 정보 넣을 배열 만들기


        String[] item_name = new String[files.length];
        String[] item_time = new String[files.length];
        String[] item_size = new String[files.length];
        String[] item_date = new String[files.length];
        for (int i = 0; i < files.length; i++) {


            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(String.valueOf(files[i]));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long timeInmillisec = Long.parseLong(time);
            long duration = timeInmillisec / 1000;
            long hours = duration / 3600;
            long minutes = (duration - hours * 3600) / 60;
            long seconds = duration - (hours * 3600 + minutes * 60);
            // 재생시간 String으로 변환하기

            String strHours = String.format("%02d", hours);
            String strMinutes = String.format("%02d", minutes);
            String strSeconds = String.format("%02d", seconds);
            String strPlayTime = strHours + ":" + strMinutes + ":" + strSeconds;


            item_name[i] = files[i].getName();
            item_date[i] = String.valueOf(files[i].lastModified());
            item_time[i] = strPlayTime;
            item_size[i] = Long.toString(files[i].length()) + "byte";

            items.add(new recordItem(item_name[i], item_date[i], item_size[i], item_time[i]));
        }

        adapter = new recordAdapter(items);
        adapter.notifyDataSetChanged();

        recyclerView.setAdapter(adapter);



    }


    //권한설정 무조건 다시(2018-10)
    public void permissioncheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission_group.STORAGE)) ;
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) ;

                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                requestPermissions(new String[]{Manifest.permission_group.STORAGE}, 1);
            }
        }
    }

    public String DateCheck() {

        //녹음시작 시간구하기(녹음파일명)
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String getTime = df.format(new Date());


        return getTime;
    }


}
