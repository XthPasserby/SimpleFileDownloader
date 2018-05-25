package com.xthpasserby.lib.simpledownloadbutton;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.xthpasserby.lib.DownloadTask;
import com.xthpasserby.lib.IDownloadListener;

public abstract class BaseDownloadButton extends AppCompatTextView implements View.OnClickListener, IDownloadListener {
    // 按钮各种状态
    protected static final int BUTTON_STATUS_UNABLE = -1;
    protected static final int BUTTON_STATUS_NORMAL = 0;
    protected static final int BUTTON_STATUS_DOWNLOADING = 1;
    protected static final int BUTTON_STATUS_PAUSE = 2;
    protected static final int BUTTON_STATUS_INSTALL = 3;

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
            case START:
            case DOWNLOADING:
            case RESUME:
                mStatus = BUTTON_STATUS_DOWNLOADING;
                break;
            case PAUSE:
                mStatus = BUTTON_STATUS_PAUSE;
                break;
            default:
                mStatus = BUTTON_STATUS_NORMAL;
                break;
        }
    }

    protected String getDownloadSpeedString(Context mContext, int speed) {
        String mSpeed;
        if (speed > 1024) {
            mSpeed = String.format(mContext.getString(R.string.download_speed_mb), ((float)speed / 1024));
        } else {
            mSpeed = String.format(mContext.getString(R.string.download_speed_kb), speed);
        }

        return mSpeed;
    }

    @Override
    public void onStatusChange(DownloadTask task) {
        if (null == mDataBean || !TextUtils.equals(task.getDownloadUrl(), mDataBean.getDownloadUrl())) return;

        changeButtonStatus();
        updateUI();
    }

    @Override
    public void onStorageOverFlow() {

    }
}
