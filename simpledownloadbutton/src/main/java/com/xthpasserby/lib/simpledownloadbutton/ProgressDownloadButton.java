package com.xthpasserby.lib.simpledownloadbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;


/**
 * 带进度条的下载按钮
 * 注意事项参考{@link BaseDownloadButton}
 */
public class ProgressDownloadButton extends BaseDownloadButton {
    private boolean isShowProgress = true;
    private int progress = 0;
    private final int mMax = 100;

    private ClipDrawable mProgressDrawable;

    public ProgressDownloadButton(Context context) {
        super(context);
        initCustomProperty(context, null);
    }

    public ProgressDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomProperty(context, attrs);
    }

    public ProgressDownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomProperty(context, attrs);
    }

    private void initCustomProperty(Context context, AttributeSet attrs) {
        Drawable drawable = null;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomProgressButton);
            assert a != null;
//            mMax = a.getInteger(R.styleable.CustomProgressButton_max, mMax);
            drawable = a.getDrawable(R.styleable.CustomProgressButton_progressDrawable);
            progress = a.getInteger(R.styleable.CustomProgressButton_progress, progress);
            a.recycle();
        }

        if (null == drawable)
            drawable = getResources().getDrawable(R.drawable.rectangle_blue_solid_bg);
        mProgressDrawable = new ClipDrawable(drawable, Gravity.START, ClipDrawable.HORIZONTAL);
    }

    @Override
    protected void updateUI() {
        switch (mStatus) {
            case BUTTON_STATUS_UNABLE:
                setProgressIsShow(false);
                setText("下载");
                setBackgroundResource(R.drawable.rectangle_grep_solid_bg);
                break;
            case BUTTON_STATUS_INSTALL:
                setProgressIsShow(false);
                setText("已完成");
                setBackgroundResource(R.drawable.rectangle_grep_solid_bg);
                break;
            case BUTTON_STATUS_DOWNLOADING:
                setProgressIsShow(true);
                setProgress(mDataBean.getPercentage());
                setText(String.format("%1$s%%", mDataBean.getPercentage()));
                setBackgroundResource(R.drawable.rectangle_grep_solid_bg);
                break;
            case BUTTON_STATUS_PAUSE:
                setProgressIsShow(true);
                setProgress(mDataBean.getPercentage());
                setText("继续");
                setBackgroundResource(R.drawable.rectangle_grep_solid_bg);
                break;
            case BUTTON_STATUS_FAILURE:
                setProgressIsShow(false);
                setText("重新下载");
                setBackgroundResource(R.drawable.rectangle_blue_solid_bg);
                break;
            case BUTTON_STATUS_NORMAL:
            default:
                setProgressIsShow(false);
                setText("下载");
                setBackgroundResource(R.drawable.rectangle_blue_solid_bg);
                break;
        }
    }

    @Override
    public void onProgress(int percentage) {
        if (mStatus == BUTTON_STATUS_DOWNLOADING) setText(String.format("%1$s%%", percentage));
        setProgress(percentage);
    }

    private void setProgressIsShow(boolean isShow) {
        isShowProgress = isShow;
    }

    private void setProgress(int progress) {
        if (progress > mMax) progress = mMax;
        if (progress < 0) progress = 0;
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawProgress(canvas);
        super.onDraw(canvas);
    }

    private void drawProgress(Canvas canvas) {
        if (!isShowProgress) return;
        if (mProgressDrawable.getBounds().bottom <= 0) {
            mProgressDrawable.setBounds(0, 0, getWidth(), getHeight());
        }

        mProgressDrawable.setLevel(10000 * progress / mMax);
        mProgressDrawable.draw(canvas);
    }
}
