package com.ch_l.downbuttonrecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.text.LocaleDisplayNames;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    int recordplayTime = 0;
    private static String RECORDED_FILE;
    MediaPlayer mediaPlayer;
    MediaRecorder recorder;
    SeekBar seekBar;
    Button btnPause, btnRecord, btnStop, btnStart;
    //textView
    TextView tv_stname, tv_ding, tv_fduration;
    String playname, sdcard;
    LinearLayout linearLayout;

    Thread recordtime;


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


        tv_stname = (TextView) findViewById(R.id.tv_stname);
        tv_ding = (TextView) findViewById(R.id.tv_ding);
        tv_fduration = (TextView) findViewById(R.id.tv_fduration);
        linearLayout = (LinearLayout) findViewById(R.id.Linear1);


        //저장경로 지정

//        File sdcard = Environment.getExternalStorageDirectory();

        sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/downButtonRecorder";
        File path = new File(sdcard);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(sdcard, DateCheck() + ".3gp");

        RECORDED_FILE = file.getAbsolutePath();

        btnPause = (Button) findViewById(R.id.buttonPause);

        btnStop = (Button) findViewById(R.id.buttonStop);


        mediaPlayer = new MediaPlayer();

        //파일 재생버튼
        startplay();

        //녹음시작버튼
        startRecordButton();

        seekbarchange();

        //녹음종료,재생종료버튼
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    recordtime.interrupt();
                    recordplayTime = 0;
                    tv_ding.setText("");


                    Log.d(TAG, "recordplayTime die");
                    Toast.makeText(getApplicationContext(), "녹음 중지", Toast.LENGTH_SHORT).show();
                }
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    Toast.makeText(getApplicationContext(), "재생 중지", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void seekbarchange() {
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //아이템 터치
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    View rev = rv.findChildViewUnder(e.getX(), e.getY());
                    int position = rv.getChildAdapterPosition(rev);

                    TextView textView = rv.getChildViewHolder(rev).itemView.findViewById(R.id.item_name);
                    TextView textView1 = rv.getChildViewHolder(rev).itemView.findViewById(R.id.item_time);
//                    tv_stname.setText(items.get(position).item_name);
                    playname = textView.getText().toString();
                    tv_stname.setText(textView.getText().toString());
                    tv_fduration.setText("재생 시간: " + textView1.getText().toString());


                    return false;
                }
                return false;
            }


            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

    // 재생시작
    private void startplay() {
        btnStart = (Button) findViewById(R.id.buttonStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Log.d(TAG, "paly start");
                try {
                    mediaPlayer = new MediaPlayer();
                    String playname2 = sdcard + "/" + playname;
                    mediaPlayer.setDataSource(playname2);

                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mediaPlayer.isPlaying()) {
                    new Thread() {
                        public void run() {
                            // 음악이 계속 작동 중이라면

                            while (mediaPlayer.isPlaying()) {
                                seekBar.setMax(mediaPlayer.getDuration()); // 음악의 시간을 최대로 설정
                                seekBar.setProgress(mediaPlayer.getCurrentPosition()); // 현재 위치를
                                // 지정
                                SystemClock.sleep(100);
                            }
                            seekBar.setProgress(0);
                        }
                    }.start();
                }else{
                    seekBar.setProgress(0);
                }

                Toast.makeText(getApplicationContext(), playname, Toast.LENGTH_SHORT).show();

            }
        });
    }

    //녹음시작
    public void startRecordButton() {
        btnRecord = (Button) findViewById(R.id.buttonRecord);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                }
                tv_fduration.setText("녹음가능용량 넣기gettotalspace()");
                Log.d(TAG, "record start");

                recorder = new MediaRecorder();

                //오디오 입력 형식 설정
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //파일 저장 방식 설정(확장자)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                //코덱 설정
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                //파일 저장 경로 설정
                recorder.setOutputFile(RECORDED_FILE);
                try {
                    Toast.makeText(getApplicationContext(), "녹음 시작", Toast.LENGTH_SHORT).show();

                    recorder.prepare();
                    recorder.start();

                    //쓰레드 재실행 오류 방지
                    recordtime = new BackgroundThread();
                    recordtime.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    //권한설정 무조건 다시(2018.10.24)
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

    //    public void recordtime() {
//        final Thread timer = new Thread() {
//            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//
//            @Override
//            public void run() {
//                if (recorder == null) {
//                    recordplayTime=0;
//                    Log.d(TAG,"recordplayTime die");
//                    return;
//                }
//                while (recorder != null) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            recordplayTime++;
//                            String re=String.valueOf(recordplayTime);
//                            Log.d(TAG,"recordplayTime ing");
//                            tv_ding.setText(re);
//                        }
//                    });
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        timer.start();
//    }
    private class BackgroundThread extends Thread {

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        @Override
        public void run() {
            if (recorder == null) {
                return;
            }
            while (recorder != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        recordplayTime++;
                        String re = String.valueOf(recordplayTime);

                        Log.d(TAG, "recordplayTime ing");

                        tv_ding.setText("녹음중:"+re+"sec");
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}





