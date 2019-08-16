package lx.own.view.online;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import lx.own.R;

/**
 * <b>静音灯控件</b><br/>
 *
 *
 * @author Lei.X
 * Created on 2019/5/17.
 */
public class MuteLampView extends View {

    private final Paint mLampPaint, mDuringLampPaint;
    @ColorInt
    private int mLampColor, mDuringLampColor;
    private int mLampAlpha, mDuringLampAlpha;

    private float mMinRadius, mMaxRadius;
    private float mLampPadding;

    private int mLampCount;
    private float mCenterX, mCenterY, mFirstLampDrawX, mDrawInterval;

    private int mProgress, mMax;
    private int mDuringPointPosition;//正在进行变化的点的索引
    private float mDuringPointRadius;//正在进行的点的半径
    private float mPointDelayPercent;//每个间隔的延时百分比(在这阶段内不动作)

    private boolean fadeout;
    private int fadeoutTime;
    private int mCurrentAlpha;
    private int mProgressIgnoreOffset;//进度偏移 实际进度会为 progress - mProgressIgnoreOffset

    {
        mLampPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDuringLampPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public MuteLampView(Context context) {
        super(context);
        init(context, null, -1);
    }

    public MuteLampView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public MuteLampView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mCurrentAlpha = 255;
        this.mLampColor = 0xFFFFFFFF;
        this.mDuringLampColor = 0xFFFFFFFF;
        this.mMinRadius = 10f;
        this.mMaxRadius = 20f;
        this.mLampPadding = 10f;
        this.mLampCount = 5;
        this.mMax = 0;
        this.mProgress = 0;
        this.mPointDelayPercent = 0.5f;
        this.fadeout = false;
        this.fadeoutTime = 0;
        if (attrs != null) {
            final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MuteLampView);
            if (ta != null) {
                this.mLampColor = ta.getColor(R.styleable.MuteLampView_muteLampView_color, mLampColor);
                this.mDuringLampColor = ta.getColor(R.styleable.MuteLampView_muteLampView_duringColor, mDuringLampColor);
                if (ta.hasValue(R.styleable.MuteLampView_muteLampView_minRadius))
                    this.mMinRadius = ta.getDimensionPixelSize(R.styleable.MuteLampView_muteLampView_minRadius, 0);
                if (ta.hasValue(R.styleable.MuteLampView_muteLampView_maxRadius))
                    this.mMaxRadius = ta.getDimensionPixelSize(R.styleable.MuteLampView_muteLampView_maxRadius, 0);
                if (ta.hasValue(R.styleable.MuteLampView_muteLampView_pointPadding))
                    this.mLampPadding = ta.getDimensionPixelSize(R.styleable.MuteLampView_muteLampView_pointPadding, 0);
                this.mLampCount = ta.getInteger(R.styleable.MuteLampView_muteLampView_pointCount, mLampCount);
                this.mMax = ta.getInteger(R.styleable.MuteLampView_muteLampView_max, mMax);
                this.mPointDelayPercent = ta.getFloat(R.styleable.MuteLampView_muteLampView_pointDelayPercent, mPointDelayPercent);
                if (mMax < 0)
                    mMax = 0;
                this.mProgress = ta.getInteger(R.styleable.MuteLampView_muteLampView_progress, mProgress);
                if (mProgress < 0)
                    mProgress = 0;
                this.fadeout = ta.getBoolean(R.styleable.MuteLampView_muteLampView_fadeout, fadeout);
                this.fadeoutTime = ta.getInteger(R.styleable.MuteLampView_muteLampView_fadeoutTime, fadeoutTime);
                ta.recycle();
            }
        }
        mLampAlpha = mLampColor >>> 24;
        mLampPaint.setAlpha(calculateCurrentAlpha(mLampAlpha));
        mLampPaint.setColor(mLampColor);
        mDuringLampAlpha = mDuringLampColor >>> 24;
        mDuringLampPaint.setAlpha(calculateCurrentAlpha(mDuringLampAlpha));
        mDuringLampPaint.setColor(mDuringLampColor);
    }

