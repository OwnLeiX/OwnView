package lx.own.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lx.own.R;
import lx.own.view.tools.SingleTypeViewRecyclePool;

/**
 * <b></b>
 * Created on 2017/5/25.
 *
 * @author LeiXun
 */

public class SingleTypeExpandableVerticalLinearLayout extends ViewGroup implements View.OnClickListener {
    private static final String TAG = "ExpandableVerticalLinea";

    public static final int GRAVITY_LEFT = 1;
    public static final int GRAVITY_CENTER = 0;
    public static final int GRAVITY_RIGHT = -1;

    private boolean isExpanded = false;
    private int mShrinkMaxItems = 2;
    private String mExpandHintText = "展开";
    private int mExpandHintTextColor = 0xFF000000;
    private int mExpandHintTextViewWidth = -1;
    private int mExpandHintTextViewHeight = -1;
    private int mExpandHintTextViewGravity = GRAVITY_CENTER;
    private int mExpandHintTextViewPaddingLeft, mExpandHintTextViewPaddingTop, mExpandHintTextViewPaddingRight, mExpandHintTextViewPaddingBottom;
    private float mExpandHintTextSize = 20;
    private TextView mExpandClickTextView;
    private SingleTypeViewRecyclePool mRecycler;
    private SingleTypeExpandableVerticalLinearLayoutAdapter mAdapter;
    private List<View> mDisplayViews;
    private int mWidth, mHeight;

    public SingleTypeExpandableVerticalLinearLayout(Context context) {
        this(context, null);
    }

    public SingleTypeExpandableVerticalLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleTypeExpandableVerticalLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    //    public ExpandableVerticalLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    //        super(context, attrs, defStyleAttr, defStyleRes);
    //    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableVerticalLinearLayout);
            if (typedArray != null) {
                mExpandHintText = typedArray.getString(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintText);
                if (TextUtils.isEmpty(mExpandHintText))
                    mExpandHintText = "展开";
                mExpandHintTextViewWidth = typedArray
                        .getDimensionPixelSize(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextViewWidth, -1);
                mExpandHintTextViewHeight = typedArray
                        .getDimensionPixelSize(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextViewHeight, -1);
                float fontSize = typedArray.getDimension(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextSize, 12.0f);//拿到sp转换成的px(按dp转换的)
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                float sp = fontSize / displayMetrics.density;//将px转回sp的数值
                mExpandHintTextSize = sp * displayMetrics.scaledDensity + 0.5f;//将sp转回dp
                mExpandHintTextViewGravity = typedArray.getInt(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextGravity,
                        GRAVITY_LEFT);
                mExpandHintTextViewPaddingLeft = typedArray
                        .getDimensionPixelSize(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextViewPaddingLeft, 0);
                mExpandHintTextViewPaddingTop = typedArray
                        .getDimensionPixelSize(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextViewPaddingTop, 0);
                mExpandHintTextViewPaddingRight = typedArray
                        .getDimensionPixelSize(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextViewPaddingRight, 0);
                mExpandHintTextViewPaddingBottom = typedArray
                        .getDimensionPixelSize(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextViewPaddingBottom, 0);
                mExpandHintTextColor = typedArray.getColor(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_hintTextColor, 0xFF000000);
                mShrinkMaxItems = typedArray.getInt(R.styleable.ExpandableVerticalLinearLayout_expandableVerticalLinearLayout_shrinkItemCount, 2);
                typedArray.recycle();
            }
        }
        mDisplayViews = new ArrayList<>();
        mExpandClickTextView = new TextView(context);
        mExpandClickTextView.setGravity(Gravity.CENTER);
        mExpandClickTextView.setPadding(mExpandHintTextViewPaddingLeft, mExpandHintTextViewPaddingTop, mExpandHintTextViewPaddingRight,
                mExpandHintTextViewPaddingBottom);
        mExpandClickTextView.setText(mExpandHintText);
        mExpandClickTextView.setTextColor(mExpandHintTextColor);
        mExpandClickTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mExpandHintTextSize);
        mExpandClickTextView.setLayoutParams(new ViewGroup.LayoutParams(mExpandHintTextViewWidth < 0 ? LayoutParams.WRAP_CONTENT : mExpandHintTextViewWidth,
                mExpandHintTextViewHeight < 0 ? LayoutParams.WRAP_CONTENT : mExpandHintTextViewHeight));
        mExpandClickTextView.setOnClickListener(this);
        addView(mExpandClickTextView, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAdapter == null) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
            return;
        }
        if (mAdapter.getCount() <= 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        if (isExpanded) {
            int count = mAdapter.getCount();
            if (mDisplayViews.size() != count) {
                mWidth = 0;
                mHeight = 0;
                clear();
                View child;
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    child = mAdapter.getView(mRecycler == null ? null : mRecycler.obtain(), i, this);
                    if (child == null)
                        continue;
                    addView(child);
                    mDisplayViews.add(child);
                    LayoutParams childLp = child.getLayoutParams();
                    int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                            childLp == null ? 0 : childLp.width);
                    int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, childLp == null ? 0 : childLp.height);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    mHeight += child.getMeasuredHeight();
                    int chileWidth = child.getMeasuredWidth();
                    if (chileWidth > mWidth)
                        mWidth = chileWidth;
                }
                mWidth += getPaddingLeft() + getPaddingRight();
                mHeight += getPaddingTop() + getPaddingBottom();
            }
            mExpandClickTextView.setVisibility(View.GONE);
        } else {
            int count = mShrinkMaxItems < mAdapter.getCount() ? mShrinkMaxItems : mAdapter.getCount();
            if (mDisplayViews.size() != count) {
                mWidth = 0;
                mHeight = 0;
                clear();
                View child = null;
                for (int i = 0; i < count; i++) {
                    child = mAdapter.getView(mRecycler == null ? null : mRecycler.obtain(), i, this);
                    if (child == null)
                        continue;
                    addView(child);
                    mDisplayViews.add(child);
                    LayoutParams childLp = child.getLayoutParams();
                    int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                            childLp == null ? 0 : childLp.width);
                    int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, childLp == null ? 0 : childLp.height);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    mHeight += child.getMeasuredHeight();
                    int childWidth = child.getMeasuredWidth();
                    if (childWidth > mWidth)
                        mWidth = childWidth;
                }
                mWidth += getPaddingLeft() + getPaddingRight();
                mHeight += getPaddingTop() + getPaddingBottom();
                if (mShrinkMaxItems < mAdapter.getCount()) {
                    mExpandClickTextView.setVisibility(View.VISIBLE);
                    LayoutParams childLp = mExpandClickTextView.getLayoutParams();
                    int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                            childLp == null ? 0 : childLp.width);
                    int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, childLp == null ? 0 : childLp.height);
                    mExpandClickTextView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    mHeight += mExpandClickTextView.getMeasuredHeight();
                }
            }
        }
        setMeasuredDimension(getFinalSize(widthMeasureSpec, mWidth), getFinalSize(heightMeasureSpec, mHeight));
    }

    private int getFinalSize(int measureSpec, int size) {
        int mode = MeasureSpec.getMode(measureSpec);
        if (mode == MeasureSpec.EXACTLY)
            return MeasureSpec.getSize(measureSpec);
        else
            return size;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mAdapter == null)
            return;
        if (mAdapter.getCount() <= 0)
            return;
        int top = getPaddingTop();
        int left = getPaddingLeft();
        int itemCount = mDisplayViews.size();
        if (isExpanded) {
            for (int i = 0; i < itemCount; i++) {
                View child = mDisplayViews.get(i);
                if (child == mExpandClickTextView)
                    continue;
                int childHeight = child.getMeasuredHeight();
                child.layout(left, top, left + child.getMeasuredWidth(), top + childHeight);
                top += childHeight;
            }
            mExpandClickTextView.layout(0, 0, 0, 0);
        } else {
            int count = mShrinkMaxItems < itemCount ? mShrinkMaxItems : itemCount;
            for (int i = 0; i < itemCount; i++) {
                View child = mDisplayViews.get(i);
                if (child == mExpandClickTextView)
                    continue;
                if (i < count) {
                    int childHeight = child.getMeasuredHeight();
                    child.layout(left, top, left + child.getMeasuredWidth(), top + childHeight);
                    top += childHeight;
                } else {
                    child.layout(0, 0, 0, 0);
                }
            }
            if (mShrinkMaxItems < mAdapter.getCount()) {
                int hintLeft = 0;
                if (mExpandHintTextViewGravity == GRAVITY_LEFT) {
                    hintLeft = left;
                } else if (mExpandHintTextViewGravity == GRAVITY_RIGHT) {
                    hintLeft = (r - l) - getPaddingRight() - mExpandClickTextView.getMeasuredWidth();
                } else {
                    hintLeft = ((r - l) - mExpandClickTextView.getMeasuredWidth()) / 2 + left;
                }
                mExpandClickTextView.layout(hintLeft, top, mExpandClickTextView.getMeasuredWidth() + hintLeft, mExpandClickTextView.getMeasuredHeight() + top);
            } else {
                mExpandClickTextView.layout(0, 0, 0, 0);
            }
        }
    }

    public void expand() {
        reset(true);
        requestLayout();
    }

    private void reset(boolean expanded) {
        this.isExpanded = expanded;
        clear();
    }

    private void clear() {
        if (mRecycler != null)
            mRecycler.recycle(mDisplayViews);
        mDisplayViews.clear();
        removeAllViews();
        addView(mExpandClickTextView);
    }

    public void shrink() {
        reset(false);
        requestLayout();
    }

    public void setAdapter(SingleTypeExpandableVerticalLinearLayoutAdapter adapter) {
        this.mAdapter = adapter;
        shrink();
    }

    public void notifyDataSetChanged() {
        shrink();
    }

    public SingleTypeViewRecyclePool getRecyclerPool() {
        return mRecycler;
    }

    public void setRecyclePool(SingleTypeViewRecyclePool mRecycler) {
        this.mRecycler = mRecycler;
    }

    public int getShrinkMaxItems() {
        return mShrinkMaxItems;
    }

    public void setShrinkMaxItems(int count) {
        this.mShrinkMaxItems = count;
    }

    @Override
    public void onClick(View v) {
        if (v == mExpandClickTextView)
            expand();
    }

    public interface SingleTypeExpandableVerticalLinearLayoutAdapter {
        int getCount();

        View getView(@Nullable View convertView, int position, ViewGroup parent);
    }
}
