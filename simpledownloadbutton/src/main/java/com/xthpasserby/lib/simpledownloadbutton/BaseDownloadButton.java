package com.xthpasserby.lib.simpledownloadbutton;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.xthpasserby.lib.DownloadStatus;
import com.xthpasserby.lib.DownloadTask;

public abstract class BaseDownloadButton extends AppCompatTextView implements View.OnClickListener, DownloadTask.ITaskStatusListener {
    // 按钮各种状态
    protected static final int BUTTON_STATUS_UNABLE = -1;
    protected static final int BUTTON_STATUS_NORMAL = 0;
    protected static final int BUTTON_STATUS_DOWNLOADING = 1;
    protected static final int BUTTON_STATUS_PAUSE = 2;
    protected static final int BUTTON_STATUS_INSTALL = 3;
    protected static final int BUTTON_STATUS_FAILURE = 4;
    protected static final int BUTTON_STATUS_WAIT = 5;

    // 按钮当前状态
    protected int mStatus = BUTTON_STATUS_NORMAL;

    private Context mContext;
    protected DownloadTask mDataBean;

    public BaseDownloadButton(Context context) {
        super(context);
        init(context);
    }

    public BaseDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseDownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        mContext = context;
        this.setClickable(true);
        this.setOnClickListener(this);
    }

    public void bindData(DownloadTask data) {
        this.mDataBean = data;
        changeButtonStatus();
        updateUI();
    }

    @Override
    public void onClick(View v) {
        if (null == mDataBean) return;
        switch (mStatus) {
            case BUTTON_STATUS_WAIT:
            case BUTTON_STATUS_UNABLE:
                break;
            case BUTTON_STATUS_INSTALL:
                break;
            case BUTTON_STATUS_PAUSE:
                mDataBean.resume();
                break;
            case BUTTON_STATUS_DOWNLOADING:
                mDataBean.pause();
                break;
            case BUTTON_STATUS_FAILURE:
                mDataBean.start();
                break;
            case BUTTON_STATUS_NORMAL:
            default:
                mDataBean.start();
                break;
        }
    }

    public void onActivityResume() {
        if (null == mDataBean) return;
        changeButtonStatus();
        updateUI();
    }

    protected abstract void updateUI();

    private void changeButtonStatus() {
        if (TextUtils.isEmpty(mDataBean.getDownloadUrl())) {
            mStatus = BUTTON_STATUS_UNABLE;
            return;
        }
        switch (mDataBean.getDownloadStatus()) {
            case SUCCESS:
                mStatus = BUTTON_STATUS_INSTALL;
                break;
            case RESUME_ERROR:
            case START:
            case DOWNLOADING:
            case RESUME:
                mStatus = BUTTON_STATUS_DOWNLOADING;
                break;
            case PAUSE:
                mStatus = BUTTON_STATUS_PAUSE;
                break;
            case FAILURE:
                mStatus = BUTTON_STATUS_FAILURE;
                break;
            case WAIT:
                mStatus = BUTTON_STATUS_WAIT;
                break;
            case CANCEL:
            case UN_START:
            default:
                mStatus = BUTTON_STATUS_NORMAL;
                break;
        }
    }

    protected String getDownloadSpeedString(int speed) {
        String mSpeed;
        if (speed > 1024) {
            mSpeed = String.format(mContext.getString(R.string.download_speed_mb), ((float)speed / 1024));
        } else {
            mSpeed = String.format(mContext.getString(R.string.download_speed_kb), speed);
        }

        return mSpeed;
    }

    @Override
    public void onStatusChange(DownloadStatus status) {
        changeButtonStatus();
        updateUI();
    }
}
