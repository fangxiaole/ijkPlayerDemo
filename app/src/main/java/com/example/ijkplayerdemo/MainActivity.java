package com.example.ijkplayerdemo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Callback;
import okhttp3.Response;
import tv.danmaku.ijk.media.example.widget.media.AndroidMediaController;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "leleTest";
    IjkVideoView mVideoView;
    TableLayout mHudView;
    TextView bt_start;
    TextView bt_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mVideoView = (IjkVideoView) findViewById(R.id.ijkvideoview);
        mHudView = (TableLayout) findViewById(R.id.hud_view);
        AndroidMediaController mMediaController = new AndroidMediaController(this, false);
        requestLivePlay();
        bt_start = findViewById(R.id.bt_start);
        bt_stop = findViewById(R.id.bt_stop);
        Button bt_start_record = findViewById(R.id.bt_start_record);
        Button bt_stop_record = findViewById(R.id.bt_stop_record);
        Button bt_sd = findViewById(R.id.bt_sd);
        Button bt_hd = findViewById(R.id.bt_hd);
        bt_start_record.setOnClickListener(this);
        bt_stop_record.setOnClickListener(this);
        bt_start.setOnClickListener(this);
        bt_stop.setOnClickListener(this);
        bt_sd.setOnClickListener(this);
        bt_hd.setOnClickListener(this);
//        startLive();
    }

    public void requestLivePlay() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("singleCMD", "rtspPlay");
        String url = UrlConstant.REQUEST_LIVE_URL;
        OKHttpUtil.getInstance().RequestGetNonSync(url, map, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                //onSupportCallback.onSupportFailed(flag);
                Log.e(TAG, "onFailure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    String subBody = body.substring(body.indexOf("{"), body.indexOf("}") + 1);
                    Log.e(TAG, "subBody=" + subBody);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            live();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void live() {
        final String mVideoPath = UrlConstant.RTSP_URL0;
        mVideoView.setVideoURI(Uri.parse(mVideoPath));
        mVideoView.start();
    }

    private void startLive() {
        final String mVideoPath = UrlConstant.RTSP_URL0;
        mVideoView.setVideoURI(Uri.parse(mVideoPath));
//        mVideoView.setRender(IjkVideoView.RENDER_TEXTURE_VIEW);
        mVideoView.start();
    }

    private void stopLive() {
        mVideoView.stopPlayback();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_start:
//                startLive();
                requestLivePlay();
                break;
            case R.id.bt_stop:
                stopLive();
                break;
            case R.id.bt_start_record:
                File file = getFolder();
                String path = file.getAbsolutePath() + File.separator;
                startRecord(path);
                break;
            case R.id.bt_stop_record:
                stopRecord();
                break;
            case R.id.bt_sd:
                sd();
                break;
            case R.id.bt_hd:
                hd();
                break;

        }
    }

    private void sd() {
        final String mVideoPath = UrlConstant.RTSP_URL0;
        mVideoView.setVideoURI(Uri.parse(mVideoPath));
    }

    private void hd() {
        final String mVideoPath = UrlConstant.RTSP_URL1;
        mVideoView.setVideoURI(Uri.parse(mVideoPath));
    }

    private void startRecord(String fileName) {
        boolean isRecord = mVideoView.startRecord(fileName, "ll.mp4");
        if(isRecord){
            Toast.makeText(this,R.string.start_record,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,R.string.start_record_fail,Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "startRecord isRecord=" + isRecord);
    }

    private void stopRecord() {
        boolean isStop = mVideoView.stopRecord();
        if(isStop){
            Toast.makeText(this,R.string.stop_record,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,R.string.stop_record_fail,Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "stopRecord isStop=" + isStop);
    }

    private File getFolder() {
        File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File uidFolder = new File(rootFolder.getAbsolutePath());
        File targetFolder = new File(uidFolder.getAbsolutePath() + File.separator + "leletest" + File.separator);
        if (!rootFolder.exists()) {
            rootFolder.mkdir();
        }
        if (!uidFolder.exists()) {
            uidFolder.mkdir();
        }
        if (!targetFolder.exists()) {
            targetFolder.mkdir();
        }
        return targetFolder;
    }
}