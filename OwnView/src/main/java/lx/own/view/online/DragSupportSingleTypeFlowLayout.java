package lx.own.view.online;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.MainThread;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import lx.own.R;

public class DragSupportSingleTypeFlowLayout extends SingleTypeFlowLayout {

    private static final int MSG_PREPARING_DRAG = -1;
    private static final int MSG_PREPARING_REPLACE = -2;

    static final int FLAG_DRAGGING = 1 << 1;
    static final int FLAG_PREPARING_DRAG = 1 << 2;
    static final int FLAG_DRAG_LAYING_OUT = 1 << 3;
    static final int MASK_DRAG = FLAG_DRAGGING | FLAG_PREPARING_DRAG;

    private int mFlags;
    private DraggingDecoration mDraggingDecoration;
    private DraggingHelper mDraggingHelper;
    private final Handler mHandler;
    private View mPrepareDraggingView, mDraggingView;
    private Rect mDraggingViewTargetArea;
    private RectF mDraggingViewCurrentAreaOffsets;
    private int mInitialDraggingViewPosition, mDraggingViewPosition, mTargetPosition;
    private long mReplaceDuration = 200L;

    public DragSupportSingleTypeFlowLayout(Context context) {
        super(context);
        this.mHandler = new InnerHandler(this);
        this.init(context, null);
    }

