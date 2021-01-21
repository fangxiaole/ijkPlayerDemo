package tv.danmaku.ijk.media.example.widget.media;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IpCameraView extends FrameLayout {

    private static final String TAG = "IpCameraView";
    private Handler mHandler = new Handler();
    private String mName = "admin";
    private String mPassword = "123456";
    private IjkVideoView mIjkVideoView;
    private String mRtspUrl;

    public IpCameraView(@NonNull Context context) {
        this(context, null);
    }

    public IpCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IpCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mIjkVideoView = new IjkVideoView(context);
        addView(mIjkVideoView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        TableLayout tabLayout = new TableLayout(context);
        tabLayout.setBackgroundColor(0x80000000);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mIjkVideoView.setHudView(tabLayout);
    }


    public void playRTSP(String url, long delay) {
        mRtspUrl = url;
        if (delay > 0) {
            mHandler.postDelayed(mPlayRunnable, delay);
        } else {
           mHandler.post(mPlayRunnable);
        }
    }

    private Runnable mPlayRunnable = new Runnable() {
        @Override
        public void run() {
            if (TextUtils.isEmpty(mRtspUrl)) {
                Toast.makeText(getContext(), "rtsp路径获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!mRtspUrl.startsWith("rtsp://")) {
                Toast.makeText(getContext(), "rtsp路径格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.e(TAG, "rtsp地址：" + mRtspUrl);
            mIjkVideoView.setVideoPath(mRtspUrl);
            mIjkVideoView.start();
        }
    };

//    private void snapshot() {
//       mIjkVideoView.getShortcut();
//    }
//
//    public void startRecord(String path) {
//        mIjkVideoView.startRecord();
//    }
//
//    public void stopRecord() {
//        mIjkVideoView.stopRecord();
//    }
//
//    public void screenShot() {
//        mIjkVideoView.snapshotPicture();
//    }
//
//    public void closeListener() {
//        mIjkVideoView.setVolume();
//    }
}
