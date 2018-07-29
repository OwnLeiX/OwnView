package lx.own.view.offline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;


public class CircleProgressBar extends View {


    private Paint mBackPaint;
    private Paint mFrontPaint,mFrontShadowPaint;
    private Paint mTextPaint,mTextShadowPaint;
    private float mStrokeWidth,mHalfStrokeWidth;
    private float mRadius;
    private float mTextSize;
    private RectF mRect,mShadowRect;
    private int mWidth;
    private int mHeight;
    private DecimalFormat mDecimalFormat;

    private int mMax = 100;
    private int mProgress = 0;
    private float mRefreshProgress;
    private float mTargetProgress;
    private float mRefreshEvery;

    private long mUiThreadId;
    private Handler mHandler;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private Timer mTimer;


    public CircleProgressBar(Context context) {
        super(context);
        init();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mUiThreadId = Thread.currentThread().getId();
        mHandler = new Handler();
        mDecimalFormat = new DecimalFormat("00.00");
    }

    private void initPaint() {
        mBackPaint = new Paint();
        mBackPaint.setColor(0xEE000000);
        mBackPaint.setAntiAlias(true);
        mBackPaint.setStyle(Paint.Style.STROKE);
        mBackPaint.setStrokeWidth(mStrokeWidth);

        mFrontPaint = new Paint();
        mFrontPaint.setColor(0xFF330099);
        mFrontPaint.setAntiAlias(true);
        mFrontPaint.setStyle(Paint.Style.STROKE);
        mFrontPaint.setStrokeWidth(mStrokeWidth);

        mFrontShadowPaint = new Paint();
        mFrontShadowPaint.setColor(0xFF000000);
        mFrontShadowPaint.setAntiAlias(true);
        mFrontShadowPaint.setStyle(Paint.Style.STROKE);
        mFrontShadowPaint.setStrokeWidth(mStrokeWidth);


        mTextPaint = new Paint();
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mTextShadowPaint = new Paint();
        mTextShadowPaint.setColor(0xFF000000);
        mTextShadowPaint.setAntiAlias(true);
        mTextShadowPaint.setTextSize(mTextSize);
        mTextShadowPaint.setTextAlign(Paint.Align.CENTER);
        mTextShadowPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getRealSize(widthMeasureSpec);
        mHeight = getRealSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
        mStrokeWidth = mWidth > mHeight ? mHeight / 4 : mWidth / 4 ;
        mRadius = mWidth > mHeight ? mHeight / 4 : mWidth / 4;
        mHalfStrokeWidth = mStrokeWidth / 2;
        mTextSize = mRadius / 1.2f;
        initPaint();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        initRect();
        float angle = mProgress / (float) mMax * 360;
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, mBackPaint);
        canvas.drawArc(mShadowRect, -90, angle, false, mFrontShadowPaint);
        canvas.drawArc(mRect, -90, angle, false, mFrontPaint);
        String _text = mDecimalFormat.format(mProgress * 100.0f / mMax);
        canvas.drawText(_text, mWidth / 2 + 1, mHeight / 2 + 1, mTextShadowPaint);
        canvas.drawText(_text, mWidth / 2, mHeight / 2, mTextPaint);
    }

    public int getRealSize(int measureSpec) {
        int result = 1;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            result = (int) (mRadius * 2 + mStrokeWidth);
        } else {
            result = size;
        }

        return result;
    }

    private void initRect() {
        if (mRect == null) {
            mRect = new RectF();
            int viewSize = (int) (mRadius * 2);
            int left = (mWidth - viewSize) / 2 - (int)(mStrokeWidth / 3);
            int top = (mHeight - viewSize) / 2 - (int)(mStrokeWidth / 3);
            int right = left + viewSize + (int)(mStrokeWidth / 3 * 2);
            int bottom = top + viewSize + (int)(mStrokeWidth / 3 * 2);
            mRect.set(left, top, right, bottom);
        }
        if(mShadowRect == null) {
            mShadowRect = new RectF();
            int viewSize = (int) (mRadius * 2);
            int left = (mWidth - viewSize) / 2 - (int)(mStrokeWidth / 3) + 1;
            int top = (mHeight - viewSize) / 2 - (int)(mStrokeWidth / 3) + 1;
            int right = left + viewSize + (int)(mStrokeWidth / 3 * 2);
            int bottom = top + viewSize + (int)(mStrokeWidth / 3 * 2);
            mShadowRect.set(left, top, right, bottom);
        }
    }

    public void setMax(int max) {
        if(max < mProgress) {
            this.mProgress = max;
            this.mMax = max;
            return;
        }
        this.mMax = max;
    }

    public synchronized void setProgress(final int progress) {

        mRefreshProgress = mProgress;
        mTargetProgress = progress;
        if(mRefreshProgressRunnable == null)
            mRefreshProgressRunnable = new RefreshProgressRunnable();

        if( Math.abs((mTargetProgress - mRefreshProgress) / mMax) <= 0.01 ) {
            mProgress = progress;
            runOnUIThread(mRefreshProgressRunnable);
        }else if(mTargetProgress - mRefreshProgress > 0) {
            mRefreshEvery = (mTargetProgress - mRefreshProgress) / 100;
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mRefreshProgress += mRefreshEvery;
                    mProgress = (int) Math.floor(mRefreshProgress);
                    runOnUIThread(mRefreshProgressRunnable);
                    if(mRefreshProgress >= mTargetProgress) {
                        mProgress = progress;
                        runOnUIThread(mRefreshProgressRunnable);
                        this.cancel();
//                        mTimer.cancel();
//                        mTimer = null;
                    }
                }
            },1,10);
        }else if(mTargetProgress - mRefreshProgress < 0) {
            mRefreshEvery = (mRefreshProgress - mTargetProgress) / 100;
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mRefreshProgress -= mRefreshEvery;
                    mProgress = (int) Math.floor(mRefreshProgress);
                    runOnUIThread(mRefreshProgressRunnable);
                    if(mRefreshProgress <= mTargetProgress) {
                        mProgress = progress;
                        runOnUIThread(mRefreshProgressRunnable);
                        this.cancel();
//                        mTimer.cancel();
//                        mTimer = null;
                    }
                }
            },1,10);
        }
    }

    private class RefreshProgressRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (CircleProgressBar.this) {
               invalidate();
            }
        }
    }

    private void runOnUIThread(Runnable action) {
        if(mUiThreadId == Thread.currentThread().getId()) {
            action.run();
        }else {
            mHandler.post(action);
        }
    }
}
