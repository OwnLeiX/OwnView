package lx.own.view.offline;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import lx.own.R;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 16/04/2017
 */

public class RipplePlayerView extends View {

    private static final int STATE_PLAYING = 1;
    private static final int STATE_STOP = 1 << 1;
    private static final int STATE_PAUSE = 1 << 2;

    private int[] mWH;//控件宽高
    private float[] mHalfWH;//控件一般宽高
    private float[] mCurrentRippleRadius;//三道波纹的半径
    private float[] mBitmapLeftAndTop;//中间封面的起始绘制点

    private Paint mRipplePaint, mCoverPaint, mStrokePaint;
    private int mRippleColor, mStrokeColor;
    private float mRippleWidth, mStrokeWidth;
    private float mCoverRadius;//圆形封面半径
    private float mRippleRange;//波纹最远扩散范围
    private float mRipplePercent;
    private float mRipplePadding;
    private int mRippleTime;//波纹扩散时间间隔

    private boolean isNeedCoverStroke;//是否绘制封面边框

    private int mCurrentState = STATE_STOP;

    private Bitmap mCover;//封面图片

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private InvalidateTask mInvalidateTask;

    public RipplePlayerView(Context context) {
        this(context, null);
    }

    public RipplePlayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RipplePlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init(context);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        mRippleWidth = 10;
        mRippleColor = 0xFFFFFFFF;
        mStrokeWidth = 1;
        mStrokeColor = 0xFF000000;
        isNeedCoverStroke = true;
        mRipplePadding = mRippleWidth * 1.5f;
        mRippleTime = 50;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RipplePlayerView);
            if (typedArray != null) {
                mRippleWidth = typedArray.getDimension(R.styleable.RipplePlayerView_rippleWidth, mRippleWidth);
                mRippleColor = typedArray.getColor(R.styleable.RipplePlayerView_rippleColor, mRippleColor);
                mStrokeWidth = typedArray.getDimension(R.styleable.RipplePlayerView_strokeWidth, mStrokeWidth);
                mStrokeColor = typedArray.getColor(R.styleable.RipplePlayerView_strokeColor, mStrokeColor);
                isNeedCoverStroke = typedArray.getBoolean(R.styleable.RipplePlayerView_coverStroke, isNeedCoverStroke);
                mRipplePadding = typedArray.getDimension(R.styleable.RipplePlayerView_ripplePadding, mRippleWidth * 1.5f);
                mRippleRange = typedArray.getDimension(R.styleable.RipplePlayerView_rippleRange, mRipplePadding * 3);
                mRippleTime = typedArray.getInt(R.styleable.RipplePlayerView_rippleTime, mRippleTime);
                typedArray.recycle();
            }
        }
    }

    private void init(Context context) {
        mWH = new int[2];
        mHalfWH = new float[2];
        mCurrentRippleRadius = new float[3];
        mBitmapLeftAndTop = new float[2];

        mInvalidateTask = new InvalidateTask(this);

        mRipplePaint = new Paint();
        mRipplePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mRipplePaint.setColor(mRippleColor);
        mRipplePaint.setStyle(Paint.Style.STROKE);
        mRipplePaint.setStrokeWidth(mRippleWidth);

        mStrokePaint = new Paint();
        mStrokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mStrokeWidth);

        mCoverPaint = new Paint();
        mCoverPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWH[0] = w;
        mWH[1] = h;
        mHalfWH[0] = w / 2.0f;
        mHalfWH[1] = h / 2.0f;
        calculatorCoverRange();
        mCover = getDefaultCover(R.drawable.img_cover);
    }

    private void calculatorCoverRange() {
        mCoverRadius = Math.min(mHalfWH[0], mHalfWH[1]) - mRippleRange;
        if (mCoverRadius <= 0)
            mCoverRadius = 1;
        mCurrentRippleRadius[0] = (int) mCoverRadius;
        mCurrentRippleRadius[1] = (int) mCoverRadius;
        mCurrentRippleRadius[2] = (int) mCoverRadius;
        mBitmapLeftAndTop[0] = (mWH[0] - mCoverRadius * 2.0f) / 2.0f;
        mBitmapLeftAndTop[1] = (mWH[1] - mCoverRadius * 2.0f) / 2.0f;
    }

    private Bitmap getDefaultCover(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, mWH[0], mWH[1]);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId, options);
        return getSizedAndRoundedBitmap(bitmap, (int) mCoverRadius * 2);
    }

    private Bitmap getSizedAndRoundedBitmap(Bitmap bitmap, int size) {
        Bitmap result = null;
        if (bitmap != null && !bitmap.isRecycled()) {
            float scale = Math.max(size * 1.0f / bitmap.getWidth(), size * 1.0f / bitmap.getHeight());
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale, 0, 0);
            float radius = size / 2.0f;
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            paint.setColor(0xFF000000);
            canvas.drawCircle(radius, radius, radius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, matrix, paint);
            bitmap.recycle();
        }
        return result;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final float halfHeight = height / 2.0f;
            final float halfWidth = width / 2.0f;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制封面下的底纹
        mRipplePaint.setAlpha(0xFF);
        mRipplePaint.setStrokeWidth(mRippleWidth);
        canvas.drawCircle(mHalfWH[0], mHalfWH[1], mCoverRadius, mRipplePaint);
        //绘制第一道波纹
        mRipplePercent = 1.0f - (mCurrentRippleRadius[0] * 1.0f - mCoverRadius) / mRippleRange;
        mRipplePaint.setAlpha((int) (mRipplePercent * 0xFF));
        mRipplePaint.setStrokeWidth(mRipplePercent * mRippleWidth);
        canvas.drawCircle(mHalfWH[0], mHalfWH[1], mCurrentRippleRadius[0], mRipplePaint);
        //如果第一道波纹扩散至2倍波纹厚度的位置及更远，开始绘制第二道波纹
        if (mCurrentRippleRadius[0] - mCurrentRippleRadius[1] >= mRipplePadding) {
            mRipplePercent = 1.0f - (mCurrentRippleRadius[1] * 1.0f - mCoverRadius) / mRippleRange;
            mRipplePaint.setAlpha((int) (mRipplePercent * 0xFF));
            mRipplePaint.setStrokeWidth(mRipplePercent * mRippleWidth);
            canvas.drawCircle(mHalfWH[0], mHalfWH[1], mCurrentRippleRadius[1], mRipplePaint);
            //如果第二道波纹扩散至2倍波纹厚度的位置及更远，开始绘制第三道波纹
            if (mCurrentRippleRadius[1] - mCurrentRippleRadius[2] >= mRipplePadding) {
                mRipplePercent = 1.0f - (mCurrentRippleRadius[2] * 1.0f - mCoverRadius) / mRippleRange;
                mRipplePaint.setAlpha((int) (mRipplePercent * 0xFF));
                mRipplePaint.setStrokeWidth(mRipplePercent * mRippleWidth);
                canvas.drawCircle(mHalfWH[0], mHalfWH[1], mCurrentRippleRadius[2], mRipplePaint);
                mCurrentRippleRadius[2]++;
            }
            mCurrentRippleRadius[1]++;
        }
        mCurrentRippleRadius[0]++;
        //如果第一道波纹扩散至最远距离，舍弃第一道波纹，将第二道波纹的位置设置给第一道，将第三道波纹的位置设置给第二道，将初始位置设置给第三道
        if (mCurrentRippleRadius[0] >= mCoverRadius + mRippleRange) {
            mCurrentRippleRadius[0] = mCurrentRippleRadius[1];
            mCurrentRippleRadius[1] = mCurrentRippleRadius[2];
            mCurrentRippleRadius[2] = (int) mCoverRadius;
        }
        //绘制圆形封面
        if (mCover != null && !mCover.isRecycled()) {
            canvas.drawBitmap(mCover, mBitmapLeftAndTop[0], mBitmapLeftAndTop[1], mCoverPaint);
            //绘制封面边框
            if (isNeedCoverStroke) {
                canvas.drawCircle(mHalfWH[0], mHalfWH[1], mCoverRadius, mStrokePaint);
            }
        }
        mHandler.postDelayed(mInvalidateTask, mRippleTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
        if (mCover != null && !mCover.isRecycled())
            mCover.recycle();
    }


    private class InvalidateTask implements Runnable {
        View target;

        private InvalidateTask(View target) {
            this.target = target;
        }

        @Override
        public void run() {
            if (target != null)
                target.invalidate();
        }
    }
}
