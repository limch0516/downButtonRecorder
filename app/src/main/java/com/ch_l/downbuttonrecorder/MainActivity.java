package com.ch_l.downbuttonrecorder;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainAct";
    //    ActionBar actionBar;
    boolean mediaplay = false;
    int recordplayTime = 0;

    long firstTouchTime = 0L;
    MediaPlayer mediaPlayer;
    public static MediaRecorder recorder;
    SeekBar seekBar;
    ImageButton btnPause, btnRecord, btnStop, btnStart, btnrestart;
    //textView
    TextView tv_stname, tv_ding, tv_fduration;
    String playname, sdcard;


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


        //recycleView 설정
        setRecyclerView();


        //RecyclerView 새로고침
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setData();
                refreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), "새로고침 됨", Toast.LENGTH_SHORT).show();
            }
        });


        tv_stname = (TextView) findViewById(R.id.tv_stname);
        tv_ding = (TextView) findViewById(R.id.tv_ding);
        tv_fduration = (TextView) findViewById(R.id.tv_fduration);


        btnStop = (ImageButton) findViewById(R.id.buttonStop);


        //파일 재생버튼
        startplay();
        playPause();
        //녹음시작버튼
        startRecordButton();
        restartButton();
        seekbarchange();

        //녹음종료,재생종료버튼
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnrestart.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setClickable(true);
                if (recorder != null) {
                    Intent intent = new Intent(getApplicationContext(), RecordService.class);
                    stopService(intent);
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
                    mediaplay = false;


                    Toast.makeText(getApplicationContext(), "재생 중지", Toast.LENGTH_SHORT).show();
                }
                setData();
            }
        });
    }

    //액션바생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:  // 설정 메뉴 선택
                Intent intent = new Intent(this, settingActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
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


    private void setRecyclerView() {
        adapter = new recordAdapter(items);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        setData();
    }

    void setData() {
        items.clear();

        //파일 정보 리스트에 넣기
        //불러올 파일 경로설정
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/downButtonRecorder");
        //불러올파일 확장자

        //불러온 파일 배열에 넣기
        File[] files = file.listFiles();
        //파일 정보 넣을 배열 만들기

        //파일정보 배열만들기
        String[] item_name = new String[files.length];
        String[] item_time = new String[files.length];
        String[] item_size = new String[files.length];
        String[] item_date = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            //재생시간 만들기
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

        adapter.notifyDataSetChanged();
        //아이템 터치
        adapter.setOnItemClickListener(new recordAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

                tv_stname.setText(items.get(position).item_name);
                playname = items.get(position).item_name;

                tv_fduration.setText("재생 시간: " + items.get(position).item_time);
            }

            @Override
            public void onItemLongClick(int position, View v) {
                Toast.makeText(MainActivity.this, position + "번째 기이이잉ㄹ게", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void restartButton() {
        btnrestart = (ImageButton) findViewById(R.id.buttonRestart);
        btnrestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaplay = true;
                new seekbarchangeTread().start();
                btnPause.setClickable(true);
                mediaPlayer.start();


            }
        });

    }

    // 재생시작
    private void startplay() {
        btnStart = (ImageButton) findViewById(R.id.buttonStart);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setVisibility(View.GONE);
                btnrestart.setVisibility(View.VISIBLE);
                btnrestart.setClickable(false);
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Log.d(TAG, "paly start");
                try {

                    mediaPlayer = new MediaPlayer();
                    String playname2 = sdcard + "/" + playname;
                    Toast.makeText(MainActivity.this, playname2 + "재생합니당", Toast.LENGTH_SHORT).show();
                    FileInputStream fis = new FileInputStream(playname2);

                    FileDescriptor fd = fis.getFD();

                    mediaPlayer.setDataSource(fd);


//                        mediaPlayer.setDataSource(playname2);
                    mediaPlayer.prepare();
                    mediaPlayer.start();


                    mediaplay = true;
                    seekBar.setMax(mediaPlayer.getDuration()); // 음악의 시간을 최대로 설정
                    new seekbarchangeTread().start();
//                        Toast.makeText(MainActivity.this, "재생 시작", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "왜오류가 날까요");
                }

            }
        });

    }

    void playPause() {
        btnPause = (ImageButton) findViewById(R.id.buttonPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPause.setClickable(false);
                btnrestart.setClickable(true);

                if (mediaplay == true) {
                    mediaplay = false;
                    mediaPlayer.pause();
                }

            }
        });


    }

    //녹음시작
    public void startRecordButton() {
        btnRecord = (ImageButton) findViewById(R.id.buttonRecord);
        //버튼 누를때 파일생성
        //저장경로 지정
        sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/downButtonRecorder";
        File path = new File(sdcard);
        if (!path.exists()) {
            path.mkdirs();
        }


        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                }
                tv_fduration.setText("녹음가능용량 넣기gettotalspace()");
                recorder = new MediaRecorder();
                Intent intent = new Intent(getApplicationContext(), RecordService.class);
                startService(intent);

                Log.d(TAG, "record start");


                //쓰레드 재실행 오류 방지
                recordtime = new BackgroundThread();
                recordtime.start();


            }
        });
    }


    static class DateCheck {


        public String DateCheck() {

            //녹음시작 시간구하기(녹음파일명)
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
            String getTime = df.format(new Date());

            return getTime;
        }
    }

    //시크바 쓰레드드
    private class seekbarchangeTread extends Thread {
        @Override
        public void run() { // 쓰레드가 시작되면 콜백되는 메서드
            // 씨크바 막대기 조금씩 움직이기 (노래 끝날 때까지 반복)
            if (mediaplay == true) {
                while (mediaplay) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    SystemClock.sleep(500);
                }
                seekBar.setProgress(0);

            } else {
                seekBar.setProgress(0);
            }


        }


    }


    @Override
    public void onBackPressed() {
        long nowTouchTime = System.currentTimeMillis();

        if ((nowTouchTime - firstTouchTime) <= 3000L) {
            super.onBackPressed();
        } else {

            Toast.makeText(
                    getApplicationContext(),
                    "[이전 버튼](super.onBackPressed())을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();

            firstTouchTime = nowTouchTime;
        }

    }

    //녹음시간 계산 쓰레드
    private class BackgroundThread extends Thread {

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        @Override
        public void run() {
            if (recorder == null) {
                return;
            }
            while (


                    recorder != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        recordplayTime++;
                        String re = String.valueOf(recordplayTime);

                        Log.d(TAG, "recordplayTime ing");

                        tv_ding.setText("녹음중:" + re + "sec");
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





