package lx.own.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;

import lx.own.R;
import lx.own.view.tools.SingleTypeViewRecyclePool;

public class SingleTypeFlowLayout extends ViewGroup {
    private ArrayList<Line> mLines;
    private LinkedList<View> mDisplayViews;
    private int mWidth, mHeight;
    private int mContentWidth, mContentHeight;

    private int mHorizontalPadding;
    private int mVerticalPadding;

    private int maxLines = -1;
    private BaseFlowItemAdapter mAdapter;
    private SingleTypeViewRecyclePool mRecycler;

    public SingleTypeFlowLayout(Context context) {
        this(context, null);
    }

    public SingleTypeFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleTypeFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    //    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    //        super(context, attrs, defStyleAttr, defStyleRes);
    //    }

    private void init(Context context, AttributeSet attrs) {
        mLines = new ArrayList<>();
        mDisplayViews = new LinkedList<>();
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
            if (typedArray != null) {
                mHorizontalPadding = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_flowLayout_horizontalPadding, 0);
                mVerticalPadding = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_flowLayout_verticalPadding, 0);
                maxLines = typedArray.getInteger(R.styleable.FlowLayout_flowLayout_maxLines, -1);
                typedArray.recycle();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = mAdapter == null ? 0 : mAdapter.getCount();
        if (count != mDisplayViews.size()) {
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
            mContentWidth = mWidth - getPaddingLeft() - getPaddingRight();
            reset();
            View child = null;
            for (int i = 0; i < count; i++) {
                child = mAdapter.getView(mRecycler == null ? null : mRecycler.obtain(), i, this);
                if (child == null)
                    continue;
                child.measure(0, 0);
                layoutChild(child, 0, i);
            }
            mContentHeight = 0;
            if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
                for (Line line : mLines) {
                    mContentHeight += line.getHeight();
                }
                mContentHeight -= mVerticalPadding;
                mHeight = mContentHeight + getPaddingBottom() + getPaddingTop();
            } else {
                mHeight = MeasureSpec.getSize(heightMeasureSpec);
            }
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    private void layoutChild(View child, int line, int position) {
        if (maxLines > 0 && line >= maxLines)
            return;
        Line l;
        if (mLines.size() <= line) {
            l = new Line(mHorizontalPadding, mVerticalPadding);
            mLines.add(l);
        } else {
            l = mLines.get(line);
        }
        if (l.getViewCount() == 0 || l.getWidth() + child.getMeasuredWidth() + mHorizontalPadding * 3 <= mContentWidth) {
            addView(child);
            mDisplayViews.offer(child);
            l.addView(child);
        } else {
            layoutChild(child, line + 1, position);
        }
    }

    private void reset() {
        if (mRecycler != null)
            mRecycler.recycle(mDisplayViews);
        mDisplayViews.clear();
        removeAllViews();
        mLines.clear();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = getPaddingTop();
        Line line = null;
        for (int position = 0; position < mLines.size(); position++) {
            line = mLines.get(position);
            if (line == null)
                continue;
            float centerVertical = (top + top + line.getContentHeight()) / 2.0f;
            int left = mHorizontalPadding + getPaddingLeft();
            //强行拉满每一行
            //			int right = mContentWidth - mHorizontalPadding;
            //            int superfluousSpace = right - left - line.getWidth();
            //            float singleSuperfluousSpace = superfluousSpace * 1.0f / line.getViewCount() / 2.0f;
            for (View mView : line.mViews) {
//                mView.measure(MeasureSpec.makeMeasureSpec(mView.getMeasuredWidth() + singleSuperfluousSpace * 2.0f, MeasureSpec.EXACTLY),
//                        MeasureSpec.makeMeasureSpec(mView.getMeasuredHeight(), MeasureSpec.EXACTLY));
                int childWidth = mView.getMeasuredWidth();
                float halfHeight = mView.getMeasuredHeight() / 2.0f;
                mView.layout(left, (int) (centerVertical - halfHeight), left + childWidth, (int) (centerVertical + halfHeight));
                left += childWidth + mHorizontalPadding;
            }
            top += line.getHeight();
        }
    }

    public int getVerticalPadding() {
        return mVerticalPadding;
    }

    public void setVerticalPadding(int verticalPadding) {
        this.mVerticalPadding = verticalPadding;
        refresh();
    }

    public int getHorizontalPadding() {
        return mHorizontalPadding;
    }

    public void setHorizontalPadding(int horizontalPadding) {
        this.mHorizontalPadding = horizontalPadding;
        refresh();
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        if (maxLines <= 0) {
            this.maxLines = -1;
        } else {
            this.maxLines = maxLines;
        }
        refresh();
    }

    public void setAdapter(BaseFlowItemAdapter adapter) {
        this.mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.setFlowLayout(this);
        }
        refresh();
    }

    private void refresh() {
        reset();
        requestLayout();
    }

    public SingleTypeViewRecyclePool getSingleTypeViewRecyclePool() {
        return mRecycler;
    }

    public void setSingleTypeViewRecyclePool(SingleTypeViewRecyclePool mRecycler) {
        this.mRecycler = mRecycler;
    }

    static class Line {
        public ArrayList<View> mViews;
        public int width;
        public int height;

        public int horizontalPadding;
        public int verticalPadding;

        public Line() {
            this(0, 0);
        }

        public Line(int horizontalPadding, int verticalPadding) {
            mViews = new ArrayList<>();
            width = 0;
            height = 0;
            this.horizontalPadding = horizontalPadding;
            this.verticalPadding = verticalPadding;
        }

        public void addView(View view) {
            if (mViews.size() == 0) {
                width += view.getMeasuredWidth();
            } else {
                width += view.getMeasuredWidth() + horizontalPadding;
            }
            height = Math.max(height, view.getMeasuredHeight());
            mViews.add(view);
        }

        public int getViewCount() {
            return mViews.size();
        }

        public View getViewAt(int index) {
            return mViews.get(index);
        }

        public int getWidth() {
            return width;
        }

        public int getContentWidth() {
            return width - horizontalPadding * (getViewCount() - 1);
        }

        public int getHeight() {
            return height + verticalPadding;
        }

        public int getContentHeight() {
            return height;
        }

        public int getVerticalPadding() {
            return verticalPadding;
        }

        public void setVerticalPadding(int verticalPadding) {
            this.verticalPadding = verticalPadding;
        }

        public int getHorizontalPadding() {
            return horizontalPadding;
        }

        public void setHorizontalPadding(int horizontalPadding) {
            this.horizontalPadding = horizontalPadding;
        }

    }

    public static abstract class BaseFlowItemAdapter {
        private SingleTypeFlowLayout mSingleTypeFlowLayout;

        private void setFlowLayout(SingleTypeFlowLayout singleTypeFlowLayout) {
            this.mSingleTypeFlowLayout = singleTypeFlowLayout;
        }

        final public void notifyDataSetChanged() {
            if (mSingleTypeFlowLayout != null) {
                mSingleTypeFlowLayout.refresh();
            }
        }

        public abstract View getView(View convertView, int position, ViewGroup parent);

        public abstract int getCount();

    }
}
