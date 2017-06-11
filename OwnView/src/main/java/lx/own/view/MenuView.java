package lx.own.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MenuView extends View {
    private static final String TAG = "MenuView";


    private static final int TYPE_NONE = -2;
    private static final int TYPE_LEVEL1 = -1;
    private static final int TYPE_LEVEL2_CENTER = 0;
    public static final int TYPE_LEVEL2_LEFT = 1;
    public static final int TYPE_LEVEL2_RIGHT = 2;
    public static final int TYPE_LEVEL3_LEFTER = 3;
    public static final int TYPE_LEVEL3_LEFT = 4;
    public static final int TYPE_LEVEL3_CENTER = 5;
    public static final int TYPE_LEVEL3_RIGHT = 6;
    public static final int TYPE_LEVEL3_RIGHTER = 7;
    private int mClickedType;

    private OnClickListener mLevel2LeftOnClickListener;
    private OnClickListener mLevel2RightOnClickListener;
    private OnClickListener mLevel3LefterOnClickListener;
    private OnClickListener mLevel3LeftOnClickListener;
    private OnClickListener mLevel3CenterOnClickListener;
    private OnClickListener mLevel3RightOnClickListener;
    private OnClickListener mLevel3RighterOnClickListener;

    private Bitmap mLevel2LeftBitmap;
    private Bitmap mLevel2RightBitmap;
    private Bitmap mLevel3LefterBitmap;
    private Bitmap mLevel3LeftBitmap;
    private Bitmap mLevel3CenterBitmap;
    private Bitmap mLevel3RightBitmap;
    private Bitmap mLevel3RighterBitmap;

    private Paint mLevel1Paint, mLevel2Paint, mLevel3Paint, mLinePaint, mClickedPaint;
    private float mStrokeWidth, mHalfStrokeWidth;
    private float mRadius;
    private RectF mLevel1Rect, mLevel2Rect, mLevel3Rect;
    private RectF mHomeButtonRect, mMenuButtonRect;
    private RectF mLevel2LeftRect, mLevel2RightRect, mLevel3LefterRect, mLevel3LeftRect, mLevel3CenterRect, mLevel3RightRect, mLevel3RighterRect;
    private int mWidth;
    private int mHeight;
    private float mStartAngle;
    private float mDuringAngle;
    private float mOnceChangeAngle = 5.0f;
    private float mLevel2DuringAngle;
    private float mLevel3DuringAngle;
    private int mDividerWidth = 5;
    private int mLevel1Color = 0xCC000000;
    private int mLevel2Color = 0x88000000;
    private int mLevel3Color = 0x44000000;
    private int mItemDividerColor = 0x44FFFFFF;
    private int mClickedColor = 0xAA330099;


    private float mLevel2ClickDrawDuringAngle;
    private float mLevel3ClickDrawDuringAngle;

    private boolean isLevel2LeftItemClicked;
    private boolean isLevel2RightItemClicked;
    private boolean isLevel3LefterItemClicked;
    private boolean isLevel3LeftItemClicked;
    private boolean isLevel3CenterItemClicked;
    private boolean isLevel3RightItemClicked;
    private boolean isLevel3RighterItemClicked;
    private boolean isLevel2Display;
    private boolean isLevel3Display;
    private boolean isNeedLevel2Change;
    private boolean isNeedLevel3Change;
    private boolean isLastTimeLevel3Display;
    private boolean isLevel1Clickable;
    private boolean isLevel2Clickable;
    private boolean isLevel3Clickable;

    private long mDownTime;
    private long mUpTime;
    private long mClickTimeout;
    private float mCurrentX,mCurrentY;

    public MenuView(Context context) {
        super(context);
        init();
    }

    public MenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStartAngle = -181;
        mDuringAngle = 181;
        isLevel2Display = false;
        isLevel3Display = false;
        isNeedLevel2Change = false;
        isNeedLevel3Change = false;
        isLastTimeLevel3Display = false;
        isLevel1Clickable = true;
        isLevel2Clickable = false;
        isLevel3Clickable = false;
        mLevel2DuringAngle = 0;
        mLevel2ClickDrawDuringAngle = 60.0f;
        mLevel3DuringAngle = 0 - mOnceChangeAngle - mOnceChangeAngle * 3;
        mLevel3ClickDrawDuringAngle = 36.0f;
        mClickTimeout = 250;
        mClickedType = TYPE_NONE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getRealSize(widthMeasureSpec);
        mHeight = getRealSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);

        mRadius = mWidth / 2 > mHeight ? mHeight / 4 : mWidth / 2 / 4;
        mStrokeWidth = mRadius;
        mHalfStrokeWidth = mStrokeWidth / 2;
        initPaint();
        initRect();
        initItemRect();
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

    private void initPaint() {
        mLevel1Paint = new Paint();
        mLevel1Paint.setColor(mLevel1Color);
        mLevel1Paint.setAntiAlias(true);

        mLevel2Paint = new Paint();
        mLevel2Paint.setColor(mLevel2Color);
        mLevel2Paint.setAntiAlias(true);
        mLevel2Paint.setStyle(Paint.Style.STROKE);
        mLevel2Paint.setStrokeWidth(mStrokeWidth);

        mLevel3Paint = new Paint();
        mLevel3Paint.setColor(mLevel3Color);
        mLevel3Paint.setAntiAlias(true);
        mLevel3Paint.setStyle(Paint.Style.STROKE);
        mLevel3Paint.setStrokeWidth(mStrokeWidth);

        mLinePaint = new Paint();
        mLinePaint.setColor(mItemDividerColor);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mStrokeWidth * 0.75f);

        mClickedPaint = new Paint();
        mClickedPaint.setColor(mClickedColor);
        mClickedPaint.setAntiAlias(true);
        mClickedPaint.setStyle(Paint.Style.STROKE);
        mClickedPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawArc(mLevel1Rect, mStartAngle, mDuringAngle, false, mLevel1Paint);
        if (isLevel2LeftItemClicked)
            canvas.drawArc(mLevel2Rect, -180.0f, mLevel2ClickDrawDuringAngle, false, mClickedPaint);
        if (isLevel2RightItemClicked)
            canvas.drawArc(mLevel2Rect, -60.0f, mLevel2ClickDrawDuringAngle, false, mClickedPaint);
        if (isNeedLevel2Change) {
            changeLevel2Display(canvas);

        } else {
            if (isLevel2Display) {
                canvas.drawArc(mLevel2Rect, mStartAngle, mDuringAngle, false, mLevel2Paint);
            }
        }
        if (mLevel2DuringAngle > 0.5f)
            canvas.drawArc(mLevel2Rect, -180.5f, 1, false, mLinePaint);
        if (mLevel2DuringAngle > 60.5f)
            canvas.drawArc(mLevel2Rect, -120.5f, 1, false, mLinePaint);
        if (mLevel2DuringAngle > 120.5f)
            canvas.drawArc(mLevel2Rect, -60.5f, 1, false, mLinePaint);
        if (mLevel2DuringAngle > 180.5f)
            canvas.drawArc(mLevel2Rect, -0.5f, 1, false, mLinePaint);


        if (isLevel3LefterItemClicked)
            canvas.drawArc(mLevel3Rect, -180.0f, mLevel3ClickDrawDuringAngle, false, mClickedPaint);
        if (isLevel3LeftItemClicked)
            canvas.drawArc(mLevel3Rect, -144.0f, mLevel3ClickDrawDuringAngle, false, mClickedPaint);
        if (isLevel3CenterItemClicked)
            canvas.drawArc(mLevel3Rect, -108.0f, mLevel3ClickDrawDuringAngle, false, mClickedPaint);
        if (isLevel3RightItemClicked)
            canvas.drawArc(mLevel3Rect, -72.0f, mLevel3ClickDrawDuringAngle, false, mClickedPaint);
        if (isLevel3RighterItemClicked)
            canvas.drawArc(mLevel3Rect, -36.0f, mLevel3ClickDrawDuringAngle, false, mClickedPaint);

        if (isNeedLevel3Change) {
            changeLevel3Display(canvas);
        } else {
            if (isLevel3Display) {
                canvas.drawArc(mLevel3Rect, mStartAngle, mDuringAngle, false, mLevel3Paint);
            }
        }

        if (mLevel3DuringAngle > 0.5f)
            canvas.drawArc(mLevel3Rect, -180.5f, 1, false, mLinePaint);
        if (mLevel3DuringAngle > 36.5f)
            canvas.drawArc(mLevel3Rect, -144.5f, 1, false, mLinePaint);
        if (mLevel3DuringAngle > 72.5f)
            canvas.drawArc(mLevel3Rect, -108.5f, 1, false, mLinePaint);
        if (mLevel3DuringAngle > 108.5f)
            canvas.drawArc(mLevel3Rect, -72.5f, 1, false, mLinePaint);
        if (mLevel3DuringAngle > 144.5f)
            canvas.drawArc(mLevel3Rect, -36.5f, 1, false, mLinePaint);
        if (mLevel3DuringAngle > 180.5f)
            canvas.drawArc(mLevel3Rect, -0.5f, 1, false, mLinePaint);
    }

    private void initRect() {
        if (mLevel1Rect == null) {
            mLevel1Rect = new RectF();
            float left = mWidth / 2 - mRadius;
            float right = mWidth / 2 + mRadius;
            float top = mHeight - mRadius;
            float bottom = mHeight + mRadius;
            mLevel1Rect.set(left, top, right, bottom);
        }
        if (mLevel2Rect == null) {
            mLevel2Rect = new RectF();
            mLevel2Rect.set(mLevel1Rect.left - mHalfStrokeWidth - mDividerWidth, mLevel1Rect.top - mHalfStrokeWidth - mDividerWidth,
                    mLevel1Rect.right + mHalfStrokeWidth + mDividerWidth, mLevel1Rect.bottom + mHalfStrokeWidth + mDividerWidth);
        }
        if (mLevel3Rect == null) {
            mLevel3Rect = new RectF();
            mLevel3Rect.set(mLevel2Rect.left - mStrokeWidth - mDividerWidth, mLevel2Rect.top - mStrokeWidth - mDividerWidth,
                    mLevel2Rect.right + mStrokeWidth + mDividerWidth, mLevel2Rect.bottom + mStrokeWidth + mDividerWidth);
        }
        if (mHomeButtonRect == null) {
            mHomeButtonRect = new RectF();
            float left = mWidth / 2.0f - (mRadius + mDividerWidth) / 1.8f;
            float right = mWidth / 2.0f + mRadius / 1.8f;
            float top = mHeight - mRadius / 1.8f;
            mHomeButtonRect.set(left, top, right, mHeight);
        }
        if (mMenuButtonRect == null) {
            mMenuButtonRect = new RectF();
            float left = mWidth / 2.0f - (mRadius + mDividerWidth) * 0.577f;
            float right = mWidth / 2.0f + (mRadius + mDividerWidth) * 0.577f;
            float top = mHeight - mRadius - mStrokeWidth - mDividerWidth;
            float bottom = mHeight - mRadius - mDividerWidth;
            mMenuButtonRect.set(left, top, right, bottom);
        }
    }

    private void initItemRect() {
        if (mLevel2LeftRect == null) {
            mLevel2LeftRect = new RectF();
            float left = mWidth / 2.0f - mRadius - mStrokeWidth - mDividerWidth;
            float right = mWidth / 2.0f - mRadius - mDividerWidth;
            float top = mHeight - mRadius;
            float bottom = mHeight;
            mLevel2LeftRect.set(left, top, right, bottom);
        }
        if (mLevel2RightRect == null) {
            mLevel2RightRect = new RectF();
            float left = mWidth / 2.0f + mRadius  + mDividerWidth;
            float right = mWidth / 2.0f + mRadius + mStrokeWidth  + mDividerWidth;
            float top = mHeight - mRadius;
            float bottom = mHeight;
            mLevel2RightRect.set(left, top, right, bottom);
        }
        if (mLevel3LefterRect == null) {
            mLevel3LefterRect = new RectF();
            float left = mLevel2LeftRect.left - mStrokeWidth - mDividerWidth;
            float right = mLevel2LeftRect.right - mStrokeWidth - mDividerWidth;
            float top = mHeight - (mRadius + mStrokeWidth + 10) * 0.726f;
            float bottom = mHeight;
            mLevel3LefterRect.set(left, top, right, bottom);
        }
        if (mLevel3RighterRect == null) {
            mLevel3RighterRect = new RectF();
            float left = mLevel2RightRect.left + mStrokeWidth + mDividerWidth;
            float right = mLevel2RightRect.right + mStrokeWidth + mDividerWidth;
            float top = mHeight - (mRadius + mStrokeWidth + mDividerWidth * 2) * 0.726f;
            float bottom = mHeight;
            mLevel3RighterRect.set(left, top, right, bottom);
        }
        if (mLevel3CenterRect == null) {
            mLevel3CenterRect = new RectF();
            float left = mWidth / 2 - (mRadius + mStrokeWidth + mDividerWidth * 2) * 0.325f;
            float right = mWidth / 2 + (mRadius + mStrokeWidth + mDividerWidth * 2) * 0.325f;
            float top = mMenuButtonRect.top - mRadius - mDividerWidth;
            float bottom = mMenuButtonRect.bottom - mRadius - mDividerWidth;
            mLevel3CenterRect.set(left, top, right, bottom);
        }

        if (mLevel3RightRect == null) {
            mLevel3RightRect = new RectF();
            float width = (mWidth / 2 + (mRadius + mStrokeWidth * 2) * 0.809f);
            float left = width - mStrokeWidth / 0.809f + mDividerWidth;
            float right = width + mDividerWidth;
            float top = mHeight - ((width - mStrokeWidth / 0.809f - mWidth / 2) / 0.325f) + 5.0f;
            float bottom = mHeight - ((mRadius + mStrokeWidth * 1.5f) * 0.726f) + 5.0f;
            mLevel3RightRect.set(left, top, right, bottom);
        }

        if (mLevel3LeftRect == null) {
            mLevel3LeftRect = new RectF();
            float left = (mWidth / 2 - (mRadius + mStrokeWidth * 2) * 0.809f) - mDividerWidth;
            float right = left + mStrokeWidth / 0.809f - mDividerWidth;
            float top = mLevel3RightRect.top;
            float bottom = mLevel3RightRect.bottom + mDividerWidth;
            mLevel3LeftRect.set(left, top, right, bottom);
        }

    }

    private void changeLevel2Display(Canvas canvas) {
        if (isLevel2Display) {
            canvas.drawArc(mLevel2Rect, mStartAngle, mLevel2DuringAngle, false, mLevel2Paint);
            mLevel2DuringAngle -= mOnceChangeAngle;
            if (mLevel2DuringAngle >= 0 - mOnceChangeAngle) {
                invalidate();
            } else {
                isLevel2Display = !isLevel2Display;
                isNeedLevel2Change = false;
                isLevel2Clickable = false;
                if (!isNeedLevel3Change)
                    isLevel1Clickable = true;
            }
        } else {
            canvas.drawArc(mLevel2Rect, mStartAngle, mLevel2DuringAngle, false, mLevel2Paint);
            mLevel2DuringAngle += mOnceChangeAngle;
            if (mLevel2DuringAngle <= mDuringAngle + mOnceChangeAngle) {
                invalidate();
            } else {
                isLevel2Display = !isLevel2Display;
                isNeedLevel2Change = false;
                isLevel2Clickable = true;
                if (!isNeedLevel3Change)
                    isLevel1Clickable = true;
            }
        }
    }

    private void changeLevel3Display(Canvas canvas) {
        if (isLevel3Display) {
            canvas.drawArc(mLevel3Rect, mStartAngle, mLevel3DuringAngle, false, mLevel3Paint);
            mLevel3DuringAngle -= mOnceChangeAngle;
            if (mLevel3DuringAngle >= 0 - mOnceChangeAngle - mOnceChangeAngle * 3) {
                invalidate();
            } else {
                isLevel3Display = !isLevel3Display;
                isNeedLevel3Change = false;
                isLevel3Clickable = false;
                if (isLevel2Display)
                    isLevel2Clickable = true;
                isLevel1Clickable = true;
            }
        } else {
            canvas.drawArc(mLevel3Rect, mStartAngle, mLevel3DuringAngle, false, mLevel3Paint);
            mLevel3DuringAngle += mOnceChangeAngle;
            if (mLevel3DuringAngle <= mDuringAngle + mOnceChangeAngle + mOnceChangeAngle * 3) {
                invalidate();
            } else {
                isLevel3Display = !isLevel3Display;
                isNeedLevel3Change = false;
                isLevel3Clickable = true;
                if (isLevel2Display)
                    isLevel2Clickable = true;
                isLevel1Clickable = true;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCurrentX = event.getX();
        mCurrentY = event.getY();

        isLevel2RightItemClicked = false;
        isLevel2LeftItemClicked = false;
        isLevel3LefterItemClicked = false;
        isLevel3LeftItemClicked = false;
        isLevel3CenterItemClicked = false;
        isLevel3RightItemClicked = false;
        isLevel3RighterItemClicked = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownTime = SystemClock.elapsedRealtime();
                mClickedType = TYPE_NONE;
                if (mLevel2LeftRect.contains(mCurrentX, mCurrentY) && isLevel2Clickable) {
                    if (!isLevel2Clickable)
                        return true;
                    mClickedType = TYPE_LEVEL2_LEFT;
                    isLevel2LeftItemClicked = true;
                    invalidate();
                }
                if (mLevel2RightRect.contains(mCurrentX, mCurrentY) && isLevel2Clickable) {
                    if (!isLevel2Clickable)
                        return true;
                    mClickedType = TYPE_LEVEL2_RIGHT;
                    isLevel2RightItemClicked = true;
                    invalidate();
                }
                if (mHomeButtonRect.contains(mCurrentX, mCurrentY) && isLevel1Clickable) {
                    mClickedType = TYPE_LEVEL1;
                }
                if (mMenuButtonRect.contains(mCurrentX, mCurrentY) && isLevel2Clickable) {
                    mClickedType = TYPE_LEVEL2_CENTER;
                }
                if (mLevel3LefterRect.contains(mCurrentX, mCurrentY)) {
                    if (!isLevel3Clickable)
                        return true;
                    mClickedType = TYPE_LEVEL3_LEFTER;
                    isLevel3LefterItemClicked = true;
                    invalidate();
                }
                if (mLevel3RighterRect.contains(mCurrentX, mCurrentY)) {
                    if (!isLevel3Clickable)
                        return true;
                    mClickedType = TYPE_LEVEL3_RIGHTER;
                    isLevel3RighterItemClicked = true;
                    invalidate();
                }
                if (mLevel3CenterRect.contains(mCurrentX, mCurrentY)) {
                    if (!isLevel3Clickable)
                        return true;
                    mClickedType = TYPE_LEVEL3_CENTER;
                    isLevel3CenterItemClicked = true;
                    invalidate();
                }
                if (mLevel3RightRect.contains(mCurrentX, mCurrentY)) {
                    if (!isLevel3Clickable)
                        return true;
                    mClickedType = TYPE_LEVEL3_RIGHT;
                    isLevel3RightItemClicked = true;
                    invalidate();
                }
                if (mLevel3LeftRect.contains(mCurrentX, mCurrentY)) {
                    if (!isLevel3Clickable)
                        return true;
                    mClickedType = TYPE_LEVEL3_LEFT;
                    isLevel3LeftItemClicked = true;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                mUpTime = SystemClock.elapsedRealtime();
                if (mUpTime - mDownTime < mClickTimeout) {
                    switch (mClickedType) {
                        case TYPE_LEVEL1:
                            if (mHomeButtonRect.contains(mCurrentX, mCurrentY)) {
                                if (!isLevel1Clickable)
                                    return true;
                                isLevel1Clickable = false;
                                isLevel2Clickable = false;
                                isLevel3Clickable = false;
                                isNeedLevel2Change = true;
                                if (isLevel2Display) {
                                    isNeedLevel3Change = isLevel3Display;
                                } else {
                                    isNeedLevel3Change = isLastTimeLevel3Display;
                                }
                                isLevel2Clickable = false;
                                isLevel3Clickable = false;
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL2_CENTER:
                            if (mMenuButtonRect.contains(mCurrentX, mCurrentY)) {
                                if (!isLevel2Clickable)
                                    return true;
                                isLevel1Clickable = false;
                                isLevel2Clickable = false;
                                isLevel3Clickable = false;
                                isNeedLevel3Change = true;
                                isLastTimeLevel3Display = !isLastTimeLevel3Display;
                                isLevel1Clickable = false;
                                isLevel3Clickable = false;
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL2_LEFT:
                            if (mLevel2LeftRect.contains(mCurrentX, mCurrentY)) {
                                if (mLevel2LeftOnClickListener != null)
                                    mLevel2LeftOnClickListener.onClick(this);
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL2_RIGHT:
                            if (mLevel2RightRect.contains(mCurrentX, mCurrentY)) {
                                if (mLevel2RightOnClickListener != null)
                                    mLevel2RightOnClickListener.onClick(this);
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL3_LEFTER:
                            if (mLevel3LefterRect.contains(mCurrentX, mCurrentY)) {
                                if (mLevel3LefterOnClickListener != null)
                                    mLevel3LefterOnClickListener.onClick(this);
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL3_LEFT:
                            if (mLevel3LeftRect.contains(mCurrentX, mCurrentY)) {
                                if (mLevel3LeftOnClickListener != null)
                                    mLevel3LeftOnClickListener.onClick(this);
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL3_CENTER:
                            if (mLevel3CenterRect.contains(mCurrentX, mCurrentY)) {
                                if (mLevel3CenterOnClickListener != null)
                                    mLevel3CenterOnClickListener.onClick(this);
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL3_RIGHT:
                            if (mLevel3RightRect.contains(mCurrentX, mCurrentY)) {
                                if (mLevel3RightOnClickListener != null)
                                    mLevel3RightOnClickListener.onClick(this);
                                invalidate();
                            }
                            break;
                        case TYPE_LEVEL3_RIGHTER:
                            if (mLevel3RighterRect.contains(mCurrentX, mCurrentY)) {
                                if (mLevel3RighterOnClickListener != null)
                                    mLevel3RighterOnClickListener.onClick(this);
                                invalidate();
                            }
                            break;
                    }
                    mClickedType = TYPE_NONE;
                } else {
                    mClickedType = TYPE_NONE;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
                isNeedLevel2Change = false;
                isNeedLevel3Change = false;
                isLevel2LeftItemClicked = false;
                isLevel2RightItemClicked = false;
                invalidate();
                break;
            default:
                mClickedType = TYPE_NONE;
                break;
        }
        return true;
    }

    public void setOnclickListener(OnClickListener listener, int type) {
        switch (type) {
            case TYPE_LEVEL2_LEFT:
                mLevel2LeftOnClickListener = listener;
                break;
            case TYPE_LEVEL2_RIGHT:
                mLevel2RightOnClickListener = listener;
                break;
            case TYPE_LEVEL3_LEFTER:
                mLevel3LefterOnClickListener = listener;
                break;
            case TYPE_LEVEL3_LEFT:
                mLevel3LeftOnClickListener = listener;
                break;
            case TYPE_LEVEL3_CENTER:
                mLevel3CenterOnClickListener = listener;
                break;
            case TYPE_LEVEL3_RIGHT:
                mLevel3RightOnClickListener = listener;
                break;
            case TYPE_LEVEL3_RIGHTER:
                mLevel3RighterOnClickListener = listener;
                break;
        }
    }


    public void setLevelDividerWidth(int width) {
        this.mDividerWidth = width;
    }

    public void setLevel1Color(int color) {
        if (mLevel1Paint == null) {
            this.mLevel1Color = color;
        } else {
            this.mLevel1Paint.setColor(color);
        }
    }

    public void setLevel2Color(int color) {
        if (mLevel2Paint == null) {
            this.mLevel2Color = color;
        } else {
            this.mLevel2Paint.setColor(color);
        }
    }

    public void setLevel3Color(int color) {
        if (mLevel3Paint == null) {
            this.mLevel3Color = color;
        } else {
            this.mLevel3Paint.setColor(color);
        }
    }

    public void setItemDividerColor(int color) {
        if (mLinePaint == null) {
            this.mItemDividerColor = color;
        } else {
            this.mLinePaint.setColor(color);
        }
    }

    public void setClickedColor(int color) {
        if (mClickedPaint == null) {
            this.mClickedColor = color;
        } else {
            this.mClickedPaint.setColor(color);
        }
    }
}
