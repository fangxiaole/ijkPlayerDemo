package com.example.ijkplayerdemo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Callback;
import okhttp3.Response;
import tv.danmaku.ijk.media.example.widget.media.AndroidMediaController;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "leleTest";
    @BindView(R.id.hud_view)
    TableLayout mHudView;
    @BindView(R.id.ijkvideoview)
    IjkVideoView mVideoView;
    @BindView(R.id.timeRuleView)
    TimeRuleView timeRuleView;
    @BindView(R.id.tx_time)
    TextView tx_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        AndroidMediaController mMediaController = new AndroidMediaController(this, false);
        requestLivePlay();
//        startLive();
        initView();
    }

    private void initView() {
        timeRuleView.setOnTimeChangedListener(new TimeRuleView.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(int newTimeValue) {
                tx_time.setText(TimeUtil.formatTimeHHmmss(newTimeValue));
            }

            @Override
            public void onTimeChangeEnd(int endTimeValue) {
                requestPlayback(startTime + endTimeValue);
            }

            @Override
            public void beforeDayClick() {

            }

            @Override
            public void afterDayClick() {

            }
        });
    }

    public void requestLivePlay() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("singleCMD", "rtspPlay");
        String url = UrlConstant.REQUEST_LIVE_URL;
        OKHttpUtil.getInstance().RequestGetNonSync(url, map, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                //onSupportCallback.onSupportFailed(flag);
                Log.e(TAG, "requestLivePlay onFailure");
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

    public void requestPlayback(long timeStamp) {
        Log.d(TAG, "requestPlayback timeStamp= " + timeStamp);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("singleCMD", "rtsp_playback|TimeStamp=" + timeStamp);
        OKHttpUtil.getInstance().RequestGetNonSync(UrlConstant.REQUEST_LIVE_URL, map, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "requestPlayback onFailure");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    String subBody = body.substring(body.indexOf("{"), body.indexOf("}") + 1);
                    Log.d(TAG, "subBody = " + subBody);
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

    @OnClick({R.id.bt_start, R.id.bt_stop, R.id.bt_start_record, R.id.bt_stop_record, R.id.bt_playback, R.id.bt_sd, R.id.bt_hd})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.bt_start) {
//            startLive();
            requestLivePlay();
        } else if (id == R.id.bt_stop) {
            stopLive();
        } else if (id == R.id.bt_start_record) {
            File file = getFolder();
            String path = file.getAbsolutePath() + File.separator;
            startRecord(path);
        } else if (id == R.id.bt_stop_record) {
            stopRecord();
        } else if (id == R.id.bt_sd) {
            hd();
        } else if (id == R.id.bt_hd) {
            sd();
        } else if (id == R.id.bt_playback) {
            timePartlist.clear();
            timeRuleView.setTimePartList(timePartlist);
            startTime = getZeroClockTimestamp(System.currentTimeMillis()) / 1000;
            endTime = startTime + 24 * 60 * 60;
            getRecordList(startTime, endTime);
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
        if (isRecord) {
            Toast.makeText(this, R.string.start_record, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.start_record_fail, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "startRecord isRecord=" + isRecord);
    }

    private void stopRecord() {
        boolean isStop = mVideoView.stopRecord();
        if (isStop) {
            Toast.makeText(this, R.string.stop_record, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.stop_record_fail, Toast.LENGTH_SHORT).show();
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

    //获取当天0点的时间戳
    public static long getZeroClockTimestamp(long time) {
        long zeroTimestamp = time - (time + TimeZone.getDefault().getRawOffset()) % (24 * 60 * 60 * 1000);
        return zeroTimestamp;
    }

    public void recordListToTimePartList(RecordListData recordListData, long recordStartTime, long recordEndTime) {
        if (recordListData != null && recordListData.getListInfo() != null) {
            for (int i = 0; i < recordListData.getListInfo().size(); i++) {
                RecordListData.ListInfoBean infoBean = recordListData.getListInfo().get(i);
                long endTime = infoBean.getStartTime() + infoBean.getRecordeDuration();
                if (infoBean.getStartTime() >= recordStartTime && infoBean.getStartTime() < recordEndTime
                        && endTime > recordStartTime && endTime <= recordEndTime) {
                    TimeRuleView.TimePart timePart = new TimeRuleView.TimePart();
                    timePart.setStartTime((int) (infoBean.getStartTime() - recordStartTime));
                    timePart.setEndTime((int) (infoBean.getStartTime() - recordStartTime + infoBean.getRecordeDuration()));
                    if (!tempTimePartlist.contains(timePart)) {
                        tempTimePartlist.add(timePart);
                    }
                }
            }
        }
        if (tempTimePartlist.size() > 0) {
            tempTimePartlist = getTimePartListMerge(tempTimePartlist);
        }
    }


    long startTime, endTime;
    List<TimeRuleView.TimePart> timePartlist = new ArrayList<>();
    List<TimeRuleView.TimePart> tempTimePartlist = new ArrayList<>();

    /**
     * 获取录像列表
     *
     * @param recordStartTime 开始时间（秒为单位）
     * @param recordEndTime   结束时间（秒为单位）
     */
    public void getRecordList(long recordStartTime, long recordEndTime) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("singleCMD", "get_recordlist");
        map.put("recordStartTime", String.valueOf(recordStartTime));
        map.put("recordEndTime", String.valueOf(recordEndTime));
        String url = UrlConstant.DEVICE_SERVER_DOMAIN_9898 + UrlConstant.GET_OTHER_SETTING;
        OKHttpUtil.getInstance().RequestGetNonSync(url, map, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "getRecordList onFailure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    Log.e(TAG, "getRecordList=" + body);
                    final RecordListData recordListData = new Gson().fromJson(body, RecordListData.class);
                    if ("success".equals(recordListData.getResult())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getRecordListSuccess(recordListData);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getRecordListSuccess(RecordListData data) {
        try {
            recordListToTimePartList(data, startTime, endTime);
            if (data.getEndflag() == 0) {
                //还没取完
                long sTime = data.getListInfo().get(data.getListInfo().size() - 1).getStartTime() + 1;
                getRecordList(sTime, endTime);
            } else {
                timePartlist.clear();
                timePartlist.addAll(tempTimePartlist);
                timeRuleView.setTimePartListAndCurrentTime(timePartlist, timePartlist.get(timePartlist.size() - 1).getEndTime());
                tx_time.setText(TimeUtil.formatTimeHHmmss(timePartlist.get(timePartlist.size() - 1).getEndTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 5s以内的文件合并
     *
     * @param timePartList
     * @return
     */
    public static List<TimeRuleView.TimePart> getTimePartListMerge(List<TimeRuleView.TimePart> timePartList) {
        for (int i = 0; i < timePartList.size(); i++) {
            if (i == 0) continue;
            int differTime = timePartList.get(i).startTime - timePartList.get(i - 1).endTime;
            if (differTime > 0 && differTime <= 5) {
                timePartList.get(i - 1).endTime = timePartList.get(i).startTime;
            }
        }

        List<TimeRuleView.TimePart> list = new ArrayList<>();
        long startTime = 0;
        for (int i = 0; i < timePartList.size(); i++) {
            if (timePartList.size() == 1) {
                TimeRuleView.TimePart part = new TimeRuleView.TimePart();
                part.startTime = (int) (timePartList.get(i).startTime);
                part.endTime = (int) (timePartList.get(i).endTime);
                list.add(part);
            } else {
                if (i != 0) {
                    if (timePartList.get(i).startTime <= timePartList.get(i - 1).endTime && timePartList.get(i - 1).endTime - timePartList.get(i).startTime <= 5 && timePartList.get(i).endTime > timePartList.get(i - 1).endTime) {
                        if (i == timePartList.size() - 1) {
                            TimeRuleView.TimePart part = new TimeRuleView.TimePart();
                            part.startTime = (int) startTime;
                            part.endTime = (int) timePartList.get(i).endTime;
                            list.add(part);
                        }
                    } else {
                        if (i == timePartList.size() - 1) {
                            TimeRuleView.TimePart part1 = new TimeRuleView.TimePart();
                            part1.startTime = (int) startTime;
                            part1.endTime = (int) timePartList.get(i - 1).endTime;
                            list.add(part1);

                            TimeRuleView.TimePart part2 = new TimeRuleView.TimePart();
                            part2.startTime = (int) timePartList.get(i).startTime;
                            part2.endTime = (int) timePartList.get(i).endTime;
                            list.add(part2);
                        } else {
                            TimeRuleView.TimePart part = new TimeRuleView.TimePart();
                            part.startTime = (int) startTime;
                            part.endTime = (int) timePartList.get(i - 1).endTime;
                            list.add(part);
                        }
                        startTime = timePartList.get(i).startTime;
                    }
                } else {
                    startTime = timePartList.get(i).startTime;
                }
            }
        }
        return list;
    }
}