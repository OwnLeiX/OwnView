package lx.own.view.offline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleNoProgressBar extends View {


    private Paint mBackPaint;
    private Paint mFrontPaint, mFrontShadowPaint;
    private float mStrokeWidth, mHalfStrokeWidth;
    private float mRadius;
    private RectF mRect, mShadowRect;
    private int mWidth;
    private int mHeight;
    private float mStartAngle;
    private boolean flag = true;

    private int mMax = 360;
    private int mProgress = 260;


    public CircleNoProgressBar(Context context) {
        super(context);
        init();
    }

    public CircleNoProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleNoProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStartAngle = 90;
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
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getRealSize(widthMeasureSpec);
        mHeight = getRealSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
        mStrokeWidth = mWidth > mHeight ? mHeight / 5 : mWidth / 5;
        mRadius = mWidth > mHeight ? mHeight / 4 : mWidth / 4;
        mHalfStrokeWidth = mStrokeWidth / 2;
        initPaint();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        initRect();
        float angle = mProgress / (float) mMax * 360;
        canvas.drawArc(mShadowRect, mStartAngle, angle, false, mFrontShadowPaint);
        canvas.drawArc(mRect, mStartAngle, angle, false, mFrontPaint);
        if (mStartAngle >= 450)
            mStartAngle = 90.0f;

        if(mProgress < 50)
            flag = true;
        if(mProgress > 310)
            flag = false;
        if (flag) {
            mProgress+=5;
            mStartAngle+=10;
            invalidate();
        } else{
            mProgress -= 5;
            mStartAngle+=10;
            invalidate();
        }

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
            int left = (mWidth - viewSize) / 2 - (int) (mStrokeWidth / 3);
            int top = (mHeight - viewSize) / 2 - (int) (mStrokeWidth / 3);
            int right = left + viewSize + (int) (mStrokeWidth / 3 * 2);
            int bottom = top + viewSize + (int) (mStrokeWidth / 3 * 2);
            mRect.set(left, top, right, bottom);
        }
        if (mShadowRect == null) {
            mShadowRect = new RectF();
            int viewSize = (int) (mRadius * 2);
            int left = (mWidth - viewSize) / 2 - (int) (mStrokeWidth / 3) + 1;
            int top = (mHeight - viewSize) / 2 - (int) (mStrokeWidth / 3) + 1;
            int right = left + viewSize + (int) (mStrokeWidth / 3 * 2);
            int bottom = top + viewSize + (int) (mStrokeWidth / 3 * 2);
            mShadowRect.set(left, top, right, bottom);
        }
    }
}
