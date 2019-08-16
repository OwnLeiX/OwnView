package lx.own.view.online;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import lx.own.R;

/**
 * <b>简单的progressBar</b><br/>
 * 仅有画圆环进度功能，和设置一个开口
 *
 * @author Lei.X
 * Created on 2019/3/25.
 */
public class SimpleCircleProgressBar extends View {

    private long mMax, mProgress;
    private volatile float calculatedSweepAngle, drawnSweepAngle;
    private float startAngle, endAngle;//开始和结束的角度
    private boolean clockwise;//顺时针画
    private int mStrokeWidth, mShadowOffsetX, mShadowOffsetY;
    @ColorInt
    private int mColor, mShadowColor;
    private final Paint mPaint;
    private final RectF mRect;


    public SimpleCircleProgressBar(Context context) {
        this(context, null);
    }

    public SimpleCircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SimpleCircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttrs(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mStrokeWidth);
        mRect = new RectF();
        calculatedSweepAngle = (endAngle - startAngle) * mProgress / mMax;
    }

//    public SimpleCircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    public void setMax(long max) {
        this.mMax = max;
        if (mMax < 0)
            mMax = 0;
        calculSweepAngle();
    }

    public void setProgress(long progress) {
        this.mProgress = progress;
        if (mProgress < 0)
            mProgress = 0;
        calculSweepAngle();
    }

    public void setMaxAndProgress(long max, long progress) {
        this.mMax = max;
        this.mProgress = progress;
        if (mProgress < 0)
            mProgress = 0;
        if (mMax < 0)
            mMax = 0;
        calculSweepAngle();
    }

    public void setMaxAndRemainProgress(long max, long remainProgress) {
        this.mMax = max;
        this.mProgress = this.mMax - remainProgress;
        if (mProgress < 0)
            mProgress = 0;
        if (mMax < 0)
            mMax = 0;
        calculSweepAngle();
    }

    public void setRemainProgress(long remainProgress) {
        this.mProgress = this.mMax - remainProgress;
        if (mProgress < 0)
            mProgress = 0;
        calculSweepAngle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int shadowSpace = mShadowOffsetX + mShadowOffsetY;
        mRect.left = ViewCompat.getPaddingStart(this) + mStrokeWidth + shadowSpace;
        mRect.top = getPaddingTop() + mStrokeWidth + shadowSpace;
        mRect.right = w - mRect.left - ViewCompat.getPaddingEnd(this);
        mRect.bottom = h - mRect.top - getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (calculatedSweepAngle != 0) {
            if (mShadowOffsetX > 0 || mShadowOffsetY > 0) {
                mPaint.setColor(mShadowColor);
                canvas.save();
                canvas.translate(mShadowOffsetX, mShadowOffsetY);
                if (clockwise) {
                    canvas.drawArc(mRect, endAngle, calculatedSweepAngle, false, mPaint);
                } else {
                    canvas.drawArc(mRect, endAngle, -calculatedSweepAngle, false, mPaint);
                }
                canvas.restore();
            }
            mPaint.setColor(mColor);
            if (clockwise) {
                canvas.drawArc(mRect, endAngle, calculatedSweepAngle, false, mPaint);
            } else {
                canvas.drawArc(mRect, endAngle, -calculatedSweepAngle, false, mPaint);
            }
        }
        drawnSweepAngle = calculatedSweepAngle;
    }

    private void readAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleCircleProgressBar);
            if (typedArray != null) {
                try {
                    mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_strokeWidth, 0);
                    startAngle = typedArray.getFloat(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_startAngle, -90f);
                    endAngle = typedArray.getFloat(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_endAngle, 270f);
                    clockwise = typedArray.getBoolean(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_clockwise, false);
                    mShadowOffsetX = typedArray.getDimensionPixelSize(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_shadowDx, 0);
                    mShadowOffsetY = typedArray.getDimensionPixelSize(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_shadowDy, 0);
                    mColor = typedArray.getColor(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_color, 0);
                    mShadowColor = typedArray.getColor(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_shadowColor, 0);
                    mMax = typedArray.getInt(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_max, 0);
                    mProgress = typedArray.getInt(R.styleable.SimpleCircleProgressBar_simpleCircleProgressBar_progress, 0);
                } finally {
                    typedArray.recycle();
                }
            } else {
                mStrokeWidth = 0;
                startAngle = -90f;
                endAngle = 270f;
                clockwise = false;
                mShadowOffsetX = 0;
                mShadowOffsetY = 0;
                mColor = 0;
                mShadowColor = 0;
                mMax = 0;
                mProgress = 0;
            }
        }
    }

    private void calculSweepAngle() {
        calculatedSweepAngle = (endAngle - startAngle) * mProgress / mMax;
        if (calculatedSweepAngle != drawnSweepAngle)
            postInvalidate();
    }

    public void setShadowColor(@ColorInt int color) {
        if (mShadowColor != color) {
            mShadowColor = color;
            postInvalidate();
        }
    }
}