package com.xthpasserby.lib.simpledownloadbutton;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import com.xthpasserby.lib.DownloadTask;


public class MultiDownloadButton extends BaseDownloadButton {
    private static final int PROGRESS_RADIUS = 20;

    private Context mContext;

    private Paint mPaint = new Paint();
    private Paint mTextPaint = new Paint();

    private int progress = 0;
    private final int mMax = 100;

    private Bitmap mStartBitmap;
    private Bitmap mPauseBitmap;

    private RectF progressRectF = new RectF();
    private RectF mButtonRectF = new RectF();
    private float mButtonRadius;
    private Point mTopCentPoint = new Point();
    private Point mCentPoint = new Point();
    private Point mStartPoint = new Point();
    private Point mPausePoint = new Point();
    private int mProgressRadius;
    private int mTextOffset;
    private String mSpeed;
    private int mStrokeButtonWidth;
    private int mStrokeProgressWidth;
    private int colorBlue = 0xFF6BAFFF;
    private int colorGrey = 0xFFEEEEEE;

    public MultiDownloadButton(Context context) {
        this(context, null);
    }

    public MultiDownloadButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiDownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mStrokeButtonWidth = dp2px(1);
        mStrokeProgressWidth = dp2px(2);

        setGravity(Gravity.CENTER);

        mPaint.setAntiAlias(true);
        mPaint.setColor(colorBlue);
        mPaint.setStrokeWidth(mStrokeButtonWidth);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(colorBlue);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER); // 文本居中
        mTextPaint.setTextSize(dp2px(10));

        mStartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.multi_button_start);
        mPauseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.multi_button_pause);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mTextOffset = dp2px(15);
        mProgressRadius = dp2px(PROGRESS_RADIUS);
        mTopCentPoint.set(getWidth() / 2, mProgressRadius + dp2px(4));
        mButtonRadius = dp2px(4);
        mCentPoint.set(getWidth() / 2, getHeight() / 2);
        if (mStartBitmap != null) {
            mStartPoint.set(mTopCentPoint.x - mStartBitmap.getWidth() / 2, mTopCentPoint.y - mStartBitmap.getHeight() / 2);
        }
        if (mPauseBitmap != null) {
            mPausePoint.set(mTopCentPoint.x - mPauseBitmap.getWidth() / 2, mTopCentPoint.y - mPauseBitmap.getHeight() / 2);
        }
        mButtonRectF.set(mCentPoint.x - dp2px(30), mCentPoint.y - dp2px(15),
                mCentPoint.x + dp2px(30), mCentPoint.y + dp2px(15));
        progressRectF.set(mTopCentPoint.x - mProgressRadius, mTopCentPoint.y - mProgressRadius,
                mTopCentPoint.x + mProgressRadius, mTopCentPoint.y + mProgressRadius);
    }

    @Override
    protected void updateUI() {
        setText("");
        switch (mStatus) {
            case BUTTON_STATUS_INSTALL:
                break;
            case BUTTON_STATUS_DOWNLOADING:
            case BUTTON_STATUS_PAUSE:
                setProgress(mDataBean.getPercentage());
                break;
            case BUTTON_STATUS_NORMAL:
            default:
                    break;
        }
        invalidate();
    }

    @Override
    public void onProgress(DownloadTask task) {
        if (null == mDataBean || !TextUtils.equals(task.getDownloadUrl(), mDataBean.getDownloadUrl())) return;
        setProgress(mDataBean.getPercentage());
    }

    private void setProgress(int progress) {
        if (progress > mMax) progress = mMax;
        if (progress < 0) progress = 0;
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCustom(canvas);
        super.onDraw(canvas);
    }

    private void drawCustom(Canvas canvas) {
        switch (mStatus) {
            case BUTTON_STATUS_NORMAL:
                drawStrokeButton(canvas);
                break;
            case BUTTON_STATUS_DOWNLOADING:
                drawProgress(canvas);
                break;
            case BUTTON_STATUS_PAUSE:
                drawProgress(canvas);
                break;
            case BUTTON_STATUS_INSTALL:
                drawButton(canvas);
                break;
        }
    }

    private void drawStrokeButton(Canvas canvas) {
        mPaint.setStrokeWidth(mStrokeButtonWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(colorBlue);
        canvas.drawRoundRect(mButtonRectF, mButtonRadius, mButtonRadius, mPaint);
        setTextColor(colorBlue);
        setText("下载");
    }

    private void drawProgress(Canvas canvas) {
        mPaint.setStrokeWidth(mStrokeProgressWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(colorGrey);
        canvas.drawArc(progressRectF, 0, 360, false, mPaint);
        mPaint.setColor(colorBlue);
        canvas.drawArc(progressRectF, 0, 360 * ((float) progress / mMax), false, mPaint);

        switch (mStatus) {
            case BUTTON_STATUS_DOWNLOADING:
                canvas.drawBitmap(mPauseBitmap, mPausePoint.x, mPausePoint.y, mPaint);
                mSpeed = getDownloadSpeedString(mContext, mDataBean.getSpeed());
                canvas.drawText(mSpeed, mTopCentPoint.x + mProgressRadius - mTextPaint.measureText(mSpeed) / 2, mTopCentPoint.y + mProgressRadius + mTextOffset, mTextPaint);
                break;
            case BUTTON_STATUS_PAUSE:
                canvas.drawBitmap(mStartBitmap, mStartPoint.x, mStartPoint.y, mPaint);
                canvas.drawText("已暂停", mTopCentPoint.x + mProgressRadius - mTextPaint.measureText("已暂停") * 2 / 3, mTopCentPoint.y + mProgressRadius + mTextOffset, mTextPaint);
                break;
        }
    }

    private void drawButton(Canvas canvas) {
        switch (mStatus) {
            case BUTTON_STATUS_INSTALL:
                mPaint.setColor(colorGrey);
                setText("已完成");
                break;
            default:
                mPaint.setColor(colorBlue);
                setText("下载");
        }
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(mButtonRectF, mButtonRadius, mButtonRadius, mPaint);
        setTextColor(Color.WHITE);
    }

    private int dp2px(final int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
