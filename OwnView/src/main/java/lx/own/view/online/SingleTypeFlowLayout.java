package lx.own.view.online;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

import lx.own.R;
import lx.own.view.online.tools.SingleTypeViewRecyclePool;

public class SingleTypeFlowLayout extends ViewGroup {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CENTER = 2;

    static final int FLAG_NEVER_MEASURED = 1;
    static final int FLAG_CONTENT_CHANGED = 1 << 1;
    static final int FLAG_SPEC_CHANGED = 1 << 2;
    static final int FLAG_LAYOUT_AGAIN = 1 << 3;
    static final int FLAG_INTERCEPT_REFRESH = 1 << 4;
    static final int MASK_MEASURE = FLAG_NEVER_MEASURED | FLAG_CONTENT_CHANGED | FLAG_SPEC_CHANGED;

    private int mFlags = FLAG_NEVER_MEASURED;
    protected final ArrayList<Line> mLines;
    protected final LinkedList<View> mDisplayViews;
    protected int mWidth, mHeight, mContentWidth, mContentHeight, mHorizontalPadding, mVerticalPadding, maxLines = -1;
    protected int preWMeasureSpec, preHMeasureSpec;
    protected int mGravity = LEFT;

    protected boolean fillMode = false;
    protected boolean dividerStartAndEnd = false;
    private int interceptedRefreshTimeCount;
    protected BaseFlowItemAdapter mAdapter;
    private SingleTypeViewRecyclePool mRecycler;

    public SingleTypeFlowLayout(Context context) {
        super(context);
        this.mLines = new ArrayList<>();
        this.mDisplayViews = new LinkedList<>();
        this.init(context, null);
    }

