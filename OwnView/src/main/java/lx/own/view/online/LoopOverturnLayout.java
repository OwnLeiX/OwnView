package lx.own.view.online;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import lx.own.R;

/**
 * <b>上屏后拿着自己的子View挨个翻转的FrameLayout</b><br/>
 * 2.1.2 抢唱房间推荐理由翻转动画用，懒得在外面写代码动画来动画去的，所以写了个这个
 *
 * @author Lei.X
 *         Created on 7/5/2019.
 */
public class LoopOverturnLayout extends FrameLayout {

    public final int MODE_HORIZONTAL = 1;
    public final int MODE_VERTICAL = 2;

    private Animator mOverturnAnimator;
    private int mCurrentDisplayingChildIndex;
    private int mOverturnMode;
    private String mOverturnFieldName;
    private long mEachChildDisplayTime, mTurnDuringTime;
    private final AnimatorListenerAdapter mLoopListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationStart(Animator animation) {
            final int count = getChildCount();
            if (count > mCurrentDisplayingChildIndex) {
                final View willDisplayChild = getChildAt(mCurrentDisplayingChildIndex);
                if (willDisplayChild != null)
                    willDisplayChild.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            if (mOverturnAnimator == animation) {
                mOverturnAnimator = null;
                final int count = getChildCount();
                if (count > mCurrentDisplayingChildIndex) {
                    final View disappearedChild = getChildAt(mCurrentDisplayingChildIndex);
                    if (disappearedChild != null)
                        disappearedChild.setVisibility(View.GONE);
                }
                if (count > 1) {
                    if (count > mCurrentDisplayingChildIndex + 1) {
                        mCurrentDisplayingChildIndex += 1;
                        final View willDisplayChild = getChildAt(mCurrentDisplayingChildIndex);
                        if (willDisplayChild != null) {
                            mOverturnAnimator = generateOverturnAnim(getChildAt(mCurrentDisplayingChildIndex), mLoopListener);
                            mOverturnAnimator.start();
                        } else {
                            startOverturnAnim();
                        }
                    } else {
                        startOverturnAnim();
                    }
                } else if (count == 1) {
                    displayFirstChild();
                }
            }
        }
    };

    public LoopOverturnLayout(@NonNull Context context) {
        this(context, null, NO_ID);
    }