    public DragSupportSingleTypeFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHandler = new InnerHandler(this);
        this.init(context, attrs);
    }

    public DragSupportSingleTypeFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHandler = new InnerHandler(this);
        this.init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DragSupportSingleTypeFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHandler = new InnerHandler(this);
        this.init(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDraggingHelper == null || fillMode) return super.onInterceptTouchEvent(ev);
        if ((mFlags & MASK_DRAG) == 0) {
            final int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                final float x = ev.getX();
                final float y = ev.getY();
                final ItemPair item = findTouchedItem(x, y);
                if (item != null && item.item != null) {
                    mFlags |= FLAG_PREPARING_DRAG;
                    setInterceptRefresh(true);
                    //没有确定时，这个View原位置要预留空白，所以这里不进行刷新
                    mPrepareDraggingView = item.item;
                    mDraggingViewTargetArea = new Rect(mPrepareDraggingView.getLeft(), mPrepareDraggingView.getTop(), mPrepareDraggingView.getRight(), mPrepareDraggingView.getBottom());
                    mDraggingViewCurrentAreaOffsets = new RectF(x - mPrepareDraggingView.getLeft(), y - mPrepareDraggingView.getTop(), x - mPrepareDraggingView.getRight(), y - mPrepareDraggingView.getBottom());
                    mInitialDraggingViewPosition = mDraggingViewPosition = mTargetPosition = item.position;
                    final ViewParent parent = getParent();
                    if (parent != null)
                        parent.requestDisallowInterceptTouchEvent(true);
                    mHandler.sendEmptyMessageDelayed(MSG_PREPARING_DRAG, ViewConfiguration.getLongPressTimeout());
                }
            }
        }
        return (mFlags & FLAG_DRAGGING) != 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDraggingHelper == null || fillMode) return super.onTouchEvent(event);
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            releaseDraggingFields();
            final ViewParent parent = getParent();
            if (parent != null)
                parent.requestDisallowInterceptTouchEvent(false);
        } else if (action == MotionEvent.ACTION_MOVE) {
            if ((mFlags & FLAG_DRAGGING) != 0) {
                final float x = event.getX();
                final float y = event.getY();
                if (mDraggingView != null) {
                    final int left = (int) (x - mDraggingViewCurrentAreaOffsets.left + 0.5f);
                    final int top = (int) (y - mDraggingViewCurrentAreaOffsets.top + 0.5f);
                    mDraggingView.layout(left,
                            top,
                            left + mDraggingView.getWidth(),
                            top + mDraggingView.getHeight());
                    if ((mFlags & FLAG_DRAG_LAYING_OUT) == 0) {
                        final int position = findLocationPosition(x, y);
                        if (position > -1 && position != mTargetPosition) {
                            mTargetPosition = position;
                            mHandler.removeMessages(MSG_PREPARING_REPLACE);
                            mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_PREPARING_REPLACE, position, -1), mReplaceDuration);
                        }
                    }
                } else {
                    releaseDraggingFields();
                    final ViewParent parent = getParent();
                    if (parent != null)
                        parent.requestDisallowInterceptTouchEvent(false);
                }
            } else if ((mFlags & FLAG_PREPARING_DRAG) != 0 && mPrepareDraggingView != null) {//还在准备中...如果移动超出了范围，直接取消所有任务
                final float x = event.getX();
                final float y = event.getY();
                if (mPrepareDraggingView.getLeft() > x || mPrepareDraggingView.getRight() < x || mPrepareDraggingView.getTop() > y || mPrepareDraggingView.getBottom() < y) {
                    releaseDraggingFields();
                    final ViewParent parent = getParent();
                    if (parent != null)
                        parent.requestDisallowInterceptTouchEvent(false);
                }
            }
        }
        return (mFlags & MASK_DRAG) != 0;
    }

    public void enableDragging(DraggingHelper helper, DraggingDecoration decoration) {
        if (helper == null) {
            disableDragging();
        } else if (mDraggingHelper == null) {
            this.mDraggingHelper = helper;
            this.mDraggingDecoration = decoration;
        }
    }

    public void disableDragging() {
        if (mDraggingHelper != null) {
            this.mDraggingHelper = null;
            releaseDraggingFields();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragSupportSingleTypeFlowLayout);
            if (typedArray != null) {
                mReplaceDuration = typedArray.getInteger(R.styleable.DragSupportSingleTypeFlowLayout_dragSupportSingleTypeFlowLayout_replaceDuration, (int) mReplaceDuration);
                typedArray.recycle();
            }
        }
        releaseDraggingFields();
    }

    private boolean draggingLayoutChild(View child, int line, int position) {
        if (maxLines > 0 && line >= maxLines)
            return false;
        Line l;
        synchronized (mLines) {
            if (mLines.size() <= line) {
                l = new Line(mHorizontalPadding, mVerticalPadding, position);
                mLines.add(l);
            } else {
                l = mLines.get(line);
            }
            if (l.getViewCount() == 0 || l.getWidth() + child.getMeasuredWidth() + mHorizontalPadding * (dividerStartAndEnd ? 3 : 1) <= mContentWidth) {
                l.addView(child);
            } else {
                draggingLayoutChild(child, line + 1, position);
            }
        }
        return true;
    }

    private ItemPair findTouchedItem(float x, float y) {
        View item = null;
        int position = -1;
        synchronized (mLines) {
            Line touchedLine = null;
            final Iterator<Line> lineIte = mLines.iterator();
            //noinspection WhileLoopReplaceableByForEach
            while (lineIte.hasNext()) {
                touchedLine = lineIte.next();
                if (touchedLine == null) continue;
                if (touchedLine.mLaidArea.contains(x, y)) {
                    break;
                } else {
                    touchedLine = null;
                }
            }
            if (touchedLine != null) {
                final ArrayList<View> items = touchedLine.mViews;
                for (int i = 0; i < items.size(); i++) {
                    item = items.get(i);
                    if (item == null) continue;
                    if (item.getLeft() <= x && item.getRight() >= x && item.getTop() < y && item.getBottom() > y) {
                        position = touchedLine.startPosition + i;
                        break;
                    } else {
                        item = null;
                    }
                }
            }
        }
        if (item != null) {
            return new ItemPair(item, position);
        } else {
            return null;
        }
    }

    private int findLocationPosition(float x, float y) {
        int position = -1;
        int lineIndex = -1;
        synchronized (mLines) {
            Line touchedLine = null;
            for (int i = 0, len = mLines.size(); i < len; i++) {
                touchedLine = mLines.get(i);
                if (touchedLine == null) continue;
                if (touchedLine.mLaidArea.top <= y && touchedLine.mLaidArea.bottom >= y) {
                    lineIndex = i;
                    break;
                } else if (i == 0 && touchedLine.mLaidArea.top > y) {
                    position = 0;
                    touchedLine = null;
                } else if (i + 1 == len && touchedLine.mLaidArea.bottom < y) {
                    position = mDisplayViews.size();
                    touchedLine = null;
                } else {
                    touchedLine = null;
                }
            }
            if (touchedLine != null) {
                final ArrayList<View> items = touchedLine.mViews;
                View item;
                float itemCenterX;
                for (int i = 0, len = items.size(); i < len; i++) {
                    item = items.get(i);
                    if (item == null || item == mDraggingView) continue;
                    if (i + 1 >= len) {//已经是这行最后一个item
                        itemCenterX = item.getLeft() + ((item.getRight() - item.getLeft()) / 2f);
                        if (itemCenterX >= x) {
                            position = touchedLine.startPosition + i;
                            break;
                        } else if (lineIndex + 1 >= mLines.size()) {//已经是最后一行
                            position = mDisplayViews.size();
                            break;
                        } else {
                            position = touchedLine.startPosition + i + 1;//position = 下一行第一个位置(重新layout后可能仍在本行)
                            break;
                        }
                    } else {//不是这行最后一个item
                        itemCenterX = item.getLeft() + ((item.getRight() - item.getLeft()) / 2f);
                        if (itemCenterX >= x) {
                            position = touchedLine.startPosition + i;
                            break;
                        } else {
                            item = items.get(i + 1);
                            if (item == mDraggingView) continue;
                            itemCenterX = item.getLeft() + ((item.getRight() - item.getLeft()) / 2f);
                            if (itemCenterX >= x) {
                                position = touchedLine.startPosition + i + 1;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return position;
    }

    private void draggingLayout() {
        final int count = mDisplayViews.size();
        View child;
        synchronized (mLines) {
            mLines.clear();
            for (int i = 0; i < count; i++) {
                child = mDisplayViews.get(i);
                if (child == null)
                    continue;
                if (!draggingLayoutChild(child, Math.max(0, mLines.size() - 1), i))
                    break;
            }
            mContentHeight = 0;
            if (MeasureSpec.getMode(preHMeasureSpec) != MeasureSpec.EXACTLY) {
                int contentHeight = 0, totalHeight = 0;
                for (Line line : mLines) {
                    contentHeight += line.getHeight();
                }
                contentHeight -= mVerticalPadding;
                if (contentHeight < 0)
                    contentHeight = 0;
                totalHeight = contentHeight + getPaddingBottom() + getPaddingTop();
                if (totalHeight != mHeight) {
                    //TODO height changed.
                }
            }

            int top = getPaddingTop();
            final DraggingDecoration decoration = this.mDraggingDecoration;
            Line line;
            synchronized (mLines) {
                for (int position = 0; position < mLines.size(); position++) {
                    line = mLines.get(position);
                    if (line == null)
                        continue;
                    float centerVertical = (top + top + line.getContentHeight()) / 2.0f;
                    int left;
                    if (mGravity == RIGHT) {
                        final int right = mContentWidth - mHorizontalPadding;
                        left = right - line.getWidth();
                    } else if (mGravity == CENTER) {
                        left = (dividerStartAndEnd ? mHorizontalPadding : 0) + getPaddingLeft();
                        final int right = mWidth - getPaddingRight() - (dividerStartAndEnd ? mHorizontalPadding : 0);
                        final int superfluousSpace = right - left - line.getWidth();//剩余的空白
                        left = (int) (left + superfluousSpace / 2.0f + 0.5f);
                    } else {
                        left = (dividerStartAndEnd ? mHorizontalPadding : 0) + getPaddingLeft();
                    }
                    line.mLaidArea.left = left;
                    line.mLaidArea.top = top;
                    int oldLeft, oldTop, newTop, childWidth;
                    float halfHeight;
                    for (View item : line.mViews) {
                        childWidth = item.getMeasuredWidth();
                        halfHeight = item.getMeasuredHeight() / 2.0f;
                        oldLeft = item.getLeft();
                        oldTop = item.getTop();
                        newTop = (int) (centerVertical - halfHeight);
                        if (mDraggingView != item) {
                            item.layout(left, newTop, left + childWidth, (int) (centerVertical + halfHeight));
                            if (decoration != null)
                                decoration.onUpdateOtherItemsLocation(item, oldLeft, oldTop, left, newTop);
                        } else {
                            mDraggingViewTargetArea.set(left, newTop, left + childWidth, (int) (centerVertical + halfHeight));
                        }
                        left += childWidth + mHorizontalPadding;
                    }
                    line.mLaidArea.right = left + line.width;
                    top += line.getHeight();
                    line.mLaidArea.bottom = top;
                }
            }
        }
    }

    private void releaseDraggingFields() {
        mHandler.removeCallbacksAndMessages(null);
        if (mDraggingView != null) {
            if (mDraggingHelper != null)
                mDraggingHelper.onEndDragging(mInitialDraggingViewPosition, mDraggingViewPosition, mDraggingView);
            final int oldLeft = mDraggingView.getLeft();
            final int oldTop = mDraggingView.getTop();
            mDraggingView.layout(mDraggingViewTargetArea.left, mDraggingViewTargetArea.top, mDraggingViewTargetArea.right, mDraggingViewTargetArea.bottom);
            if (mDraggingDecoration != null) {
                for (View item : mDisplayViews) {
                    if (item == mDraggingView) {
                        mDraggingDecoration.processDraggingItemAfterDragging(item);
                    } else {
                        mDraggingDecoration.processOtherItemsAfterDragging(item);
                    }
                }
                mDraggingDecoration.onRestoreDraggingItemLocation(mDraggingView, oldLeft, oldTop, mDraggingViewTargetArea.left, mDraggingViewTargetArea.top);
            }
        }
        mDraggingViewPosition = -1;
        mTargetPosition = -1;
        mPrepareDraggingView = null;
        mDraggingView = null;
        mDraggingViewCurrentAreaOffsets = null;
        mDraggingViewTargetArea = null;
        mFlags &= ~MASK_DRAG;
        setInterceptRefresh(false);
    }

    public interface DraggingDecoration {
        @MainThread
        void processDraggingItemBeforeDragging(View item);

        @MainThread
        void processDraggingItemAfterDragging(View item);

        @MainThread
        void processOtherItemsBeforeDragging(View item);

        @MainThread
        void processOtherItemsAfterDragging(View item);

        @MainThread
        void onRestoreDraggingItemLocation(View item, int oldX, int oldY, int newX, int newY);

        @MainThread
        void onUpdateOtherItemsLocation(View item, int oldX, int oldY, int newX, int newY);
    }

    public interface DraggingHelper {

        //AsThisTime,you should remove corresponding data in BaseFlowItemAdapter#getView(View,int,ViewGroup) and BaseFlowItemAdapter#getCount()
        @MainThread
        void onStartDragging(int position, View convertView);

        //AsThisTime,you should add corresponding data in BaseFlowItemAdapter#getView(View,int,ViewGroup) and BaseFlowItemAdapter#getCount()
        @MainThread
        void onEndDragging(int oldPosition, int newPosition, View convertView);
    }

    private static class InnerHandler extends Handler {

        private final WeakReference<DragSupportSingleTypeFlowLayout> mLayoutRef;

        private InnerHandler(DragSupportSingleTypeFlowLayout layout) {
            super(Looper.getMainLooper());
            mLayoutRef = new WeakReference<>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final DragSupportSingleTypeFlowLayout layout = mLayoutRef.get();
            if (layout == null) return;
            switch (msg.what) {
                case MSG_PREPARING_DRAG:
                    if ((layout.mFlags & FLAG_PREPARING_DRAG) != 0) {
                        layout.mFlags &= ~FLAG_PREPARING_DRAG;
                        if (layout.mPrepareDraggingView != null) {
                            layout.mFlags |= FLAG_DRAGGING;
                            if (layout.mDraggingDecoration != null) {
                                for (View item : layout.mDisplayViews) {
                                    if (item == layout.mPrepareDraggingView) {
                                        layout.mDraggingDecoration.processDraggingItemBeforeDragging(item);
                                    } else {
                                        layout.mDraggingDecoration.processOtherItemsBeforeDragging(item);
                                    }
                                }
                            }
                            if (layout.mDraggingHelper != null)
                                layout.mDraggingHelper.onStartDragging(layout.mInitialDraggingViewPosition, layout.mPrepareDraggingView);
                            layout.mDraggingView = layout.mPrepareDraggingView;
                            layout.bringChildToFront(layout.mDraggingView);
                            layout.mPrepareDraggingView = null;
                        }
                    }
                    break;
                case MSG_PREPARING_REPLACE:
                    if ((layout.mFlags & FLAG_DRAGGING) != 0) {
                        final int targetPosition = msg.arg1;
                        if (targetPosition == layout.mTargetPosition) {
                            layout.mFlags |= FLAG_DRAG_LAYING_OUT;
                            if (layout.mTargetPosition < layout.mDraggingViewPosition) {
                                layout.mDisplayViews.add(layout.mTargetPosition, layout.mDisplayViews.remove(layout.mDraggingViewPosition));
                            } else if (layout.mTargetPosition > layout.mDraggingViewPosition) {
                                layout.mDisplayViews.add(--layout.mTargetPosition, layout.mDisplayViews.remove(layout.mDraggingViewPosition));
                            }
                            layout.mDraggingViewPosition = layout.mTargetPosition;
                            layout.draggingLayout();
                            layout.mFlags &= ~FLAG_DRAG_LAYING_OUT;
                        }
                    }
                    break;
            }
        }
    }

    private class ItemPair {
        public final View item;
        public final int position;

        private ItemPair(View item, int position) {
            this.item = item;
            this.position = position;
        }
    }

    public static class DefaultDraggingDecoration implements DraggingDecoration {

        private final ValueAnimator mAnimator;
        private final ArrayList<View> mAnimList;
        private final ValueAnimator.AnimatorUpdateListener mUpdateListener;

        {
            mAnimList = new ArrayList<>();
            mAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            mAnimator.setDuration(100L);
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.setRepeatMode(ValueAnimator.REVERSE);
            mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    final float rotation = -0.75f + fraction * 1.5f;
                    synchronized (mAnimList) {
                        for (View view : mAnimList) {
                            view.setRotation(rotation);
                        }
                    }
                }
            };
        }

        private void startAnim() {
            if (!mAnimator.isRunning()) {
                mAnimator.addUpdateListener(mUpdateListener);
                mAnimator.start();
            }
        }

        private void endAnim() {
            if (mAnimator.isRunning()) {
                mAnimator.removeUpdateListener(mUpdateListener);
                mAnimator.cancel();
            }
        }

        @Override
        public void processDraggingItemBeforeDragging(View item) {
            synchronized (mAnimList) {
                mAnimList.add(item);
            }
            startAnim();
        }

        @Override
        public void processDraggingItemAfterDragging(View item) {
            endAnim();
            item.setRotation(0);
        }

        @Override
        public void processOtherItemsBeforeDragging(View item) {
            synchronized (mAnimList) {
                mAnimList.add(item);
            }
            startAnim();
        }

        @Override
        public void processOtherItemsAfterDragging(View item) {
            endAnim();
            item.setRotation(0);
        }

        @Override
        public void onRestoreDraggingItemLocation(View item, int oldX, int oldY, int newX, int newY) {
            if (oldX != newX || oldY != newY) {
                final int translationX = oldX - newX;
                final int translationY = oldY - newY;
                item.setTranslationX(translationX);
                item.setTranslationY(translationY);
                final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(item,
                        PropertyValuesHolder.ofFloat("translationX", translationX, 0),
                        PropertyValuesHolder.ofFloat("translationY", translationY, 0));
                animator.setDuration(50L);
                animator.start();
            }
        }

        @Override
        public void onUpdateOtherItemsLocation(View item, int oldX, int oldY, int newX, int newY) {
            if (oldX != newX || oldY != newY) {
                final int translationX = oldX - newX;
                final int translationY = oldY - newY;
                item.setTranslationX(translationX);
                item.setTranslationY(translationY);
                final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(item,
                        PropertyValuesHolder.ofFloat("translationX", translationX, 0),
                        PropertyValuesHolder.ofFloat("translationY", translationY, 0));
                animator.setDuration(200L);
                animator.start();
            }
        }
    }
}