    public SingleTypeFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLines = new ArrayList<>();
        this.mDisplayViews = new LinkedList<>();
        this.init(context, attrs);
    }

    public SingleTypeFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mLines = new ArrayList<>();
        this.mDisplayViews = new LinkedList<>();
        this.init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SingleTypeFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mLines = new ArrayList<>();
        this.mDisplayViews = new LinkedList<>();
        this.init(context, attrs);
    }

    public void setAdapter(BaseFlowItemAdapter adapter) {
        this.mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.setFlowLayout(this);
        }
        refresh();
    }

    public SingleTypeViewRecyclePool getSingleTypeViewRecyclePool() {
        return mRecycler;
    }

    public void setSingleTypeViewRecyclePool(SingleTypeViewRecyclePool mRecycler) {
        this.mRecycler = mRecycler;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        checkMeasureSpec(widthMeasureSpec, heightMeasureSpec);
        if ((mFlags & MASK_MEASURE) > 0) {
            mFlags &= ~MASK_MEASURE;
            final int count = mAdapter == null ? 0 : mAdapter.getCount();
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
            mContentWidth = mWidth - getPaddingLeft() - getPaddingRight();
            reset(false);
            View child = null;
            LayoutParams childLayoutParams = null;
            final int defaultChildMeasureSpec = getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, LayoutParams.WRAP_CONTENT);
            int childWSpec = defaultChildMeasureSpec;
            int childHSpec = defaultChildMeasureSpec;
            synchronized (mLines) {
                for (int i = 0; i < count; i++) {
                    child = mAdapter.getView(mRecycler == null ? null : mRecycler.obtain(), i, this);
                    if (child == null)
                        continue;
                    childLayoutParams = child.getLayoutParams();
                    if (childLayoutParams != null) {
                        //Fixme 这里有个系统bug，Android 5.1 360F4，在TextView内文本正确的情况下，measure出来的width不对。我也不知道为什么
                        //Fixme 经研究发现，mContentWidth不变的情况下，TextView会保持原width不重新measure。
                        if (child instanceof TextView && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            final TextView castedChild = (TextView) child;
                            final int measuredWidth = (int) (castedChild.getPaint().measureText(castedChild.getText() == null ? "" : castedChild.getText().toString()) + child.getPaddingLeft() + child.getPaddingRight() + 0.5f);
                            childWSpec = getChildMeasureSpec(MeasureSpec.EXACTLY, 0, measuredWidth);
                        } else {
                            childWSpec = getChildMeasureSpec(MeasureSpec.makeMeasureSpec(mContentWidth, MeasureSpec.EXACTLY), 0, childLayoutParams.width);
                        }
                        childHSpec = getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, childLayoutParams.height);
                    }
                    child.measure(childWSpec, childHSpec);
                    if (!layoutChild(child, fillMode ? 0 : Math.max(0, mLines.size() - 1), i))
                        break;
                }
                mContentHeight = 0;
                if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
                    for (Line line : mLines) {
                        mContentHeight += line.getHeight();
                    }
                    mContentHeight -= mVerticalPadding;
                    if (mContentHeight < 0)
                        mContentHeight = 0;
                    mHeight = mContentHeight + getPaddingBottom() + getPaddingTop();
                } else {
                    mHeight = MeasureSpec.getSize(heightMeasureSpec);
                }
            }
            mFlags |= FLAG_LAYOUT_AGAIN;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if ((mFlags & FLAG_LAYOUT_AGAIN) == 0) return;
        mFlags &= ~FLAG_LAYOUT_AGAIN;
        int top = getPaddingTop();
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
                    final int right = r - l - getPaddingRight() - (dividerStartAndEnd ? mHorizontalPadding : 0);
                    final int superfluousSpace = right - left - line.getWidth();//剩余的空白
                    left = (int) (left + superfluousSpace / 2.0f + 0.5f);
                } else {
                    left = (dividerStartAndEnd ? mHorizontalPadding : 0) + getPaddingLeft();
                }
                line.mLaidArea.left = left;
                line.mLaidArea.top = top;
                int childWidth;
                float halfHeight;
                for (View item : line.mViews) {
                    childWidth = item.getMeasuredWidth();
                    halfHeight = item.getMeasuredHeight() / 2.0f;
                    item.layout(left, (int) (centerVertical - halfHeight), left + childWidth, (int) (centerVertical + halfHeight));
                    left += childWidth + mHorizontalPadding;
                }
                line.mLaidArea.right = left + line.width;
                top += line.getHeight();
                line.mLaidArea.bottom = top;
            }
        }
    }

    protected void refresh() {
        if (!isInterceptRefresh()) {
            reset(true);
            requestLayout();
        } else {
            interceptedRefreshTimeCount++;
        }
    }

    final protected void setInterceptRefresh(boolean intercept) {
        if (intercept) {
            mFlags |= FLAG_INTERCEPT_REFRESH;
        } else {
            mFlags &= ~FLAG_INTERCEPT_REFRESH;
            if (interceptedRefreshTimeCount > 0)
                refresh();
            interceptedRefreshTimeCount = 0;
        }
    }

    final protected boolean isInterceptRefresh() {
        return (mFlags & FLAG_INTERCEPT_REFRESH) != 0;
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SingleTypeFlowLayout);
            if (typedArray != null) {
                mHorizontalPadding = typedArray.getDimensionPixelSize(R.styleable.SingleTypeFlowLayout_flowLayout_horizontalPadding, 0);
                mVerticalPadding = typedArray.getDimensionPixelSize(R.styleable.SingleTypeFlowLayout_flowLayout_verticalPadding, 0);
                maxLines = typedArray.getInteger(R.styleable.SingleTypeFlowLayout_flowLayout_maxLines, -1);
                fillMode = typedArray.getBoolean(R.styleable.SingleTypeFlowLayout_flowLayout_fillMode, false);
                dividerStartAndEnd = typedArray.getBoolean(R.styleable.SingleTypeFlowLayout_flowLayout_dividerStartAndEnd, false);
                mGravity = typedArray.getInteger(R.styleable.SingleTypeFlowLayout_flowLayout_gravity, LEFT);
                typedArray.recycle();
            }
        }
    }

    private boolean layoutChild(View child, int line, int position) {
        if (maxLines > 0 && line >= maxLines)
            return false;
        Line l;
        if (mLines.size() <= line) {
            l = new Line(mHorizontalPadding, mVerticalPadding, position);
            mLines.add(l);
        } else {
            l = mLines.get(line);
        }
        if (l.getViewCount() == 0 || l.getWidth() + child.getMeasuredWidth() + mHorizontalPadding * (dividerStartAndEnd ? 3 : 1) <= mContentWidth) {
            addView(child);
            mDisplayViews.offer(child);
            l.addView(child);
        } else {
            layoutChild(child, line + 1, position);
        }
        return true;
    }

    private void reset(boolean addFlag) {
        if (mRecycler != null)
            mRecycler.recycle(mDisplayViews);
        mDisplayViews.clear();
        removeAllViews();
        mLines.clear();
        if (addFlag)
            mFlags |= FLAG_CONTENT_CHANGED;
    }

    private void checkMeasureSpec(int widthMeasureSpec, int heightMeasureSpec) {
        if (preWMeasureSpec != widthMeasureSpec || preHMeasureSpec != heightMeasureSpec) {
            mFlags |= FLAG_SPEC_CHANGED;
            preWMeasureSpec = widthMeasureSpec;
            preHMeasureSpec = heightMeasureSpec;
        }
    }

    static class Line {
        ArrayList<View> mViews;
        int width;
        int height;

        int horizontalPadding;
        int verticalPadding;
        final int startPosition;

        RectF mLaidArea;

        Line(int horizontalPadding, int verticalPadding, int startPosition) {
            mViews = new ArrayList<>();
            width = 0;
            height = 0;
            this.horizontalPadding = horizontalPadding;
            this.verticalPadding = verticalPadding;
            this.mLaidArea = new RectF();
            this.startPosition = startPosition;
        }

        void addView(View view) {
            if (mViews.size() == 0) {
                width += view.getMeasuredWidth();
            } else {
                width += view.getMeasuredWidth() + horizontalPadding;
            }
            height = Math.max(height, view.getMeasuredHeight());
            mViews.add(view);
        }

        int getViewCount() {
            return mViews.size();
        }

        int getWidth() {
            return width;
        }

        int getContentWidth() {
            return width - horizontalPadding * (getViewCount() - 1);
        }

        int getHeight() {
            return height + verticalPadding;
        }

        int getContentHeight() {
            return height;
        }
    }

    public static abstract class BaseFlowItemAdapter {
        protected SingleTypeFlowLayout mSingleTypeFlowLayout;

        private void setFlowLayout(SingleTypeFlowLayout singleTypeFlowLayout) {
            this.mSingleTypeFlowLayout = singleTypeFlowLayout;
        }

        public void notifyDataSetChanged() {
            if (mSingleTypeFlowLayout != null) {
                mSingleTypeFlowLayout.refresh();
            }
        }

        public abstract View getView(View convertView, int position, ViewGroup parent);

        public abstract int getCount();

    }
}
