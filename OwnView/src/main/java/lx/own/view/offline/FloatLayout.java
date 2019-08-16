package lx.own.view.offline;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.customview.widget.ViewDragHelper;

import java.util.ArrayList;
import java.util.Random;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 2016/12/4
 */

public class FloatLayout extends ViewGroup {

    private static final String TAG = "FloatLayout";

    private ArrayList<View> mDescendants;
    private Random mRandom;
    private int mWidth,mHeight;
    private int mDescendantWidth,mDescendantsHeight;
    private float mPivotX,mPivotY;
    private View settingView;
    private ViewDragHelper mHelper;
    private ViewDragHelper.Callback mCallback;


    public FloatLayout(Context context) {
        super(context);
        init();
    }

    public FloatLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FloatLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mRandom = new Random();
        createCallback();
        mHelper = ViewDragHelper.create(this,1.0f,mCallback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mDescendantWidth = (int) (mWidth / 2.0f);
        mDescendantsHeight = (int) (mHeight / 2.0f);
        for (int i = 0; i < getChildCount(); i++) {
            settingView = getChildAt(i);
            settingView.measure(MeasureSpec.makeMeasureSpec(settingView.getLayoutParams().width,MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(settingView.getLayoutParams().height,MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mWidth = r - l;
        mHeight = b - t;
        for (int i = 0; i < getChildCount(); i++) {
            settingView = getChildAt(i);
            int left = mRandom.nextInt(mWidth - settingView.getMeasuredWidth());
            int top = mRandom.nextInt(mHeight - settingView.getMeasuredHeight());
//            float scalePercentage = getScalePercentage(settingView);
//            settingView.layout(getScaledLeft(left, scalePercentage),getScaledTop(top,scalePercentage),
//                    getScaledRight(left,scalePercentage), getScaledBottom(top,scalePercentage));
            settingView.layout(left,top,left + settingView.getMeasuredWidth(),top + settingView.getMeasuredHeight());
            ObjectAnimator oa = ObjectAnimator.ofFloat(settingView,"translationY",0.0f,-5.0f,0.0f,5.0f).setDuration((mRandom.nextInt(4) + 5) * 500);
            oa.setRepeatCount(ObjectAnimator.INFINITE);
            oa.setRepeatMode(ObjectAnimator.REVERSE);
            oa.start();
        }
    }

    private float getScalePercentage(View view) {
        int povitX = view.getLeft() + view.getRight() / 2;
        int povitY = view.getTop() + view.getBottom() / 2;
        float horizontalOffsetPercentage;
        float verticalOffsetPercentage;
        float offsetPercentage;
            horizontalOffsetPercentage = 1.0f - Math.abs((float)povitX - mPivotX) / mPivotX;
            verticalOffsetPercentage = 1.0f - Math.abs((float) povitY - mPivotY) / mPivotY;
        offsetPercentage =  horizontalOffsetPercentage > verticalOffsetPercentage ? verticalOffsetPercentage : horizontalOffsetPercentage;
        Log.wtf(TAG,"FloatLayout.getScalePercentage(): " + offsetPercentage);
        return offsetPercentage < 0.1f ? 0.1f : offsetPercentage;
    }

    private int getScaledLeft(int left,float scalePercentage) {
        int newLeft = (int) (left + ((float)mDescendantWidth * (1.0f - scalePercentage)) / 2.0f);
        return newLeft;
    }

    private int getScaledRight(int left,float scalePercentage) {
        int newRight = (int) (left + (float)mDescendantWidth * scalePercentage + ((float)mDescendantWidth * (1.0f - scalePercentage)) / 2.0f);
        return newRight;
    }

    private int getScaledTop(int top,float scalePercentage) {
        int newTop = (int) (top + ((float)mDescendantsHeight * scalePercentage) / 2.0f);
        return newTop;
    }

    private int getScaledBottom(int top,float scalePercentage) {
        int newBottom = (int) (top + (float)mDescendantsHeight * scalePercentage + ((float)mDescendantsHeight * scalePercentage) / 2.0f);
        return newBottom;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mHelper.processTouchEvent(event);
        return true;
    }

    private void createCallback(){
        mCallback = new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return reviseHorizontalOffset(child,left);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return reviseVerticalOffset(child,top);
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return (int) (mWidth - 0.2f * mDescendantWidth);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return (int) (mHeight - 0.2f * mDescendantsHeight);
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//                float scalePercentage = getScalePercentage(changedView);
//                changedView.layout(left,top,
//                        (int) (left + mDescendantWidth * scalePercentage), (int) (top + mDescendantsHeight * scalePercentage));
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
            }

            private int reviseHorizontalOffset(View view,int left) {
                int revisedLeft = left;
                if(left < 40 - view.getWidth()) {
                    revisedLeft = - view.getWidth() + 40;
                }else if(left > mWidth - 40){
                    revisedLeft = mWidth - 40;
                }
                return revisedLeft;
            }

            private int reviseVerticalOffset(View view,int top) {
                int revisedTop = top;
                if(top < 40 - view.getHeight()) {
                    revisedTop = 40 - view.getHeight();
                }else if(top > mHeight - 40){
                    revisedTop = mHeight - 40;
                }
                return revisedTop;
            }
        };
    }
}