//    public MuteLampView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        calculateDrawLocation(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mLampCount; i++) {
            if (i < mDuringPointPosition) {
                canvas.drawCircle(mFirstLampDrawX + i * mDrawInterval, mCenterY, mMinRadius, mDuringLampPaint);
            } else if (i == mDuringPointPosition) {
                canvas.drawCircle(mFirstLampDrawX + i * mDrawInterval, mCenterY, mDuringPointRadius, mDuringLampPaint);
            } else {
                canvas.drawCircle(mFirstLampDrawX + i * mDrawInterval, mCenterY, mMinRadius, mLampPaint);
            }
        }
    }

    public void setMax(int max) {
        if (max >= 0 && mMax != max) {
            mMax = max;
            if (calculateDuringPoint())
                postInvalidate();
        }
    }

    public void setProgress(int progress) {
        if (progress >= 0 && mProgress != progress) {
            mProgress = progress;
            if (calculateDuringPoint())
                postInvalidate();
        }
    }

    public int getProgressIgnoreOffset() {
        return mProgressIgnoreOffset;
    }

    public void setProgressIgnoreOffset(int mProgressIgnoreOffset) {
        this.mProgressIgnoreOffset = mProgressIgnoreOffset;
    }

    public boolean isMaxed() {
        return mMax <= mProgress - mProgressIgnoreOffset;
    }

    @AnyThread
    public boolean isMaxedWithFadeout() {
        return mMax + fadeoutTime <= mProgress - mProgressIgnoreOffset;
    }

    private int calculateCurrentAlpha(int colorAlpha) {
        int alpha = (int) (mCurrentAlpha / 255f * colorAlpha + 0.5f);
        if (alpha > 255)
            alpha = 255;
        else if (alpha < 0)
            alpha = 0;
        return alpha;
    }

    private void calculateDrawLocation(int width, int height) {
        mCenterX = width / 2.0f;
        mCenterY = height / 2.0f;
        mDrawInterval = mMinRadius * 2f + mLampPadding;
        final int halfCount = mLampCount >> 1;
        if (mLampCount % 2 == 1) {//奇数个
            mFirstLampDrawX = mCenterX - halfCount * mDrawInterval;
        } else {//偶数个
            if (halfCount <= 0) {//count <= 1的奇葩情况
                mFirstLampDrawX = mCenterX;
            } else {
                mFirstLampDrawX = mCenterX - (halfCount - 0.5f) * mDrawInterval;
            }
        }
        calculateDuringPoint();
    }

    private boolean calculateDuringPoint() {
        boolean needInvalidate = false;
        mCurrentAlpha = 255;
        int availableProgress = mProgress - mProgressIgnoreOffset;
        if (availableProgress < 0)
            availableProgress = 0;
        if (mMax <= availableProgress) {
            if (mLampCount != mDuringPointPosition) {
                this.mDuringPointPosition = mLampCount;
                needInvalidate = true;
            }
            if (fadeout && fadeoutTime > 0) {
                mCurrentAlpha = (int) ((1.0f - (availableProgress - mMax) * 1.0f / fadeoutTime) * 255 + 0.5f);
                if (mCurrentAlpha < 0)
                    mCurrentAlpha = 0;
                else if (mCurrentAlpha > 255)
                    mCurrentAlpha = 255;
                needInvalidate = true;
            }
        } else {
            //每个点占据的进度长度
            final int ePointProgress = mMax / mLampCount;
            //当前进度正在影响的点
            mDuringPointPosition = availableProgress / ePointProgress;
            //当前这个点进行的百分比
            float percent = (availableProgress % ePointProgress) * 1.0f / ePointProgress;
            if (percent < mPointDelayPercent) {
                mDuringPointPosition -= 1;
                mDuringPointRadius = mMinRadius;
            } else {
                //percent是以1.0为基准计算，在设置了delayPercent的情况下，基准变为了(1.0f - delayPercent),所以这里等于要扩大percent
                float totalPercent = 1.0f - percent / (1.0f - mPointDelayPercent);
                if (totalPercent > 1f)
                    totalPercent = 1f;
                else if (totalPercent < 0f)
                    totalPercent = 0f;
                mDuringPointRadius = (mMaxRadius - mMinRadius) * totalPercent + mMinRadius;
            }
            needInvalidate = true;
        }
        mLampPaint.setAlpha(calculateCurrentAlpha(mLampAlpha));
        mDuringLampPaint.setAlpha(calculateCurrentAlpha(mDuringLampAlpha));
        return needInvalidate;
    }

    public int getMax() {
        return mMax;
    }
}