    public LoopOverturnLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, NO_ID);
    }

    public LoopOverturnLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOverturnMode = MODE_HORIZONTAL;
        mEachChildDisplayTime = 2000L;
        mTurnDuringTime = 500L;
        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoopOverturnLayout);
            if (typedArray != null) {
                mOverturnMode = typedArray.getInteger(R.styleable.LoopOverturnLayout_loopOverturnLayout_orientation, mOverturnMode);
                mEachChildDisplayTime = typedArray.getInteger(R.styleable.LoopOverturnLayout_loopOverturnLayout_displayDuration, 2000);
                mTurnDuringTime = typedArray.getInteger(R.styleable.LoopOverturnLayout_loopOverturnLayout_overturnDuration, 500);
                typedArray.recycle();
            }
        }
        mOverturnFieldName = getOverturnFieldName(mOverturnMode);
    }

    public void addChild(View child) {
        if (child != null) {
            child.setVisibility(getChildCount() > 0 ? View.GONE : View.VISIBLE);
            super.addView(child);
        }
    }

    public void removeChild(View child) {
        super.removeView(child);
    }

    public void clearChildren() {
        super.removeAllViews();
    }

    public void setOverTurnMode(int mode) {
        if (mOverturnMode != mode) {
            mOverturnMode = mode;
            mOverturnFieldName = getOverturnFieldName(mode);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        displayFirstChild();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startFirstOverturnAnim();
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        processChildAdded();
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        processChildRemoved();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopOverturnAnim();
    }

    private void processChildAdded() {
        if (ViewCompat.isAttachedToWindow(this) && !hasOverturnAnimRunning())
            startFirstOverturnAnim();
    }

    private void processChildRemoved() {
        stopOverturnAnim();
        if (ViewCompat.isAttachedToWindow(this))
            startOverturnAnim();
    }

    private void startFirstOverturnAnim() {
        final int childCount = getChildCount();
        if (childCount > 1) {
            mCurrentDisplayingChildIndex = 0;
            final View view = getChildAt(mCurrentDisplayingChildIndex);
            mOverturnAnimator = generateFirstOverturnAnim(view, mLoopListener);
            mOverturnAnimator.start();
        } else if (childCount == 1) {
            mCurrentDisplayingChildIndex = 0;
            final View child = getChildAt(mCurrentDisplayingChildIndex);
            if (child != null) {
                child.setRotationX(0);
                child.setRotationY(0);
            }
        }
    }

    private void startOverturnAnim() {
        final int childCount = getChildCount();
        if (childCount > 1) {
            mCurrentDisplayingChildIndex = 0;
            final View view = getChildAt(mCurrentDisplayingChildIndex);
            mOverturnAnimator = generateOverturnAnim(view, mLoopListener);
            mOverturnAnimator.start();
        }
    }

    private void stopOverturnAnim() {
        if (mOverturnAnimator != null && mOverturnAnimator.isRunning()) {
            mOverturnAnimator.removeAllListeners();
            mOverturnAnimator.cancel();
            mOverturnAnimator = null;
        }
        mCurrentDisplayingChildIndex = 0;
        final int childCount = getChildCount();
        if (childCount > 1) {
            displayFirstChild();
        }
    }

    private boolean hasOverturnAnimRunning() {
        return mOverturnAnimator != null && mOverturnAnimator.isRunning();
    }

    private void displayFirstChild() {
        final int childCount = getChildCount();
        if (childCount > 0) {
            View child;
            for (int i = 0; i < childCount; i++) {
                child = getChildAt(i);
                if (i == 0) {
                    child.setVisibility(View.VISIBLE);
                    child.setRotationX(0);
                    child.setRotationY(0);
                } else {
                    child.setVisibility(View.GONE);
                }
            }
        }
    }

    private void disappearAllChildren() {
        final int childCount = getChildCount();
        if (childCount > 0) {
            View child;
            for (int i = 0; i < childCount; i++) {
                child = getChildAt(i);
                child.setVisibility(View.GONE);
            }
        }
    }

    private Animator generateFirstOverturnAnim(View view, AnimatorListenerAdapter animatorListener) {
        if (view == null) return null;
        view.setRotationX(0);
        view.setRotationY(0);
        final ObjectAnimator turnOut = ObjectAnimator.ofFloat(view, mOverturnFieldName, 0, 90);
        turnOut.setDuration(mTurnDuringTime);
        turnOut.setStartDelay(mEachChildDisplayTime);
        turnOut.addListener(animatorListener);
        return turnOut;
    }

    private Animator generateOverturnAnim(View view, AnimatorListenerAdapter animatorListener) {
        if (view == null) return null;
        if (mOverturnMode == MODE_HORIZONTAL) {
            view.setRotationX(-90);
            view.setRotationY(0);
        } else {
            view.setRotationY(-90);
            view.setRotationX(0);
        }
        final ObjectAnimator turnIn = ObjectAnimator.ofFloat(view, mOverturnFieldName, -90, 0);
        turnIn.setDuration(mTurnDuringTime);
        final ObjectAnimator turnOut = ObjectAnimator.ofFloat(view, mOverturnFieldName, 0, 90);
        turnOut.setDuration(mTurnDuringTime);
        turnOut.setStartDelay(mEachChildDisplayTime);
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(turnIn, turnOut);
        set.addListener(animatorListener);
        return set;
    }

    private String getOverturnFieldName(int mode) {
        if (mode == MODE_HORIZONTAL) {
            return "rotationX";
        } else {
            return "rotationY";
        }
    }
}
